package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 5WHY审核参数。 */
@Data
public class FiveWhyReviewBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空")
    private Long id;

    @NotBlank(message = "审核结果不能为空")
    private String reviewStatus;

    @Size(max = 1000, message = "审核意见不能超过1000个字符")
    private String reviewComment;
}
