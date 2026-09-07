package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 平台问题与建议新增、修改参数。
 */
@Data
public class PlatformFeedbackBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "反馈主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "反馈类型不能为空")
    @Size(max = 30, message = "反馈类型不能超过30个字符")
    private String feedbackType;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    @NotBlank(message = "描述不能为空")
    @Size(max = 10000, message = "描述不能超过10000个字符")
    private String description;

    @Size(max = 100, message = "模块名称不能超过100个字符")
    private String moduleName;

    @Size(max = 200, message = "页面标题不能超过200个字符")
    private String pageTitle;

    @Size(max = 500, message = "页面地址不能超过500个字符")
    private String pagePath;

    @Size(max = 100, message = "业务编号不能超过100个字符")
    private String businessRef;

    @Size(max = 5000, message = "复现步骤不能超过5000个字符")
    private String reproduceSteps;

    @Size(max = 2000, message = "期望结果不能超过2000个字符")
    private String expectedResult;

    @Size(max = 2000, message = "实际结果不能超过2000个字符")
    private String actualResult;

    @Size(max = 20, message = "影响范围不能超过20个字符")
    private String impactScope;

    @Size(max = 20, message = "优先级不能超过20个字符")
    private String priority;

    @Size(max = 2000, message = "附件参数长度超出限制")
    private String attachmentOssIds;
}
