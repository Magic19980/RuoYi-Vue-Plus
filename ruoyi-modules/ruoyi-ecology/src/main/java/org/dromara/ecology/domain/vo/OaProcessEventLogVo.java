package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 审批事件日志视图。 */
@Data
public class OaProcessEventLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long processId;
    private String eventType;
    private String fromStatus;
    private String toStatus;
    private String requestSummary;
    private String responseSummary;
    private String errorCode;
    private LocalDateTime createTime;
}
