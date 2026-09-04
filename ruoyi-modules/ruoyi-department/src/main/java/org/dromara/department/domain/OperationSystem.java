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
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 系统在线率台账实体，对应 {@code dm_operation_system} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_operation_system")
public class OperationSystem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 系统在线率记录主键。 */
    private Long id;

    /** 业务科室主键。 */
    private Long deptId;

    /** 关联项目主键。 */
    private Long projectId;

    /** 统计日期。 */
    private LocalDate statDate;

    /** 系统名称。 */
    private String systemName;

    /** 系统负责人。 */
    private String responsiblePerson;

    /** 在线天数。 */
    private BigDecimal onlineDays;

    /** 停机时长，单位为分钟。 */
    private Integer downtimeMinutes;

    /** 在线率。 */
    private BigDecimal onlineRate;

    /** 记录备注。 */
    private String remark;

    /** 数据来源。 */
    private String sourceType;

    /** 来源文件名称。 */
    private String sourceFileName;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
