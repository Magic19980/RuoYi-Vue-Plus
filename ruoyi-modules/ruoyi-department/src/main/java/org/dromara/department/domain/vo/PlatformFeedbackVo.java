package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台问题与建议列表和详情视图。
 */
@Data
public class PlatformFeedbackVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String feedbackNo;

    private String feedbackType;

    private String title;

    private String description;

    private String moduleName;

    private String pageTitle;

    private String pagePath;

    private String businessRef;

    private String reproduceSteps;

    private String expectedResult;

    private String actualResult;

    private String impactScope;

    private String priority;

    private String status;

    private Long assigneeId;

    private String assigneeName;

    private String assigneeDeptName;

    private String reporterName;

    private String reporterDeptName;

    private Long createBy;

    private String resolutionNote;

    private String attachmentOssIds;

    private LocalDateTime closedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean mine;

    private Boolean assignedToMe;

    /** 当前登录用户是否可以保存处理进展。 */
    private Boolean processAllowed;

    private List<PlatformFeedbackAttachmentVo> attachments;

    private List<PlatformFeedbackActivityVo> activities;
}
