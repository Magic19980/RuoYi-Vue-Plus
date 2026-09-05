package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 协作社区举报视图。
 */
@Data
public class DepartmentCommunityReportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long postId;
    private String postTitle;
    private String reporterName;
    private String reason;
    private String status;
    private String handledByName;
    private String handleNote;
    private LocalDateTime createTime;
    private LocalDateTime handledAt;
}
