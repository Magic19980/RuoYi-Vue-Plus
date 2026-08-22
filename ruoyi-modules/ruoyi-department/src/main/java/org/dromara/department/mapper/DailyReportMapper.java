package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DailyReport;
import org.dromara.department.domain.bo.DailyReportQueryBo;
import org.dromara.department.domain.vo.DailyReportVo;

/**
 * 日报数据层。
 */
@Mapper
public interface DailyReportMapper extends BaseMapperPlus<DailyReport, DailyReportVo> {

    @Select({
        "<script>",
        "select r.id, r.report_date, r.user_id, r.dept_id, r.today_work, r.tomorrow_plan,",
        "r.coordination_note, r.status, r.source_type, r.leave_id,",
        "u.user_name, u.nick_name, d.dept_name",
        "from dm_daily_report r",
        "left join sys_user u on u.user_id = r.user_id and u.del_flag = '0'",
        "left join sys_dept d on d.dept_id = r.dept_id and d.del_flag = '0'",
        "where r.del_flag = '0'",
        "<if test='bo.id != null'> and r.id = #{bo.id} </if>",
        "<if test='bo.reportDate != null'> and r.report_date = #{bo.reportDate} </if>",
        "<if test='bo.beginDate != null'> and r.report_date &gt;= #{bo.beginDate} </if>",
        "<if test='bo.endDate != null'> and r.report_date &lt;= #{bo.endDate} </if>",
        "<if test='bo.status != null and bo.status != \"\"'> and r.status = #{bo.status} </if>",
        "<if test='bo.userId != null and all == true'> and r.user_id = #{bo.userId} </if>",
        "<if test='bo.userName != null and bo.userName != \"\"'> and (u.user_name like concat('%', #{bo.userName}, '%') or u.nick_name like concat('%', #{bo.userName}, '%')) </if>",
        "<choose>",
        "<when test='all == true'>",
        "</when>",
        "<when test='deptId != null'> and r.dept_id = #{deptId} </when>",
        "<otherwise> and r.user_id = #{userId} </otherwise>",
        "</choose>",
        "order by r.report_date desc, r.id desc",
        "</script>"
    })
    Page<DailyReportVo> selectPageList(Page<DailyReportVo> page,
                                        @Param("bo") DailyReportQueryBo bo,
                                        @Param("userId") Long userId,
                                        @Param("deptId") Long deptId,
                                        @Param("all") boolean all);

    @Select("select user_id from sys_user where del_flag = '0' and (user_name = #{name} or nick_name = #{name}) limit 1")
    Long selectUserIdByName(@Param("name") String name);

    @Select("select dept_id from sys_user where del_flag = '0' and user_id = #{userId}")
    Long selectDeptIdByUserId(@Param("userId") Long userId);
}
