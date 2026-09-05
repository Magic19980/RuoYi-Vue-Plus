package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 协作社区互动结果。
 */
@Data
public class DepartmentCommunityReactionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Boolean liked;
    private Boolean favorited;
    private Integer likeCount;
    private Integer favoriteCount;
}
