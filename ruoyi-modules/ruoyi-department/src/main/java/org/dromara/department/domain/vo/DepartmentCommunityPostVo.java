package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 协作社区帖子列表和详情视图。
 */
@Data
public class DepartmentCommunityPostVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content;
    private String postType;
    private String tags;
    private String visibility;
    private Long deptId;
    private String deptName;
    private String authorName;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Long acceptedCommentId;
    private Boolean liked;
    private Boolean favorited;
    private Boolean mine;

    private Integer mediaCount;

    private List<DepartmentCommunityMediaVo> mediaList;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
