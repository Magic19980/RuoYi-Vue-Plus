package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 平台问题与建议评论参数。
 */
@Data
public class PlatformFeedbackCommentBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论不能超过2000个字符")
    private String content;
}
