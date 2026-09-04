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
 * 工单台账实体，对应 {@code dm_work_order} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_work_order")
public class WorkOrder extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 工单主键。 */
    private Long id;

    /** 业务科室主键。 */
    private Long deptId;

    /** 工单编号。 */
    private String ticketNo;

    /** 工单发生日期。 */
    private LocalDate occurDate;

    /** 来源数据统计周期开始日期。 */
    private LocalDate sourcePeriodStart;

    /** 来源数据统计周期结束日期。 */
    private LocalDate sourcePeriodEnd;

    /** 请求部门。 */
    private String requestDept;

    /** 结算单位。 */
    private String settlementUnit;

    /** 项目负责人。 */
    private String projectOwner;

    /** 相关系统名称。 */
    private String systemName;

    /** 安装部门。 */
    private String installDepartment;

    /** 安装班组。 */
    private String installTeam;

    /** 工单业务类别。 */
    private String workCategory;

    /** 故障类型。 */
    private String faultType;

    /** 工单标题。 */
    private String title;

    /** 工单工作内容。 */
    private String workContent;

    /** 计量单位。 */
    private String unit;

    /** 工作数量。 */
    private BigDecimal quantity;

    /** 责任人。 */
    private String responsiblePerson;

    /** 实际处理人。 */
    private String handler;

    /** 解决时长，单位为分钟。 */
    private Integer resolutionMinutes;

    /** 反馈渠道。 */
    private String feedbackChannel;

    /** 数据来源。 */
    private String sourceType;

    /** 来源导入批次主键。 */
    private Long sourceBatchId;

    /** 来源文件名称。 */
    private String sourceFileName;

    /** 来源文件页码。 */
    private Integer sourcePage;

    /** PDF解析置信度。 */
    private BigDecimal parseConfidence;

    /** PDF解析说明或异常信息。 */
    private String parseMessage;

    /** 工单备注。 */
    private String remark;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
