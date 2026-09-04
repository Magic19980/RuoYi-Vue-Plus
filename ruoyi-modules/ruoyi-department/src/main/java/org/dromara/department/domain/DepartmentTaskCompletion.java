package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/** 任务实例对应的实际业务完成记录实体，对应 {@code dm_department_task_completion} 表。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_task_completion")
public class DepartmentTaskCompletion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 完成记录主键。 */
    private Long id;
    /** 任务实例主键。 */
    private Long instanceId;
    /** 被完成的业务任务类型。 */
    private String taskType;
    /** 被完成业务记录的主键。 */
    private Long sourceId;
    /** 实际完成时间。 */
    private LocalDateTime completedAt;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
