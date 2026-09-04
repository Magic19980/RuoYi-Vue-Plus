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
import java.time.LocalDateTime;

/** 任务定义在某个成员、某个周期上的实例实体，对应 {@code dm_department_task_instance} 表。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_task_instance")
public class DepartmentTaskInstance extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 任务实例主键。 */
    private Long id;
    /** 周期任务规则主键。 */
    private Long ruleId;
    /** 业务科室主键。 */
    private Long deptId;
    /** 任务成员用户主键。 */
    private Long userId;
    /** 任务周期开始日期，包含当天。 */
    private LocalDate periodStart;
    /** 任务周期结束日期，包含当天。 */
    private LocalDate periodEnd;
    /** 任务截止时间。 */
    private LocalDateTime deadline;
    /** 周期内要求完成的次数。 */
    private Integer requiredCount;
    /** 周期内已完成的次数。 */
    private Integer completedCount;
    /** 任务实例状态。 */
    private String status;
    /** 实例生成时间。 */
    private LocalDateTime generatedAt;
    /** 实例完成时间。 */
    private LocalDateTime completedAt;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
