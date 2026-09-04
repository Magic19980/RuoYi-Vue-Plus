package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.OperationRecord;
import org.dromara.department.domain.bo.OperationRecordQueryBo;
import org.dromara.department.domain.vo.OperationRecordVo;
import org.dromara.department.service.DepartmentScope;

import java.time.LocalDate;
import java.util.List;

/**
 * 运维工作记录数据层。
 */
@Mapper
public interface OperationRecordMapper extends BaseMapperPlus<OperationRecord, OperationRecordVo> {

    /** 按权限范围分页查询运维工作记录。 */
    @Select({
        "<script>",
        "select o.id, o.dept_id, o.request_person, o.customer_unit, o.request_role_type, o.request_time,",
        "o.handler, o.process_time, o.completion_time, o.response_minutes, o.processing_minutes, o.lunch_break,",
        "o.process_status, o.process_method, o.submitter, o.project_id, p.project_name, o.system_name, o.fault_type, o.business_description,",
        "o.solution, o.remark, o.source_type, o.source_file_name, o.create_time",
        "from dm_operation_record o left join dm_department_project p on p.id = o.project_id and p.del_flag = '0' where o.del_flag = '0'",
        "<if test='bo.beginDate != null'> and o.request_time &gt;= concat(#{bo.beginDate}, ' 00:00:00') </if>",
        "<if test='bo.endDate != null'> and o.request_time &lt; concat(date_add(#{bo.endDate}, interval 1 day), ' 00:00:00') </if>",
        "<if test='bo.customerUnit != null and bo.customerUnit != \"\"'> and o.customer_unit like concat('%', #{bo.customerUnit}, '%') </if>",
        "<if test='bo.systemName != null and bo.systemName != \"\"'> and o.system_name like concat('%', #{bo.systemName}, '%') </if>",
        "<if test='bo.projectId != null'> and o.project_id = #{bo.projectId} </if>",
        "<if test='bo.processStatus != null and bo.processStatus != \"\"'> and o.process_status = #{bo.processStatus} </if>",
        "<if test='bo.processMethod != null and bo.processMethod != \"\"'> and o.process_method = #{bo.processMethod} </if>",
        "<if test='bo.keyword != null and bo.keyword != \"\"'> and (o.request_person like concat('%', #{bo.keyword}, '%') or o.handler like concat('%', #{bo.keyword}, '%') or o.business_description like concat('%', #{bo.keyword}, '%') or o.solution like concat('%', #{bo.keyword}, '%')) </if>",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and o.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by o.request_time desc, o.id desc",
        "</script>"
    })
    Page<OperationRecordVo> selectPageList(Page<OperationRecordVo> page,
                                            @Param("bo") OperationRecordQueryBo bo,
                                            @Param("scope") DepartmentScope scope);

    /** 查询指定日期范围内的运维记录，用于汇总统计。 */
    @Select({
        "<script>",
        "select o.id, o.dept_id, o.request_person, o.customer_unit, o.request_role_type, o.request_time,",
        "o.handler, o.process_time, o.completion_time, o.response_minutes, o.processing_minutes, o.lunch_break,",
        "o.process_status, o.process_method, o.submitter, o.project_id, p.project_name, o.system_name, o.fault_type, o.business_description,",
        "o.solution, o.remark, o.source_type, o.source_file_name, o.create_time",
        "from dm_operation_record o left join dm_department_project p on p.id = o.project_id and p.del_flag = '0' where o.del_flag = '0'",
        "and o.request_time &gt;= concat(#{beginDate}, ' 00:00:00')",
        "and o.request_time &lt; concat(date_add(#{endDate}, interval 1 day), ' 00:00:00')",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and o.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by o.request_time asc, o.id asc",
        "</script>"
    })
    List<OperationRecordVo> selectForSummary(@Param("beginDate") LocalDate beginDate,
                                             @Param("endDate") LocalDate endDate,
                                             @Param("scope") DepartmentScope scope);
}
