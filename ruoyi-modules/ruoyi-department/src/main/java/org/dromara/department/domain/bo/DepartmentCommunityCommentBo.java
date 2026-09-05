package org.dromara.department.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 协作社区评论参数。
 */
@Data
public class DepartmentCommunityCommentBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long parentId;

    @Size(max = 2000, message = "评论不能超过2000个字符")
    private String content;

    /**
     * 评论图片对应的 OSS 文件 ID，使用逗号分隔。
     * 评论正文和图片至少需要提交一项，具体规则由业务层统一校验。
     */
    private String mediaOssIds;
}
