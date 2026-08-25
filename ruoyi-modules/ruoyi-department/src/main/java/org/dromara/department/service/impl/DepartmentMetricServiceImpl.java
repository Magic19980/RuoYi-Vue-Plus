package org.dromara.department.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.department.domain.vo.OperationSummaryVo;
import org.dromara.department.domain.vo.WorkOrderSummaryVo;
import org.dromara.department.service.IDepartmentMetricService;
import org.dromara.department.service.IOperationLedgerService;
import org.dromara.department.service.IWorkOrderService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/** 统一人工单、运维台账指标口径。 */
@RequiredArgsConstructor
@Service
public class DepartmentMetricServiceImpl implements IDepartmentMetricService {

    private final IWorkOrderService workOrderService;
    private final IOperationLedgerService operationLedgerService;

    @Override
    public WorkOrderSummaryVo buildManualOrderSummary(LocalDate beginDate, LocalDate endDate) {
        return workOrderService.buildSummary(beginDate, endDate);
    }

    @Override
    public OperationSummaryVo buildOperationSummary(LocalDate beginDate, LocalDate endDate) {
        return operationLedgerService.buildSummary(beginDate, endDate);
    }
}
