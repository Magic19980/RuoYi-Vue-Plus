package org.dromara.department.service;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 将房间预约相关的外部副作用统一延迟到数据库事务提交之后。
 *
 * <p>预约事实、权限变更和楼层发布都可能触发站内推送或外部通知。
 * 如果当前调用处在真实事务中，提交前不应让其他客户端看到尚未落库的状态；
 * 非事务任务则保持原有的立即执行行为。</p>
 */
public final class RoomBookingTransactionPublisher {

    private RoomBookingTransactionPublisher() {
    }

    public static void afterCommitOrNow(Runnable action) {
        if (action == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()
            && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}
