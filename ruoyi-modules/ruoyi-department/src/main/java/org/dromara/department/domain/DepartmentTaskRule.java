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
import java.time.LocalDate;
import java.time.LocalTime;

/** 科室周期任务规则。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_task_rule")
public class DepartmentTaskRule extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long deptId;
    private String taskName;
    private String taskType;
    private String cycleType;
    private Integer requiredCount;
    private Integer deadlineDay;
    private LocalTime deadlineTime;
    private String countMode;
    private Integer remindHours;
    private LocalDate effectiveStart;
    private LocalDate effectiveEnd;
    private String status;
    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
