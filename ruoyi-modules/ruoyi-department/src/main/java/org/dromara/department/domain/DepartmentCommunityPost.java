package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 协作社区帖子实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_community_post")
public class DepartmentCommunityPost extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    private String content;

    private String postType;

    private String tags;

    private String visibility;

    private Long deptId;

    private String status;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer favoriteCount;

    private Long acceptedCommentId;

    @TableLogic
    private String delFlag;
}
