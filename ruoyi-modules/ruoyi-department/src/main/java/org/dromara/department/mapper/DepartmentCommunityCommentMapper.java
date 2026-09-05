package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentCommunityComment;
import org.dromara.department.domain.vo.DepartmentCommunityCommentVo;

import java.util.List;

/**
 * 协作社区评论数据层。
 */
@Mapper
public interface DepartmentCommunityCommentMapper extends BaseMapperPlus<DepartmentCommunityComment, DepartmentCommunityCommentVo> {

    @Select({
        "select c.id, c.post_id, c.parent_id, c.content, c.status,",
        "coalesce(u.nick_name, u.user_name) as author_name, d.dept_name,",
        "case when c.create_by = #{userId} then 1 else 0 end as mine, c.create_time",
        "from dm_department_community_comment c",
        "left join sys_user u on u.user_id = c.create_by and u.del_flag = '0'",
        "left join sys_dept d on d.dept_id = c.create_dept and d.del_flag = '0'",
        "where c.post_id = #{postId} and c.del_flag = '0' and c.status = 'ENABLED'",
        "order by c.create_time asc, c.id asc"
    })
    List<DepartmentCommunityCommentVo> selectListByPostId(@Param("postId") Long postId, @Param("userId") Long userId);
}
