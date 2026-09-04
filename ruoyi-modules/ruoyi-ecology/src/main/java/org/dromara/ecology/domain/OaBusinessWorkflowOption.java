package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 业务类型允许使用的审批方式。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_business_workflow_option")
public class OaBusinessWorkflowOption extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long bindingId;

    private Long optionId;

    private Integer sortNo;

    @TableLogic
    private String delFlag;
}
