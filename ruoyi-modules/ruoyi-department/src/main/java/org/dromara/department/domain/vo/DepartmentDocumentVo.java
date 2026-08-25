package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 科室资料列表和详情视图。
 */
@Data
public class DepartmentDocumentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long deptId;

    private Long projectId;

    private String projectName;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String description;

    private String tags;

    private String visibility;

    private String status;

    private LocalDate expireDate;

    private Long currentVersionId;

    private Integer versionNo;

    private Long currentOssId;

    private String currentOriginalName;

    private String currentFileSuffix;

    private Long currentFileSize;

    private String currentContentType;

    private String createByName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
