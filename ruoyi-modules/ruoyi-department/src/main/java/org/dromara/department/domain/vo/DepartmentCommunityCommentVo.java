package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 协作社区评论视图。
 */
@Data
public class DepartmentCommunityCommentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long postId;
    private Long parentId;
    private String content;
    private String authorName;
    private String deptName;
    private String status;
    private Boolean mine;
    private LocalDateTime createTime;
    private List<DepartmentCommunityMediaVo> mediaList;
}
