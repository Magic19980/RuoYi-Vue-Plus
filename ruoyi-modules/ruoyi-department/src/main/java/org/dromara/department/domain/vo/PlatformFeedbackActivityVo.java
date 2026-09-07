package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 平台问题与建议操作记录视图。
 */
@Data
public class PlatformFeedbackActivityVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String actionType;

    private String actionNote;

    private String fromStatus;

    private String toStatus;

    private String operatorName;

    private LocalDateTime createTime;
}
