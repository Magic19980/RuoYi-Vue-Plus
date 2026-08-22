package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/** SCORE 提案分类新增、修改参数。 */
@Data
public class ScoreCategoryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "分类主键不能为空", groups = EditGroup.class)
    private Long id;

    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 500, message = "分类名称不能超过500个字符")
    private String categoryName;

    private Integer sortNum;

    private String status;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
