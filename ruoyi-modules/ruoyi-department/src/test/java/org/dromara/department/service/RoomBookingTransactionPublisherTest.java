package org.dromara.department.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 验证预约推送和外部通知不会早于业务事务提交。 */
@Tag("dev")
class RoomBookingTransactionPublisherTest {

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void publishesImmediatelyWhenNoTransactionIsActive() {
        AtomicInteger calls = new AtomicInteger();

        RoomBookingTransactionPublisher.afterCommitOrNow(calls::incrementAndGet);

        assertEquals(1, calls.get());
    }

    @Test
    void defersPublishUntilAfterCommitWhenTransactionIsActive() {
        AtomicInteger calls = new AtomicInteger();
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        RoomBookingTransactionPublisher.afterCommitOrNow(calls::incrementAndGet);

        assertEquals(0, calls.get());
        TransactionSynchronizationManager.getSynchronizations().get(0).afterCommit();
        assertEquals(1, calls.get());
    }

    @Test
    void rollbackPathDoesNotPublish() {
        AtomicInteger calls = new AtomicInteger();
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        RoomBookingTransactionPublisher.afterCommitOrNow(calls::incrementAndGet);
        TransactionSynchronizationManager.getSynchronizations().get(0).afterCompletion(1);

        assertEquals(0, calls.get());
    }

    @Test
    void ignoresNullAction() {
        RoomBookingTransactionPublisher.afterCommitOrNow(null);
    }
}
