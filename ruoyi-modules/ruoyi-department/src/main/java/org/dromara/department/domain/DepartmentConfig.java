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

/** 业务科室配置。科室主键直接复用 sys_dept.dept_id。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department")
public class DepartmentConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "dept_id", type = IdType.INPUT)
    private Long deptId;

    private String status;

    private Long managerUserId;

    private Integer sortNum;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
