package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 泛微流程配置，不存放密钥。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_workflow_config")
public class OaWorkflowConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 表单主配置 ID；当前对象是表单与审批选项合并后的提交视图。 */
    private Long formId;

    private String workflowId;

    private String workflowName;

    /** 可复用的泛微表单名称。旧数据使用 sourceWorkflowName 作为回退值。 */
    private String formName;

    /** 全局审批方式系统编码；提交时由当前表单的字段选项转换为泛微实际值。 */
    private String approvalCode;

    /** 审批方式展示名称，例如依次签、会签、部门负责人审批。 */
    private String approvalName;

    /** 现有本地审批方案的兼容模式，实际泛微审批值由表单字段选项决定。 */
    private String processType;

    /** 所选审批方式对应的节点字段映射。 */
    private String participantMappingJson;

    /** 旧模型中的原始泛微流程名称，仅用于兼容历史数据。 */
    private String sourceWorkflowName;

    private String requestNameTemplate;

    private String fieldMappingJson;

    /** 业务表单专属字段映射 JSON。 */
    private String specificFieldMappingJson;

    /** 当前泛微表单的字段定义及字段编码映射。 */
    private String fieldSchemaJson;

    private String status;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
