package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentCommunityMedia;
import org.dromara.department.domain.vo.DepartmentCommunityMediaVo;

import java.util.List;

/**
 * 协作社区媒体附件数据层。
 */
@Mapper
public interface DepartmentCommunityMediaMapper extends BaseMapperPlus<DepartmentCommunityMedia, DepartmentCommunityMediaVo> {

    /**
     * 删除帖子当前有效的媒体关联记录，不删除 OSS 文件本身。
     * 媒体关联是可重建关系，使用物理删除可以避免逻辑删除记录参与唯一索引冲突。
     */
    @Delete("delete from dm_department_community_media where post_id = #{postId} and del_flag = '0'")
    int deleteActiveByPostId(@Param("postId") Long postId);

    @Select({
        "select id, post_id, oss_id, media_type, original_name as file_name, file_suffix, content_type,",
        "file_size, sort_num",
        "from dm_department_community_media",
        "where post_id = #{postId} and del_flag = '0'",
        "order by sort_num asc, id asc"
    })
    List<DepartmentCommunityMediaVo> selectListByPostId(@Param("postId") Long postId);
}
