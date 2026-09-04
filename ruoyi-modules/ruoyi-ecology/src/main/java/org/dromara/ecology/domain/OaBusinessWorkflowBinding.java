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

/** 业务类型选择的泛微表单及默认审批方式。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_business_workflow_binding")
public class OaBusinessWorkflowBinding extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String businessType;

    private Long formId;

    private Long defaultOptionId;

    private String status;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
