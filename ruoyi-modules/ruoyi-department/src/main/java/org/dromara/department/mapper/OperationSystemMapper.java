package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.OperationSystem;
import org.dromara.department.domain.vo.OperationSystemVo;

import java.time.LocalDate;
import java.util.List;

/**
 * 系统在线率台账数据层。
 */
@Mapper
public interface OperationSystemMapper extends BaseMapperPlus<OperationSystem, OperationSystemVo> {

    @Select({
        "<script>",
        "select s.id, s.dept_id, s.project_id, p.project_name, s.stat_date, s.system_name, s.responsible_person, s.server_name, s.server_ip,",
        "s.online_days, s.downtime_minutes, s.online_rate, s.remark, s.source_type, s.source_file_name, s.create_time",
        "from dm_operation_system s left join dm_department_project p on p.id = s.project_id where s.del_flag = '0'",
        "<if test='beginDate != null'> and s.stat_date &gt;= #{beginDate} </if>",
        "<if test='endDate != null'> and s.stat_date &lt;= #{endDate} </if>",
        "<if test='systemName != null and systemName != \"\"'> and s.system_name like concat('%', #{systemName}, '%') </if>",
        "<choose>",
        "<when test='all == true'></when>",
        "<otherwise> and s.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "order by s.stat_date desc, s.id desc",
        "</script>"
    })
    Page<OperationSystemVo> selectPageList(Page<OperationSystemVo> page,
                                            @Param("beginDate") LocalDate beginDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("systemName") String systemName,
                                            @Param("deptId") Long deptId,
                                            @Param("all") boolean all);

    @Select({
        "<script>",
        "select s.id, s.dept_id, s.project_id, p.project_name, s.stat_date, s.system_name, s.responsible_person, s.server_name, s.server_ip,",
        "s.online_days, s.downtime_minutes, s.online_rate, s.remark, s.source_type, s.source_file_name, s.create_time",
        "from dm_operation_system s left join dm_department_project p on p.id = s.project_id where s.del_flag = '0'",
        "and s.stat_date &gt;= #{beginDate} and s.stat_date &lt;= #{endDate}",
        "<choose>",
        "<when test='all == true'></when>",
        "<otherwise> and s.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "order by s.stat_date asc, s.id asc",
        "</script>"
    })
    List<OperationSystemVo> selectForSummary(@Param("beginDate") LocalDate beginDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("deptId") Long deptId,
                                              @Param("all") boolean all);
}
