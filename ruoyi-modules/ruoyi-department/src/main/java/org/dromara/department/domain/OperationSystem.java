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
 * 系统在线率台账对象 dm_operation_system。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_operation_system")
public class OperationSystem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    private Long projectId;

    private LocalDate statDate;

    private String systemName;

    private String responsiblePerson;

    private String serverName;

    private String serverIp;

    private BigDecimal onlineDays;

    private Integer downtimeMinutes;

    private BigDecimal onlineRate;

    private String remark;

    private String sourceType;

    private String sourceFileName;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
