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

/** 任务实例对应的实际业务完成记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_task_completion")
public class DepartmentTaskCompletion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long instanceId;
    private String taskType;
    private Long sourceId;
    private LocalDateTime completedAt;

    @TableLogic
    private String delFlag;
}
