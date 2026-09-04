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
 * 业务科室配置实体，对应 {@code dm_department} 表。
 *
 * <p>科室主键直接复用 {@code sys_dept.dept_id}，科室名称和组织层级以系统部门主数据为准。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department")
public class DepartmentConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "dept_id", type = IdType.INPUT)
    /** 业务科室主键，同时为系统部门主键。 */
    private Long deptId;

    /** 配置状态。 */
    private String status;

    /** 科室负责人用户主键。 */
    private Long managerUserId;

    /** 显示排序号。 */
    private Integer sortNum;

    /** 配置备注。 */
    private String remark;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
