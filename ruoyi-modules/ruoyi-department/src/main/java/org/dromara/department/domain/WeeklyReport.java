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
 * 科室周报快照对象 dm_weekly_report。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_weekly_report")
public class WeeklyReport extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private LocalDate weekStart;

    private LocalDate weekEnd;

    private String title;

    private Integer reportCount;

    private Integer requiredUserCount;

    private Integer filledUserCount;

    private Integer missingUserCount;

    private String status;

    private String snapshotJson;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
