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

/** 周期任务成员分配。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_task_assignment")
public class DepartmentTaskAssignment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long ruleId;
    private Long deptId;
    private Long userId;
    private LocalDate effectiveStart;
    private LocalDate effectiveEnd;
    /** 日报任务个人工作日，使用 ISO 星期编号 1-7 逗号分隔。 */
    private String workDays;
    /** 日报任务提醒时间；非日报任务为空。 */
    private java.time.LocalTime reminderTime;
    private String status;
    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
