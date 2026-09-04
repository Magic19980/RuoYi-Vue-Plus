package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 科室负责的项目主数据实体，对应 {@code dm_department_project} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_project")
public class DepartmentProject extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 项目主键。 */
    private Long id;

    /** 业务科室主键。 */
    private Long deptId;

    /** 项目编码。 */
    private String projectCode;

    /** 项目名称。 */
    private String projectName;

    /** 项目类型。 */
    private String projectType;

    /** 项目负责人。 */
    private String responsiblePerson;

    /** 项目状态。 */
    private String status;

    /** 显示排序号。 */
    private Integer sortNum;

    /** 项目备注。 */
    private String remark;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
