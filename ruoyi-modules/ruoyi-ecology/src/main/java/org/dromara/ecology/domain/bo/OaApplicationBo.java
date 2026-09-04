package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 泛微通用审批申请参数。 */
@Data
public class OaApplicationBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "申请主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "业务类型不能为空")
    @Size(max = 64, message = "业务类型不能超过64个字符")
    private String businessType;

    /** 来源模块，用于业务方回写和权限审计。 */
    @Size(max = 64, message = "来源模块不能超过64个字符")
    private String sourceModule;

    /** 来源业务单据主键。 */
    @Size(max = 100, message = "业务单据主键不能超过100个字符")
    private String businessId;

    /** 来源业务单号。 */
    @Size(max = 100, message = "业务单号不能超过100个字符")
    private String businessNo;

    /** 单部门申请的兼容字段；多部门申请使用 deptIds。 */
    private Long deptId;

    /** 申请涉及的部门，AUTO 模式下用于解析审批范围。 */
    private List<Long> deptIds;

    @NotBlank(message = "申请标题不能为空")
    @Size(max = 200, message = "申请标题不能超过200个字符")
    private String title;

    @NotBlank(message = "申请内容不能为空")
    @Size(max = 5000, message = "申请内容不能超过5000个字符")
    private String content;

    @Size(max = 20, message = "紧急程度不能超过20个字符")
    private String urgency;

    /** 结构化表单数据，JSON 对象。 */
    @Size(max = 100000, message = "表单数据不能超过100000个字符")
    private String formDataJson;

    /** 已上传到本地 OSS 的附件。 */
    private List<OaAttachmentBo> attachments;

    /** 审批人编排，保存为快照后再提交泛微。 */
    private List<OaApprovalParticipantBo> participants;

    @NotNull(message = "泛微流程配置不能为空")
    private Long workflowConfigId;

    /** 业务申请使用的公司/组织 ID。 */
    private Long companyId;

    /** SEQUENTIAL、COUNTERSIGN、MIXED。 */
    private String processType = "SEQUENTIAL";

    /** 可选的审批方案；不填写时由后端按业务规则自动匹配。 */
    private Long approvalPlanId;

    /** AUTO_RULE 自动匹配 / PLAN 选择方案 / MANUAL 本次临时指定。 */
    private String approvalMode = "AUTO_RULE";
}
