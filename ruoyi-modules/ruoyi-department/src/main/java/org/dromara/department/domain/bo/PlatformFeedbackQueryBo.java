package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 平台问题与建议查询参数。
 */
@Data
public class PlatformFeedbackQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;

    private String feedbackType;

    private String priority;

    private String status;

    private String moduleName;

    private Long assigneeId;

    private String feed;
}
