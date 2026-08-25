package org.dromara.department.service;

import org.dromara.department.domain.vo.OperationSummaryVo;
import org.dromara.department.domain.vo.WorkOrderSummaryVo;

import java.time.LocalDate;

/**
 * 科室统一指标服务。
 *
 * <p>指标的业务来源在这里集中定义，周报快照和 PPT 只消费同一份汇总结果，
 * 避免页面、周报和导出各自重复计算导致口径漂移。</p>
 */
public interface IDepartmentMetricService {

    WorkOrderSummaryVo buildManualOrderSummary(LocalDate beginDate, LocalDate endDate);

    OperationSummaryVo buildOperationSummary(LocalDate beginDate, LocalDate endDate);
}
