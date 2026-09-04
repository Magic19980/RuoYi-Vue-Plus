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

/** 泛微审批方案。流程定义与审批人员方案分离，方案可被多个业务复用。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_department_approval")
public class OaDepartmentApproval extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long workflowConfigId;
    private String businessType;
    private String sourceModule;
    /** 业务需要发起泛微流程的归属组织；为空表示该业务下全组织通用。 */
    private Long businessDeptId;
    /** 业务人员可识别的方案名称，例如“财务复核方案”。 */
    private String planName;
    /** 业务字段匹配条件，仅管理员维护；为空表示该业务下的通用方案。 */
    private String matchConditionJson;
    /** 数值越大越优先，条件相同时用于确定自动匹配结果。 */
    private Integer priority;
    private String status;
    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
