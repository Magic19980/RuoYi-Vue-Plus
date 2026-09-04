package org.dromara.ecology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ecology.domain.OaApplication;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** 泛微通用审批申请视图。 */
@Data
@AutoMapper(target = OaApplication.class)
public class OaApplicationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String applicationNo;
    private String businessType;
    private String sourceModule;
    private String businessId;
    private String businessNo;
    private String title;
    private String content;
    private String urgency;
    private String formDataJson;
    private List<OaAttachmentVo> attachments;
    private List<OaApprovalParticipantVo> participants;
    private Long applicantUserId;
    private String applicantName;
    private Long deptId;
    private List<Long> deptIds;
    private Long companyId;
    private String processType;
    private Long approvalPlanId;
    private String approvalMode;
    private Long workflowConfigId;
    private String formName;
    private String workflowName;
    private String approvalCode;
    private String approvalName;
    private String workflowId;
    private String status;
    private Long processId;
    private String oaRequestId;
    private String localStatus;
    private String oaStatus;
    private String oaStatusRaw;
    private String requestName;
    private String oaLink;
    private String failReason;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastSyncAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
