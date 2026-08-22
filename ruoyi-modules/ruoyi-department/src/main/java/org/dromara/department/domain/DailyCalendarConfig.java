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

/** 科室日报周工作日规则。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_daily_calendar_config")
public class DailyCalendarConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    /** 个人工作日规则所属人员；为空时表示历史科室默认规则。 */
    private Long userId;

    private String workDays;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
