package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 资料视频预览信息。
 */
@Data
public class DepartmentDocumentVideoPreviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long documentId;

    private Long versionId;

    private String fileName;

    private String contentType;

    private Long fileSize;

    private String playbackUrl;
}
