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
 * 工单台账对象 dm_work_order。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_work_order")
public class WorkOrder extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    private String ticketNo;

    private LocalDate occurDate;

    private LocalDate sourcePeriodStart;

    private LocalDate sourcePeriodEnd;

    private String requestDept;

    private String settlementUnit;

    private String projectOwner;

    private String systemName;

    private String installDepartment;

    private String installTeam;

    private String workCategory;

    private String faultType;

    private String title;

    private String workContent;

    private String unit;

    private BigDecimal quantity;

    private String responsiblePerson;

    private String handler;

    private Integer resolutionMinutes;

    private String feedbackChannel;

    private String sourceType;

    private Long sourceBatchId;

    private String sourceFileName;

    private Integer sourcePage;

    private BigDecimal parseConfidence;

    private String parseMessage;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
