package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 协作社区帖子新增、修改参数。
 */
@Data
public class DepartmentCommunityPostBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "帖子主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    @Size(max = 300, message = "副标题不能超过300个字符")
    private String subtitle;

    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容不能超过10000个字符")
    private String content;

    @Size(max = 20, message = "内容类型不能超过20个字符")
    private String postType;

    @Size(max = 500, message = "标签不能超过500个字符")
    private String tags;

    @Size(max = 1000, message = "媒体附件数量或参数长度超出限制")
    private String mediaOssIds;

    @Size(max = 20, message = "可见范围不能超过20个字符")
    private String visibility;

    @Size(max = 20, message = "帖子状态不能超过20个字符")
    private String status;
}
