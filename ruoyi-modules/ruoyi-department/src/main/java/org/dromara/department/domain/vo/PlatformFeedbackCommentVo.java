package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 平台问题与建议评论视图。
 */
@Data
public class PlatformFeedbackCommentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long feedbackId;

    private String content;

    private String authorName;

    /** 作者头像对应的 OSS ID。 */
    private Long authorAvatar;

    private String deptName;

    private Boolean mine;

    private LocalDateTime createTime;
}
