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
 * 科室负责的项目主数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_project")
public class DepartmentProject extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    private String projectCode;

    private String projectName;

    private String projectType;

    private String responsiblePerson;

    private String status;

    private Integer sortNum;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
