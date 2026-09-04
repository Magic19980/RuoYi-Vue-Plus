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

/**
 * 科室人员休假安排实体，对应 {@code dm_daily_leave} 表。
 *
 * <p>休假记录用于计算日报任务和日历状态，不直接删除历史业务数据。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_daily_leave")
public class DailyLeave extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 休假记录主键。 */
    private Long id;

    /** 业务科室主键。 */
    private Long deptId;

    /** 休假人员用户主键。 */
    private Long userId;

    /** 休假开始日期，包含当天。 */
    private LocalDate startDate;

    /** 休假结束日期，包含当天。 */
    private LocalDate endDate;

    /** 休假类型字典值。 */
    private String leaveType;

    /** 休假原因或说明。 */
    private String reason;

    /** 休假状态。 */
    private String status;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
