package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.PersonProfile;
import org.dromara.department.domain.bo.PersonProfileQueryBo;
import org.dromara.department.domain.vo.PersonProfileVo;
import org.dromara.department.domain.vo.PersonUserOptionVo;

import java.util.List;

/**
 * 人员档案数据层。
 */
@Mapper
public interface PersonProfileMapper extends BaseMapperPlus<PersonProfile, PersonProfileVo> {

    @Select({
        "<script>",
        "select p.id, p.user_id, u.user_name, u.nick_name, d.dept_name,",
        "p.employee_no, p.job_title, p.daily_report_enabled, p.reminder_time, p.remark",
        "from dm_person_profile p",
        "left join sys_user u on u.user_id = p.user_id and u.del_flag = '0'",
        "left join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0'",
        "where p.del_flag = '0'",
        "<if test='bo.id != null'> and p.id = #{bo.id} </if>",
        "<if test='bo.userName != null and bo.userName != \"\"'> and (u.user_name like concat('%', #{bo.userName}, '%') or u.nick_name like concat('%', #{bo.userName}, '%')) </if>",
        "<if test='bo.jobTitle != null and bo.jobTitle != \"\"'> and p.job_title like concat('%', #{bo.jobTitle}, '%') </if>",
        "<if test='bo.dailyReportEnabled != null and bo.dailyReportEnabled != \"\"'> and p.daily_report_enabled = #{bo.dailyReportEnabled} </if>",
        "<choose>",
        "<when test='all == true'>",
        "</when>",
        "<when test='deptId != null'> and u.dept_id = #{deptId} </when>",
        "<otherwise> and p.user_id = #{userId} </otherwise>",
        "</choose>",
        "order by p.id desc",
        "</script>"
    })
    Page<PersonProfileVo> selectPageList(Page<PersonProfileVo> page,
                                          @Param("bo") PersonProfileQueryBo bo,
                                          @Param("userId") Long userId,
                                          @Param("deptId") Long deptId,
                                          @Param("all") boolean all);

    @Select({
        "<script>",
        "select u.user_id, u.dept_id, u.user_name, u.nick_name, d.dept_name",
        "from sys_user u left join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0'",
        "where u.del_flag = '0' and u.status = '0'",
        "<if test='all == false and deptId != null'> and u.dept_id = #{deptId} </if>",
        "order by u.user_name",
        "</script>"
    })
    List<PersonUserOptionVo> selectUserOptions(@Param("deptId") Long deptId, @Param("all") boolean all);

    @Select("select u.dept_id from dm_person_profile p join sys_user u on u.user_id = p.user_id and u.del_flag = '0' where p.id = #{id} and p.del_flag = '0'")
    Long selectDeptIdByProfileId(@Param("id") Long id);
}
