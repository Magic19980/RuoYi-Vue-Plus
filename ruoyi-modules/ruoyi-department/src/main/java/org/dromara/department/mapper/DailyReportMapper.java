package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DailyReport;
import org.dromara.department.domain.bo.DailyReportQueryBo;
import org.dromara.department.domain.vo.DailyReportVo;

import java.time.LocalDate;

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

    /** 查询用户实际纳入日报的科室，未建人员档案时回退到系统用户主部门。 */
    @Select("select coalesce((select p.create_dept from dm_person_profile p where p.user_id = u.user_id and p.del_flag = '0' and p.member_status = 'ACTIVE' "
        + "and p.join_date <= current_date and (p.leave_date is null or p.leave_date > current_date) order by p.join_date desc, p.id desc limit 1), u.dept_id) "
        + "from sys_user u where u.del_flag = '0' and u.user_id = #{userId}")
    Long selectDailyReportDeptIdByUserId(@Param("userId") Long userId);

    @Select("select count(1) from dm_person_profile p join sys_user u on u.user_id = p.user_id and u.del_flag = '0' and u.status = '0' "
        + "where p.user_id = #{userId} and p.create_dept = #{deptId} and p.del_flag = '0' and p.member_status = 'ACTIVE' "
        + "and p.join_date <= current_date and (p.leave_date is null or p.leave_date > current_date)")
    long countMemberInDept(@Param("userId") Long userId, @Param("deptId") Long deptId);

    @Select("select count(1) from dm_person_profile p join sys_user u on u.user_id = p.user_id and u.del_flag = '0' and u.status = '0' "
        + "where p.user_id = #{userId} and p.create_dept = #{deptId} and p.del_flag = '0' "
        + "and p.join_date <= #{date} and (p.leave_date is null or p.leave_date > #{date})")
    long countMemberInDeptAt(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("date") LocalDate date);
}
