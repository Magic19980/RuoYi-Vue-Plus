package org.dromara.department.domain.bo;

import lombok.Data;

/**
 * 协作社区帖子查询参数。
 */
@Data
public class DepartmentCommunityPostQueryBo {

    private String keyword;

    private String postType;

    private String tag;

    private String feed;
}
