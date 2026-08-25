package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 科室资料版本视图。
 */
@Data
public class DepartmentDocumentVersionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long documentId;

    private Integer versionNo;

    private Long ossId;

    private String originalName;

    private String fileSuffix;

    private Long fileSize;

    private String contentType;

    private String versionNote;

    private String createByName;

    private LocalDateTime createTime;
}
