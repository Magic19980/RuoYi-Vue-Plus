package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentConfig;
import org.dromara.department.domain.bo.DepartmentConfigQueryBo;
import org.dromara.department.domain.vo.DepartmentConfigVo;

import java.util.List;

/** 业务科室配置数据层。 */
@Mapper
public interface DepartmentConfigMapper extends BaseMapperPlus<DepartmentConfig, DepartmentConfigVo> {

    @Select({
        "<script>",
        "select c.dept_id, d.dept_name, c.status, c.manager_user_id,",
        "coalesce(m.nick_name, m.user_name) as manager_name, c.sort_num,",
        "(select count(1) from dm_person_profile p where p.create_dept = c.dept_id and p.del_flag = '0' and p.member_status = 'ACTIVE' and p.join_date &lt;= current_date and (p.leave_date is null or p.leave_date > current_date)) as member_count,",
        "c.remark, c.create_time, c.update_time",
        "from dm_department c join sys_dept d on d.dept_id = c.dept_id and d.del_flag = '0'",
        "left join sys_user m on m.user_id = c.manager_user_id and m.del_flag = '0'",
        "where c.del_flag = '0'",
        "<if test='bo.deptName != null and bo.deptName != \"\"'> and d.dept_name like concat('%', #{bo.deptName}, '%') </if>",
        "<if test='bo.status != null and bo.status != \"\"'> and c.status = #{bo.status} </if>",
        "<choose>",
        "<when test='all == true'></when>",
        "<otherwise> and c.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "order by c.sort_num asc, d.dept_name asc, c.dept_id asc",
        "</script>"
    })
    Page<DepartmentConfigVo> selectPageList(Page<DepartmentConfigVo> page,
                                              @Param("bo") DepartmentConfigQueryBo bo,
                                              @Param("deptId") Long deptId,
                                              @Param("all") boolean all);

    @Select("select c.dept_id, d.dept_name, c.status, c.manager_user_id, coalesce(m.nick_name, m.user_name) as manager_name, c.sort_num, "
        + "(select count(1) from dm_person_profile p where p.create_dept = c.dept_id and p.del_flag = '0' and p.member_status = 'ACTIVE' and p.join_date <= current_date and (p.leave_date is null or p.leave_date > current_date)) as member_count, "
        + "c.remark, c.create_time, c.update_time from dm_department c join sys_dept d on d.dept_id = c.dept_id and d.del_flag = '0' "
        + "left join sys_user m on m.user_id = c.manager_user_id and m.del_flag = '0' "
        + "where c.dept_id = #{deptId} and c.del_flag = '0'")
    DepartmentConfigVo selectDetail(@Param("deptId") Long deptId);

    @Select("select d.dept_id, d.dept_name from sys_dept d left join dm_department c on c.dept_id = d.dept_id and c.del_flag = '0' "
        + "where d.del_flag = '0' and d.status = '0' and c.dept_id is null order by d.dept_name, d.dept_id")
    List<DepartmentConfigVo> selectAvailableDepartments();

    @Select("select c.dept_id, d.dept_name from dm_department c join sys_dept d on d.dept_id = c.dept_id and d.del_flag = '0' "
        + "where c.del_flag = '0' and c.status = 'ENABLED' and d.status = '0' "
        + "order by c.sort_num asc, d.dept_name asc, c.dept_id asc")
    List<DepartmentConfigVo> selectEnabledDepartments();

    @Select("select count(1) from dm_department where dept_id = #{deptId} and del_flag = '0' and status = 'ENABLED'")
    long countEnabled(@Param("deptId") Long deptId);

    @Select("select count(1) from sys_dept where dept_id = #{deptId} and del_flag = '0' and status = '0'")
    long countActiveSystemDepartment(@Param("deptId") Long deptId);
}
