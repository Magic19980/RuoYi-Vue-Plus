package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** OA 申请涉及的部门明细。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_application_dept")
public class OaApplicationDept extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long applicationId;
    private Long deptId;
    private String deptName;
    private Integer sortNo;

    @TableLogic
    private String delFlag;
}
