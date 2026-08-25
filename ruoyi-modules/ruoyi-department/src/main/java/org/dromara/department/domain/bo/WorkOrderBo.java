package org.dromara.department.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 工单台账新增、修改参数。
 */
@Data
public class WorkOrderBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;

    private LocalDate occurDate;

    private LocalDate sourcePeriodStart;

    private LocalDate sourcePeriodEnd;

    @Size(max = 100, message = "工单编号不能超过100个字符")
    private String ticketNo;

    @Size(max = 100, message = "申请部门不能超过100个字符")
    private String requestDept;

    @Size(max = 150, message = "结算单位不能超过150个字符")
    private String settlementUnit;

    @Size(max = 100, message = "项目负责人不能超过100个字符")
    private String projectOwner;

    @NotBlank(message = "项目名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 150, message = "项目名称不能超过150个字符")
    private String systemName;

    @Size(max = 150, message = "安装车间不能超过150个字符")
    private String installDepartment;

    @Size(max = 150, message = "安装班组不能超过150个字符")
    private String installTeam;

    @Size(max = 100, message = "工作类别不能超过100个字符")
    private String workCategory;

    @Size(max = 100, message = "故障类型不能超过100个字符")
    private String faultType;

    @Size(max = 255, message = "项目特征不能超过255个字符")
    private String title;

    private String workContent;

    @Size(max = 30, message = "计量单位不能超过30个字符")
    private String unit;

    @NotNull(message = "工单量不能为空", groups = {AddGroup.class, EditGroup.class})
    @DecimalMin(value = "0", inclusive = false, message = "工单量必须大于0")
    private BigDecimal quantity;

    @Size(max = 100, message = "责任人不能超过100个字符")
    private String responsiblePerson;

    @Size(max = 100, message = "处理人不能超过100个字符")
    private String handler;

    private Integer resolutionMinutes;

    @Size(max = 50, message = "反馈渠道不能超过50个字符")
    private String feedbackChannel;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;
}
