package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/** SCORE提案新增、修改参数。 */
@Data
public class ScoreProposalBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;

    @Size(max = 255, message = "企业名称不能超过255个字符")
    private String companyName;

    private String teamMembers;

    @Size(max = 64, message = "工号不能超过64个字符")
    private String employeeNo;

    @NotBlank(message = "提议者姓名不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 100, message = "提议者姓名不能超过100个字符")
    private String proposerName;

    @Size(max = 50, message = "职位层级不能超过50个字符")
    private String proposerLevel;

    @Size(max = 150, message = "部门不能超过150个字符")
    private String deptName;

    private Long mainCategoryId;

    private Long subCategoryId;

    @NotNull(message = "提议人不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long proposerUserId;

    @Size(max = 255, message = "提案大类不能超过255个字符")
    private String mainCategory;

    @Size(max = 500, message = "提案小类不能超过500个字符")
    private String subCategory;

    private String problemDescription;
    private String improvementMeasure;
    private String implementerSupervisor;
    private List<Long> implementerUserIds;
    private Long beforeOssId;
    private Long afterOssId;
    private LocalDate startDate;
    private LocalDate plannedCompletionDate;
    private LocalDate actualCompletionDate;
    private String completionStatus;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;

    private String reviewStatus;
    private String reviewComment;
}
