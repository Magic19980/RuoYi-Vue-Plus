package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentConfig;
import org.dromara.department.domain.bo.DepartmentConfigQueryBo;
import org.dromara.department.domain.vo.DepartmentConfigVo;
import org.dromara.department.service.DepartmentScope;

import java.util.List;

/** 业务科室配置数据层。 */
@Mapper
public interface DepartmentConfigMapper extends BaseMapperPlus<DepartmentConfig, DepartmentConfigVo> {

    /**
     * 按权限范围分页查询业务科室配置。
     *
     * @param page  分页对象
     * @param bo    科室配置查询条件
     * @param scope 科室数据权限范围
     * @return 分页科室配置数据
     */
    @Select({
        "<script>",
        "select c.dept_id, d.dept_name, case when d.dept_id is not null and d.del_flag = '0' and d.status = '0' then true else false end as system_dept_available,",
        "c.status, c.manager_user_id,",
        "coalesce(m.nick_name, m.user_name) as manager_name, c.sort_num,",
        "coalesce(active_member.member_count, 0) as member_count,",
        "c.remark, c.create_time, c.update_time",
        "from dm_department c left join sys_dept d on d.dept_id = c.dept_id",
        "left join sys_user m on m.user_id = c.manager_user_id and m.del_flag = '0'",
        "left join (select p.create_dept, count(1) as member_count from dm_person_profile p",
        "where p.del_flag = '0' and p.member_status = 'ACTIVE' and p.join_date &lt;= current_date",
        "and (p.leave_date is null or p.leave_date &gt; current_date)",
        "<if test='!scope.all and scope.deptId != null'> and p.create_dept = #{scope.deptId} </if>",
        "group by p.create_dept) active_member on active_member.create_dept = c.dept_id",
        "where c.del_flag = '0'",
        "<if test='bo.deptName != null and bo.deptName != \"\"'> and d.dept_name like concat('%', #{bo.deptName}, '%') </if>",
        "<if test='bo.status != null and bo.status != \"\"'> and c.status = #{bo.status} </if>",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and c.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by c.sort_num asc, d.dept_name asc, c.dept_id asc",
        "</script>"
    })
    Page<DepartmentConfigVo> selectPageList(Page<DepartmentConfigVo> page,
                                              @Param("bo") DepartmentConfigQueryBo bo,
                                              @Param("scope") DepartmentScope scope);

    /** 查询指定业务科室的详细配置。 */
    @Select("select c.dept_id, d.dept_name, c.status, c.manager_user_id, coalesce(m.nick_name, m.user_name) as manager_name, c.sort_num, "
        + "(select count(1) from dm_person_profile p where p.create_dept = c.dept_id and p.del_flag = '0' and p.member_status = 'ACTIVE' and p.join_date <= current_date and (p.leave_date is null or p.leave_date > current_date)) as member_count, "
        + "c.remark, c.create_time, c.update_time from dm_department c join sys_dept d on d.dept_id = c.dept_id and d.del_flag = '0' "
        + "left join sys_user m on m.user_id = c.manager_user_id and m.del_flag = '0' "
        + "where c.dept_id = #{deptId} and c.del_flag = '0'")
    DepartmentConfigVo selectDetail(@Param("deptId") Long deptId);

    /** 查询尚未配置为业务科室的有效系统部门。 */
    @Select({
        "<script>",
        "select d.dept_id, d.dept_name from sys_dept d left join dm_department c on c.dept_id = d.dept_id",
        "where d.del_flag = '0' and d.status = '0' and c.dept_id is null",
        "and (d.dept_name like concat('%', #{deptName}, '%') or coalesce(d.indonesian_name, '') like concat('%', #{deptName}, '%'))",
        "order by d.dept_name, d.dept_id limit 100",
        "</script>"
    })
    List<DepartmentConfigVo> selectAvailableDepartments(@Param("deptName") String deptName);

    /** 查询指定系统组织下的直接子部门及其是否可选状态。 */
    @Select({
        "<script>",
        "select d.dept_id, d.parent_id, d.dept_name,",
        "case when c.dept_id is null then true else false end as selectable,",
        "case when exists (select 1 from sys_dept child where child.parent_id = d.dept_id and child.del_flag = '0' and child.status = '0') then true else false end as has_children",
        "from sys_dept d left join dm_department c on c.dept_id = d.dept_id and c.del_flag = '0'",
        "where d.del_flag = '0' and d.status = '0' and d.parent_id = #{parentId}",
        "order by d.order_num asc, d.dept_name asc, d.dept_id asc",
        "</script>"
    })
    List<DepartmentConfigVo> selectOrganizationChildren(@Param("parentId") Long parentId);

    /** 查询全部已启用的业务科室。 */
    @Select("select c.dept_id, d.dept_name from dm_department c join sys_dept d on d.dept_id = c.dept_id and d.del_flag = '0' "
        + "where c.del_flag = '0' and c.status = 'ENABLED' and d.status = '0' "
        + "order by c.sort_num asc, d.dept_name asc, c.dept_id asc")
    List<DepartmentConfigVo> selectEnabledDepartments();

    /** 统计指定业务科室是否存在已启用配置。 */
    @Select("select count(1) from dm_department c join sys_dept d on d.dept_id = c.dept_id and d.del_flag = '0' and d.status = '0' "
        + "where c.dept_id = #{deptId} and c.del_flag = '0' and c.status = 'ENABLED'")
    long countEnabled(@Param("deptId") Long deptId);

    /** 统计指定系统部门是否有效。 */
    @Select("select count(1) from sys_dept where dept_id = #{deptId} and del_flag = '0' and status = '0'")
    long countActiveSystemDepartment(@Param("deptId") Long deptId);
}
