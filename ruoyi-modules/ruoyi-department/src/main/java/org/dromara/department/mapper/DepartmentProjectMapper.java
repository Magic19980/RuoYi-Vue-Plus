package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentProject;
import org.dromara.department.domain.bo.DepartmentProjectQueryBo;
import org.dromara.department.domain.vo.DepartmentProjectVo;
import org.dromara.department.service.DepartmentScope;

import java.util.List;

/**
 * 科室项目主数据数据层。
 */
@Mapper
public interface DepartmentProjectMapper extends BaseMapperPlus<DepartmentProject, DepartmentProjectVo> {

    @Select({
        "<script>",
        "select p.id, p.dept_id, p.project_code, p.project_name, p.project_type, p.responsible_person,",
        "p.status, p.sort_num, p.remark, p.create_time,",
        "(select count(1) from dm_operation_record o where o.project_id = p.id and o.del_flag = '0') as operation_record_count",
        "from dm_department_project p where p.del_flag = '0'",
        "<if test='bo.projectCode != null and bo.projectCode != \"\"'> and p.project_code like concat('%', #{bo.projectCode}, '%') </if>",
        "<if test='bo.projectName != null and bo.projectName != \"\"'> and p.project_name like concat('%', #{bo.projectName}, '%') </if>",
        "<if test='bo.projectType != null and bo.projectType != \"\"'> and p.project_type = #{bo.projectType} </if>",
        "<if test='bo.status != null and bo.status != \"\"'> and p.status = #{bo.status} </if>",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and p.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by p.sort_num asc, p.id asc",
        "</script>"
    })
    Page<DepartmentProjectVo> selectPageList(Page<DepartmentProjectVo> page,
                                              @Param("bo") DepartmentProjectQueryBo bo,
                                              @Param("scope") DepartmentScope scope);

    @Select({
        "<script>",
        "select p.id, p.dept_id, p.project_code, p.project_name, p.project_type, p.responsible_person,",
        "p.status, p.sort_num, p.remark, p.create_time",
        "from dm_department_project p where p.del_flag = '0' and p.status = 'ENABLED'",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and p.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by p.sort_num asc, p.project_name asc, p.id asc",
        "</script>"
    })
    List<DepartmentProjectVo> selectOptions(@Param("scope") DepartmentScope scope);

    @Select("select id from dm_department_project where del_flag = '0' and dept_id = #{deptId} and project_name = #{projectName} limit 1")
    Long selectIdByName(@Param("deptId") Long deptId, @Param("projectName") String projectName);

    @Select("select count(1) from dm_operation_record where project_id = #{projectId} and del_flag = '0'")
    int countOperationRecords(@Param("projectId") Long projectId);
}
