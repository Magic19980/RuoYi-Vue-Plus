package org.dromara.department.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 房间预约外部通知渠道配置。默认不配置企业微信，避免未授权时外发消息。 */
@Data
@Component
@ConfigurationProperties(prefix = "room-booking.notification")
public class RoomBookingNotificationProperties {

    /** 企业微信群机器人 Webhook 地址，可留空。 */
    private String wecomWebhookUrl;
}
