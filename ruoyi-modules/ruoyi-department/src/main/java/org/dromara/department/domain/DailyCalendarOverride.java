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
 * 科室日报日期例外规则实体，对应 {@code dm_daily_calendar_override} 表。
 *
 * <p>用于覆盖默认工作日历，可按全科室或指定人员配置。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_daily_calendar_override")
public class DailyCalendarOverride extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 例外规则主键。 */
    private Long id;

    /** 业务科室主键。 */
    private Long deptId;

    /** 目标人员；为空表示全科室生效。 */
    private Long userId;

    /** 例外日期。 */
    private LocalDate calendarDate;

    /** 例外类型字典值，例如工作日或休息日。 */
    private String dayType;

    /** 是否要求填写日报。 */
    private Boolean needReport;

    /** 规则备注。 */
    private String remark;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
