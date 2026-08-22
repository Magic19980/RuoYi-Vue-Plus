package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 人工单 PDF 统计明细修改参数。
 */
@Data
public class WorkOrderDetailBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "明细主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "人工单主记录不能为空")
    private Long workOrderId;

    @Size(max = 100, message = "申请部门不能超过100个字符")
    private String requestDept;

    @Size(max = 150, message = "结算单位不能超过150个字符")
    private String settlementUnit;

    @Size(max = 100, message = "项目负责人不能超过100个字符")
    private String projectOwner;

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 150, message = "项目名称不能超过150个字符")
    private String projectName;

    @Size(max = 255, message = "项目特征不能超过255个字符")
    private String projectFeature;

    @Size(max = 30, message = "计量单位不能超过30个字符")
    private String unit;

    @Size(max = 50, message = "工程量不能超过50个字符")
    private String engineeringQuantity;

    @Size(max = 50, message = "中国人工不能超过50个字符")
    private String chineseLabor;

    @Size(max = 50, message = "印尼人工不能超过50个字符")
    private String indonesiaLabor;

    @Size(max = 150, message = "安装车间不能超过150个字符")
    private String installDepartment;

    @Size(max = 150, message = "安装班组不能超过150个字符")
    private String installTeam;

    private String workContent;
}
