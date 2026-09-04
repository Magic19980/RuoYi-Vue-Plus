package org.dromara.ecology.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.ecology.config.EcologyProperties;
import org.dromara.ecology.service.IOaHrmSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 泛微 HRM 组织与人员定时同步任务。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OaHrmSyncTask {

    private final EcologyProperties properties;
    private final IOaHrmSyncService syncService;

    @Scheduled(fixedDelayString = "${ecology.hrm-sync-fixed-delay-millis:3600000}",
        initialDelayString = "${ecology.hrm-sync-initial-delay-millis:120000}")
    public void sync() {
        if (!properties.isEnabled() || !properties.isHrmSyncEnabled()) {
            return;
        }
        try {
            syncService.syncOrganization(false);
            syncService.syncUsers(false);
        } catch (Exception ex) {
            log.error("泛微 HRM 定时同步失败", ex);
        }
    }
}
