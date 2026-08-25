package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.time.LocalDate;

/**
 * 科室资料新增、编辑参数。
 */
@Data
public class DepartmentDocumentBo {

    @NotNull(message = "资料主键不能为空", groups = EditGroup.class)
    private Long id;

    private Long projectId;

    @NotNull(message = "资料分类不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long categoryId;

    @NotBlank(message = "资料标题不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 200, message = "资料标题不能超过200个字符", groups = {AddGroup.class, EditGroup.class})
    private String title;

    @Size(max = 1000, message = "资料说明不能超过1000个字符", groups = {AddGroup.class, EditGroup.class})
    private String description;

    @Size(max = 500, message = "资料标签不能超过500个字符", groups = {AddGroup.class, EditGroup.class})
    private String tags;

    private String visibility;

    private String status;

    private LocalDate expireDate;
}
