package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.WorkOrder;
import org.dromara.department.domain.bo.WorkOrderQueryBo;
import org.dromara.department.domain.vo.WorkOrderVo;

import java.util.List;

/**
 * 工单台账数据层。
 */
@Mapper
public interface WorkOrderMapper extends BaseMapperPlus<WorkOrder, WorkOrderVo> {

    @Select({
        "<script>",
        "select w.id, w.dept_id, w.ticket_no, w.occur_date, w.source_period_start, w.source_period_end,",
        "w.request_dept, w.settlement_unit, w.project_owner, w.system_name, w.install_department, w.install_team, w.work_category, w.fault_type,",
        "w.title, w.work_content, w.unit, w.quantity, w.responsible_person, w.handler, w.status,",
        "w.resolution_minutes, w.feedback_channel, w.review_status, w.source_type, w.source_batch_id,",
        "w.source_file_name, w.source_page, (select count(1) from dm_work_order_detail d where d.work_order_id = w.id and d.del_flag = '0') as detail_count,",
        "w.parse_confidence, w.parse_message, w.remark, w.create_time",
        "from dm_work_order w",
        "where w.del_flag = '0'",
        // 发生日期按月份保存为该月1号，查询按月份与日期范围是否重叠处理。
        "<if test='bo.beginDate != null'> and (w.occur_date is null or last_day(w.occur_date) &gt;= #{bo.beginDate}) </if>",
        "<if test='bo.endDate != null'> and (w.occur_date is null or w.occur_date &lt;= #{bo.endDate}) </if>",
        "<if test='bo.ticketNo != null and bo.ticketNo != \"\"'> and w.ticket_no like concat('%', #{bo.ticketNo}, '%') </if>",
        "<if test='bo.systemName != null and bo.systemName != \"\"'> and w.system_name like concat('%', #{bo.systemName}, '%') </if>",
        "<if test='bo.faultType != null and bo.faultType != \"\"'> and w.fault_type = #{bo.faultType} </if>",
        "<if test='bo.status != null and bo.status != \"\"'> and w.status = #{bo.status} </if>",
        "<if test='bo.reviewStatus != null and bo.reviewStatus != \"\"'> and w.review_status = #{bo.reviewStatus} </if>",
        "<if test='bo.sourceType != null and bo.sourceType != \"\"'> and w.source_type = #{bo.sourceType} </if>",
        "<if test='bo.keyword != null and bo.keyword != \"\"'> and (w.title like concat('%', #{bo.keyword}, '%') or w.work_content like concat('%', #{bo.keyword}, '%') or w.handler like concat('%', #{bo.keyword}, '%')) </if>",
        "<choose>",
        "<when test='all == true'></when>",
        "<otherwise> and w.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "order by w.occur_date desc, w.id desc",
        "</script>"
    })
    Page<WorkOrderVo> selectPageList(Page<WorkOrderVo> page,
                                      @Param("bo") WorkOrderQueryBo bo,
                                      @Param("deptId") Long deptId,
                                      @Param("all") boolean all);

    @Select({
        "<script>",
        "select w.id, w.dept_id, w.ticket_no, w.occur_date, w.source_period_start, w.source_period_end,",
        "w.request_dept, w.settlement_unit, w.project_owner, w.system_name, w.install_department, w.install_team, w.work_category, w.fault_type,",
        "w.title, w.work_content, w.unit, w.quantity, w.responsible_person, w.handler, w.status,",
        "w.resolution_minutes, w.feedback_channel, w.review_status, w.source_type, w.source_batch_id,",
        "w.source_file_name, w.source_page, (select count(1) from dm_work_order_detail d where d.work_order_id = w.id and d.del_flag = '0') as detail_count,",
        "w.parse_confidence, w.parse_message, w.remark, w.create_time",
        "from dm_work_order w",
        "where w.del_flag = '0' and w.review_status &lt;&gt; 'REJECTED' and w.occur_date is not null",
        "<if test='includePending != true'> and w.review_status = 'CONFIRMED' </if>",
        "and w.occur_date &lt;= #{endDate} and last_day(w.occur_date) &gt;= #{beginDate}",
        "<choose>",
        "<when test='all == true'></when>",
        "<otherwise> and w.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "order by w.occur_date asc, w.id asc",
        "</script>"
    })
    List<WorkOrderVo> selectForSummary(@Param("beginDate") java.time.LocalDate beginDate,
                                       @Param("endDate") java.time.LocalDate endDate,
                                       @Param("deptId") Long deptId,
                                       @Param("all") boolean all,
                                       @Param("includePending") boolean includePending);

    @Select({
        "<script>",
        "select count(1) from dm_work_order w where w.del_flag = '0' and w.review_status = 'PENDING'",
        "and (w.occur_date is null or (w.occur_date &lt;= #{endDate} and last_day(w.occur_date) &gt;= #{beginDate}))",
        "<choose>",
        "<when test='all == true'></when>",
        "<otherwise> and w.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "</script>"
    })
    int countPending(@Param("beginDate") java.time.LocalDate beginDate,
                     @Param("endDate") java.time.LocalDate endDate,
                     @Param("deptId") Long deptId,
                     @Param("all") boolean all);
}
