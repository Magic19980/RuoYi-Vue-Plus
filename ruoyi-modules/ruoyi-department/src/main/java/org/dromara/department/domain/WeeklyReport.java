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
 * 科室周报快照实体，对应 {@code dm_weekly_report} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_weekly_report")
public class WeeklyReport extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 周报快照主键。 */
    private Long id;

    /** 周报周期开始日期，包含当天。 */
    private LocalDate weekStart;

    /** 周报周期结束日期，包含当天。 */
    private LocalDate weekEnd;

    /** 周报标题。 */
    private String title;

    /** 周期内已生成日报数量。 */
    private Integer reportCount;

    /** 周期内应填报人员数量。 */
    private Integer requiredUserCount;

    /** 周期内已填报人员数量。 */
    private Integer filledUserCount;

    /** 周期内缺报人员数量。 */
    private Integer missingUserCount;

    /** 周报快照状态。 */
    private String status;

    /** 周报汇总快照JSON。 */
    private String snapshotJson;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
