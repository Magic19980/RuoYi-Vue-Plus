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

/** 周期任务成员分配实体，对应 {@code dm_department_task_assignment} 表。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_task_assignment")
public class DepartmentTaskAssignment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 成员分配主键。 */
    private Long id;
    /** 周期任务规则主键。 */
    private Long ruleId;
    /** 业务科室主键。 */
    private Long deptId;
    /** 被分配人员用户主键。 */
    private Long userId;
    /** 分配生效开始日期，包含当天。 */
    private LocalDate effectiveStart;
    /** 分配生效结束日期，包含当天。 */
    private LocalDate effectiveEnd;
    /** 日报任务个人工作日，使用 ISO 星期编号 1-7 逗号分隔。 */
    private String workDays;
    /** 日报任务提醒时间；非日报任务为空。 */
    private java.time.LocalTime reminderTime;
    /** 成员分配状态。 */
    private String status;
    /** 分配备注。 */
    private String remark;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
