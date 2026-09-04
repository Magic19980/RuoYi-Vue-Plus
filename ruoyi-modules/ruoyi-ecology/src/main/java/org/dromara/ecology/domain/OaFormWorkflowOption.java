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

/** 可跨表单复用的审批方式。optionCode 是系统内关联编码，实际泛微值由表单字段选项维护。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_workflow_option")
public class OaFormWorkflowOption extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 系统审批方式编码，不直接作为泛微字段值提交。 */
    private String optionCode;

    private String optionName;

    /** 数据库历史字段，新模型不参与泛微流程判断。 */
    private String processType;

    /** 该审批方式的节点字段映射 JSON。 */
    private String participantMappingJson;

    private Integer sortNo;

    private String status;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
