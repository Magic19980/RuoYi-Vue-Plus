package org.dromara.ecology.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.ecology.service.IOaApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 泛微审批状态自动对账任务。 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ecology", name = "reconcile-enabled", havingValue = "true")
public class EcologyReconcileTask {

    private final IOaApplicationService applicationService;

    @Scheduled(fixedDelayString = "${ecology.reconcile-fixed-delay-millis:300000}",
        initialDelayString = "${ecology.reconcile-initial-delay-millis:60000}")
    public void reconcile() {
        try {
            applicationService.reconcileDue();
        } catch (Exception ex) {
            log.error("泛微审批自动对账任务执行失败", ex);
        }
    }
}
