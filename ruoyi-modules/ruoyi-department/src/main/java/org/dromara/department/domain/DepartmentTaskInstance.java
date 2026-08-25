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

/** 任务定义在某个成员、某个周期上的实例。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_task_instance")
public class DepartmentTaskInstance extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long ruleId;
    private Long deptId;
    private Long userId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime deadline;
    private Integer requiredCount;
    private Integer completedCount;
    private String status;
    private LocalDateTime generatedAt;
    private LocalDateTime completedAt;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
