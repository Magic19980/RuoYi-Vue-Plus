package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentCommunityReport;
import org.dromara.department.domain.bo.DepartmentCommunityReportQueryBo;
import org.dromara.department.domain.vo.DepartmentCommunityReportVo;

/**
 * 协作社区举报数据层。
 */
@Mapper
public interface DepartmentCommunityReportMapper extends BaseMapperPlus<DepartmentCommunityReport, DepartmentCommunityReportVo> {

    @Select({
        "<script>",
        "select r.id, r.post_id, p.title as post_title,",
        "coalesce(u.nick_name, u.user_name) as reporter_name,",
        "r.reason, r.status, coalesce(h.nick_name, h.user_name) as handled_by_name,",
        "r.handle_note, r.create_time, r.handled_at",
        "from dm_department_community_report r",
        "left join dm_department_community_post p on p.id = r.post_id",
        "left join sys_user u on u.user_id = r.reporter_user_id and u.del_flag = '0'",
        "left join sys_user h on h.user_id = r.handled_by and h.del_flag = '0'",
        "where r.del_flag = '0'",
        "<if test='bo.status != null and bo.status != &quot;&quot;'> and r.status = #{bo.status} </if>",
        "order by case when r.status = 'PENDING' then 0 else 1 end, r.create_time desc, r.id desc",
        "</script>"
    })
    Page<DepartmentCommunityReportVo> selectPageList(Page<DepartmentCommunityReportVo> page,
                                                       @Param("bo") DepartmentCommunityReportQueryBo bo);
}
