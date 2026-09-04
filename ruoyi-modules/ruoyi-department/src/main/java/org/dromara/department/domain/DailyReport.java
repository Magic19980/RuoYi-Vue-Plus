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
 * 科室日报实体，对应 {@code dm_daily_report} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_daily_report")
public class DailyReport extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 日报主键。 */
    private Long id;

    /** 日报日期。 */
    private LocalDate reportDate;

    /** 填报人用户主键。 */
    private Long userId;

    /** 业务科室主键。 */
    private Long deptId;

    /** 今日工作内容。 */
    private String todayWork;

    /** 明日工作计划。 */
    private String tomorrowPlan;

    /** 协调事项或需要协助的内容。 */
    private String coordinationNote;

    /** 日报状态。 */
    private String status;

    /** 日报来源，例如人工填写或休假自动生成。 */
    private String sourceType;

    /** 休假自动日报关联的休假记录。 */
    private Long leaveId;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
