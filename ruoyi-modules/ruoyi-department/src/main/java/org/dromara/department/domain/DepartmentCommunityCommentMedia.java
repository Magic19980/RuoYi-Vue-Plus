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
 * 协作社区评论图片附件实体。
 *
 * <p>评论附件与帖子媒体分表，避免复用帖子媒体的 post_id 造成归属混淆。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_community_comment_media")
public class DepartmentCommunityCommentMedia extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long commentId;

    private Long ossId;

    private String mediaType;

    private String originalName;

    private String fileSuffix;

    private String contentType;

    private Long fileSize;

    private Integer sortNum;

    @TableLogic
    private String delFlag;
}
