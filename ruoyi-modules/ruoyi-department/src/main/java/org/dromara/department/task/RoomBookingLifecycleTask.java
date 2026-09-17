package org.dromara.department.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.department.service.IRoomBookingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 房间预约生命周期后台任务：未签到自动释放，结束后自动完成。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomBookingLifecycleTask {

    private final IRoomBookingService roomBookingService;

    @Scheduled(fixedDelayString = "${department.room.lifecycle-fixed-delay-millis:60000}", initialDelayString = "${department.room.lifecycle-initial-delay-millis:30000}")
    public void process() {
        try {
            int count = roomBookingService.processBookingLifecycle();
            if (count > 0) {
                log.info("房间预约生命周期任务处理 {} 条实例", count);
            }
        } catch (Exception ex) {
            log.error("房间预约生命周期任务执行失败", ex);
        }
    }
}
