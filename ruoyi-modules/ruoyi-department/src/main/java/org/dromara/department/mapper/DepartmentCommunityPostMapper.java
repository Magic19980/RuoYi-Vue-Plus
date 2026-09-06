package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentCommunityPost;
import org.dromara.department.domain.bo.DepartmentCommunityPostQueryBo;
import org.dromara.department.domain.vo.DepartmentCommunityPostVo;

/**
 * 协作社区帖子数据层。
 */
@Mapper
public interface DepartmentCommunityPostMapper extends BaseMapperPlus<DepartmentCommunityPost, DepartmentCommunityPostVo> {

    @Select({
        "<script>",
        "select p.id, p.title, p.subtitle, p.content, p.post_type, p.tags, p.visibility, p.dept_id,",
        "d.dept_name, coalesce(u.nick_name, u.user_name) as author_name, p.status,",
        "p.view_count, p.like_count, p.comment_count, p.favorite_count, p.accepted_comment_id,",
        "case when exists (select 1 from dm_department_community_reaction r where r.post_id = p.id and r.user_id = #{userId} and r.reaction_type = 'LIKE' and r.del_flag = '0') then 1 else 0 end as liked,",
        "case when exists (select 1 from dm_department_community_reaction r where r.post_id = p.id and r.user_id = #{userId} and r.reaction_type = 'FAVORITE' and r.del_flag = '0') then 1 else 0 end as favorited,",
        "case when p.create_by = #{userId} then 1 else 0 end as mine,",
        "p.create_time, p.update_time",
        "from dm_department_community_post p",
        "left join sys_user u on u.user_id = p.create_by and u.del_flag = '0'",
        "left join sys_dept d on d.dept_id = p.dept_id and d.del_flag = '0'",
        "where p.del_flag = '0'",
        "and (",
        "  (p.status in ('PUBLISHED', 'RESOLVED') and (p.visibility = 'ALL' or (p.visibility = 'DEPT' and p.dept_id = #{deptId})))",
        "<if test='bo.feed == &quot;MINE&quot;'>",
        "  or p.create_by = #{userId}",
        "</if>",
        ")",
        "<if test='bo.keyword != null and bo.keyword != &quot;&quot;'>",
        "and (p.title like concat('%', #{bo.keyword}, '%') or p.subtitle like concat('%', #{bo.keyword}, '%') or p.content like concat('%', #{bo.keyword}, '%') or p.tags like concat('%', #{bo.keyword}, '%'))",
        "</if>",
        "<if test='bo.postType != null and bo.postType != &quot;&quot;'> and p.post_type = #{bo.postType} </if>",
        "<if test='bo.tag != null and bo.tag != &quot;&quot;'> and p.tags like concat('%', #{bo.tag}, '%') </if>",
        "<if test='bo.feed == &quot;MINE&quot;'> and p.create_by = #{userId} </if>",
        "<choose>",
        "  <when test='bo.feed == &quot;HOT&quot;'> order by (p.like_count + p.comment_count * 2 + p.favorite_count + p.view_count / 10) desc, p.create_time desc, p.id desc </when>",
        "  <otherwise> order by p.create_time desc, p.id desc </otherwise>",
        "</choose>",
        "</script>"
    })
    Page<DepartmentCommunityPostVo> selectPageList(Page<DepartmentCommunityPostVo> page,
                                                    @Param("bo") DepartmentCommunityPostQueryBo bo,
                                                    @Param("userId") Long userId,
                                                    @Param("deptId") Long deptId);

    @Select({
        "select p.id, p.title, p.subtitle, p.content, p.post_type, p.tags, p.visibility, p.dept_id,",
        "d.dept_name, coalesce(u.nick_name, u.user_name) as author_name, p.status,",
        "p.view_count, p.like_count, p.comment_count, p.favorite_count, p.accepted_comment_id,",
        "case when exists (select 1 from dm_department_community_reaction r where r.post_id = p.id and r.user_id = #{userId} and r.reaction_type = 'LIKE' and r.del_flag = '0') then 1 else 0 end as liked,",
        "case when exists (select 1 from dm_department_community_reaction r where r.post_id = p.id and r.user_id = #{userId} and r.reaction_type = 'FAVORITE' and r.del_flag = '0') then 1 else 0 end as favorited,",
        "case when p.create_by = #{userId} then 1 else 0 end as mine,",
        "p.create_time, p.update_time",
        "from dm_department_community_post p",
        "left join sys_user u on u.user_id = p.create_by and u.del_flag = '0'",
        "left join sys_dept d on d.dept_id = p.dept_id and d.del_flag = '0'",
        "where p.id = #{id} and p.del_flag = '0'",
        "and (",
        "  (p.status in ('PUBLISHED', 'RESOLVED') and (p.visibility = 'ALL' or (p.visibility = 'DEPT' and p.dept_id = #{deptId})))",
        "  or p.create_by = #{userId}",
        ")"
    })
    DepartmentCommunityPostVo selectDetailById(@Param("id") Long id,
                                                @Param("userId") Long userId,
                                                @Param("deptId") Long deptId);

    @Update("update dm_department_community_post set view_count = coalesce(view_count, 0) + 1 where id = #{id} and del_flag = '0'")
    int incrementViewCount(@Param("id") Long id);

    @Update({
        "update dm_department_community_post p set",
        "p.like_count = (select count(1) from dm_department_community_reaction r where r.post_id = p.id and r.reaction_type = 'LIKE' and r.del_flag = '0'),",
        "p.favorite_count = (select count(1) from dm_department_community_reaction r where r.post_id = p.id and r.reaction_type = 'FAVORITE' and r.del_flag = '0'),",
        "p.comment_count = (select count(1) from dm_department_community_comment c where c.post_id = p.id and c.del_flag = '0' and c.status = 'ENABLED'),",
        "p.update_time = now() where p.id = #{postId} and p.del_flag = '0'"
    })
    int refreshCounts(@Param("postId") Long postId);
}
