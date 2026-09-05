package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 协作社区媒体附件视图。
 */
@Data
public class DepartmentCommunityMediaVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long postId;
    private Long commentId;
    private Long ossId;
    private String mediaType;
    private String fileName;
    private String fileSuffix;
    private String contentType;
    private Long fileSize;
    private Integer sortNum;
    private String previewUrl;
}
