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

/** 科室日报日期例外规则。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_daily_calendar_override")
public class DailyCalendarOverride extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    /** 目标人员；为空表示全科室生效。 */
    private Long userId;

    private LocalDate calendarDate;

    /** 日期例外类型字典值（dm_date_exception）。 */
    private String dayType;

    /** 是否需要填写日报（true 是，false 否）。 */
    private Boolean needReport;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
