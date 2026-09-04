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

/** 泛微审批业务类型配置。业务类型标识用于审批方案匹配，名称仅用于页面展示。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_business_type")
public class OaBusinessType extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 稳定的业务类型标识，例如 EXPENSE_REIMBURSEMENT。 */
    private String businessType;

    /** 页面展示名称。 */
    private String businessName;

    private String status;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
