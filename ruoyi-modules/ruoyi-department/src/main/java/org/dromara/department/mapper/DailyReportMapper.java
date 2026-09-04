package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DailyReport;
import org.dromara.department.domain.bo.DailyReportQueryBo;
import org.dromara.department.domain.vo.DailyReportVo;
import org.dromara.department.service.DepartmentScope;

import java.time.LocalDate;

/**
 * 日报数据层。
 */
@Mapper
public interface DailyReportMapper extends BaseMapperPlus<DailyReport, DailyReportVo> {

    /**
     * 按权限范围分页查询日报。
     *
     * @param page   分页对象
     * @param bo     日报查询条件
     * @param userId 当前登录用户主键
     * @param scope  科室数据权限范围
     * @return 分页日报数据
     */
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
        "<if test='bo.userId != null and scope.all'> and r.user_id = #{bo.userId} </if>",
        "<if test='bo.userName != null and bo.userName != \"\"'> and (u.user_name like concat('%', #{bo.userName}, '%') or u.nick_name like concat('%', #{bo.userName}, '%')) </if>",
        "<choose>",
        "<when test='scope.all'>",
        "</when>",
        "<when test='scope.deptId != null'> and r.dept_id = #{scope.deptId} </when>",
        "<otherwise> and r.user_id = #{userId} </otherwise>",
        "</choose>",
        "order by r.report_date desc, r.id desc",
        "</script>"
    })
    Page<DailyReportVo> selectPageList(Page<DailyReportVo> page,
                                        @Param("bo") DailyReportQueryBo bo,
                                        @Param("userId") Long userId,
                                        @Param("scope") DepartmentScope scope);

    /**
     * 按用户名或昵称查询用户主键。
     *
     * @param name 用户名或昵称
     * @return 用户主键，不存在时返回 {@code null}
     */
    @Select("select user_id from sys_user where del_flag = '0' and (user_name = #{name} or nick_name = #{name}) limit 1")
    Long selectUserIdByName(@Param("name") String name);

    /**
     * 统计用户当前是否为指定科室的有效成员。
     *
     * @param userId 用户主键
     * @param deptId 业务科室主键
     * @return 有效成员记录数
     */
    @Select("select count(1) from dm_person_profile p join sys_user u on u.user_id = p.user_id and u.del_flag = '0' and u.status = '0' "
        + "where p.user_id = #{userId} and p.create_dept = #{deptId} and p.del_flag = '0' and p.member_status = 'ACTIVE' "
        + "and p.join_date <= current_date and (p.leave_date is null or p.leave_date > current_date)")
    long countMemberInDept(@Param("userId") Long userId, @Param("deptId") Long deptId);

    /**
     * 统计用户在指定日期是否属于指定科室的有效成员。
     *
     * @param userId 用户主键
     * @param deptId 业务科室主键
     * @param date   判断日期
     * @return 有效成员记录数
     */
    @Select("select count(1) from dm_person_profile p join sys_user u on u.user_id = p.user_id and u.del_flag = '0' and u.status = '0' "
        + "where p.user_id = #{userId} and p.create_dept = #{deptId} and p.del_flag = '0' "
        + "and p.join_date <= #{date} and (p.leave_date is null or p.leave_date > #{date})")
    long countMemberInDeptAt(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("date") LocalDate date);
}
