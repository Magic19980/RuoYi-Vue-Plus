package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 通用导入部门别名映射。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_import_dept_alias")
public class OaImportDeptAlias extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String businessType;

    private String sourceDeptName;

    private String normalizedName;

    private Long deptId;

    private String targetDeptName;

    private String status;

    @TableLogic
    private String delFlag;
}
