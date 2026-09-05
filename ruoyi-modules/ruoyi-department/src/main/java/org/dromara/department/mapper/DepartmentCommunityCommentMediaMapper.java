package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentCommunityCommentMedia;
import org.dromara.department.domain.vo.DepartmentCommunityMediaVo;

import java.util.List;

/**
 * 协作社区评论图片附件数据层。
 */
@Mapper
public interface DepartmentCommunityCommentMediaMapper extends BaseMapperPlus<DepartmentCommunityCommentMedia, DepartmentCommunityMediaVo> {

    @Select({
        "<script>",
        "select id, comment_id, oss_id, media_type, original_name as file_name, file_suffix, content_type,",
        "file_size, sort_num",
        "from dm_department_community_comment_media",
        "where del_flag = '0' and comment_id in",
        "<foreach collection='commentIds' item='commentId' open='(' separator=',' close=')'>#{commentId}</foreach>",
        "order by comment_id asc, sort_num asc, id asc",
        "</script>"
    })
    List<DepartmentCommunityMediaVo> selectListByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Delete("delete from dm_department_community_comment_media where comment_id = #{commentId} and del_flag = '0'")
    int deleteActiveByCommentId(@Param("commentId") Long commentId);

    @Delete({
        "delete from dm_department_community_comment_media",
        "where comment_id in (select id from dm_department_community_comment where post_id = #{postId})",
        "and del_flag = '0'"
    })
    int deleteActiveByPostId(@Param("postId") Long postId);
}
