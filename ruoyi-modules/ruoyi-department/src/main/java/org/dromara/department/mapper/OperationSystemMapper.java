package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.OperationSystem;
import org.dromara.department.domain.vo.OperationSystemVo;
import org.dromara.department.service.DepartmentScope;

import java.time.LocalDate;
import java.util.List;

/**
 * 系统在线率台账数据层。
 */
@Mapper
public interface OperationSystemMapper extends BaseMapperPlus<OperationSystem, OperationSystemVo> {

    /** 按权限范围分页查询系统在线率记录。 */
    @Select({
        "<script>",
        "select s.id, s.dept_id, s.project_id, p.project_name, s.stat_date, s.system_name, s.responsible_person,",
        "s.online_days, s.downtime_minutes, s.online_rate, s.remark, s.source_type, s.source_file_name, s.create_time",
        "from dm_operation_system s left join dm_department_project p on p.id = s.project_id where s.del_flag = '0'",
        "<if test='beginDate != null'> and s.stat_date &gt;= #{beginDate} </if>",
        "<if test='endDate != null'> and s.stat_date &lt;= #{endDate} </if>",
        "<if test='systemName != null and systemName != \"\"'> and s.system_name like concat('%', #{systemName}, '%') </if>",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and s.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by s.stat_date desc, s.id desc",
        "</script>"
    })
    Page<OperationSystemVo> selectPageList(Page<OperationSystemVo> page,
                                            @Param("beginDate") LocalDate beginDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("systemName") String systemName,
                                            @Param("scope") DepartmentScope scope);

    /** 查询指定日期范围内的系统在线率记录，用于汇总统计。 */
    @Select({
        "<script>",
        "select s.id, s.dept_id, s.project_id, p.project_name, s.stat_date, s.system_name, s.responsible_person,",
        "s.online_days, s.downtime_minutes, s.online_rate, s.remark, s.source_type, s.source_file_name, s.create_time",
        "from dm_operation_system s left join dm_department_project p on p.id = s.project_id where s.del_flag = '0'",
        "and s.stat_date &gt;= #{beginDate} and s.stat_date &lt;= #{endDate}",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and s.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by s.stat_date asc, s.id asc",
        "</script>"
    })
    List<OperationSystemVo> selectForSummary(@Param("beginDate") LocalDate beginDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("scope") DepartmentScope scope);
}
