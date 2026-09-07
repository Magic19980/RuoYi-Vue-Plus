package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 平台问题与建议处理人配置参数。
 */
@Data
public class PlatformFeedbackHandlerConfigBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "请配置反馈处理人")
    @Size(min = 2, max = 3, message = "反馈处理人必须配置2至3人")
    private List<Long> userIds;
}
