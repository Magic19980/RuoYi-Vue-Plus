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

/** 科室周期任务规则实体，对应 {@code dm_department_task_rule} 表。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_task_rule")
public class DepartmentTaskRule extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 任务规则主键。 */
    private Long id;
    /** 业务科室主键。 */
    private Long deptId;
    /** 任务名称。 */
    private String taskName;
    /** 任务业务类型。 */
    private String taskType;
    /** 周期类型。 */
    private String cycleType;
    /** 每个周期要求完成的次数。 */
    private Integer requiredCount;
    /** 截止日期在周期内的日序号。 */
    private Integer deadlineDay;
    /** 每日截止时间。 */
    private LocalTime deadlineTime;
    /** 完成次数统计口径。 */
    private String countMode;
    /** 提醒时间距截止时间的小时数。 */
    private Integer remindHours;
    /** 规则生效开始日期，包含当天。 */
    private LocalDate effectiveStart;
    /** 规则生效结束日期，包含当天。 */
    private LocalDate effectiveEnd;
    /** 规则状态。 */
    private String status;
    /** 规则备注。 */
    private String remark;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
