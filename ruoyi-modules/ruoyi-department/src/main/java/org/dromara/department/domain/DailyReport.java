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
 * 科室日报对象 dm_daily_report。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_daily_report")
public class DailyReport extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private LocalDate reportDate;

    private Long userId;

    private Long deptId;

    private String todayWork;

    private String tomorrowPlan;

    private String coordinationNote;

    private String status;

    private String sourceType;

    /** 休假自动日报关联的休假记录。 */
    private Long leaveId;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
