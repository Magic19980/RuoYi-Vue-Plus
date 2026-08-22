package org.dromara.department.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 5WHY分析新增、修改参数。 */
@Data
public class FiveWhyBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;

    @Size(max = 255, message = "公司/部门信息不能超过255个字符")
    private String companyDept;

    @Size(max = 64, message = "工号不能超过64个字符")
    private String employeeNo;

    @NotBlank(message = "分析人不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 100, message = "分析人不能超过100个字符")
    private String analystName;

    @NotNull(message = "分析日期不能为空", groups = {AddGroup.class, EditGroup.class})
    private LocalDate analysisDate;

    @NotBlank(message = "问题名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 255, message = "问题名称不能超过255个字符")
    private String problemName;

    private String problemDescription;
    private String impactScope;

    @Valid
    @Size(min = 5, max = 5, message = "必须填写1至5共5层WHY分析")
    private List<FiveWhyWhyBo> whys = new ArrayList<>();

    @Valid
    private List<FiveWhyImprovementBo> improvements = new ArrayList<>();

    private Long beforeOssId;
    private Long afterOssId;
    private String effectVerification;
    private String standardizationPlan;
    private String standardizationExecution;
    private String reviewStatus;
    private String reviewComment;
}
