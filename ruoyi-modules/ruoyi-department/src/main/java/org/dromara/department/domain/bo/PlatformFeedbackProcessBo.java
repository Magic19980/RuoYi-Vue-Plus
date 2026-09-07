package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 平台问题与建议处理参数。
 */
@Data
public class PlatformFeedbackProcessBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "反馈主键不能为空")
    private Long id;

    @Size(max = 20, message = "状态不能超过20个字符")
    private String status;

    @Size(max = 2000, message = "处理说明不能超过2000个字符")
    private String note;
}
