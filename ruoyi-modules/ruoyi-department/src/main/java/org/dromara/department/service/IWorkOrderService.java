package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.WorkOrderBo;
import org.dromara.department.domain.bo.WorkOrderDetailBo;
import org.dromara.department.domain.bo.WorkOrderQueryBo;
import org.dromara.department.domain.vo.WorkOrderImportResultVo;
import org.dromara.department.domain.vo.WorkOrderDetailVo;
import org.dromara.department.domain.vo.WorkOrderExportVo;
import org.dromara.department.domain.vo.WorkOrderSummaryVo;
import org.dromara.department.domain.vo.WorkOrderVo;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 工单台账业务接口。
 */
public interface IWorkOrderService {

    PageResult<WorkOrderVo> queryPageList(WorkOrderQueryBo bo, PageQuery pageQuery);

    List<WorkOrderVo> queryList(WorkOrderQueryBo bo);

    List<WorkOrderExportVo> queryExportList(WorkOrderQueryBo bo);

    WorkOrderVo queryById(Long id);

    List<WorkOrderDetailVo> queryDetails(Long workOrderId);

    Boolean updateDetailByBo(WorkOrderDetailBo bo);

    Boolean deleteDetails(Collection<Long> ids);

    Boolean insertByBo(WorkOrderBo bo);

    Boolean updateByBo(WorkOrderBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    WorkOrderImportResultVo importPdf(MultipartFile file);

    WorkOrderSummaryVo buildSummary(LocalDate beginDate, LocalDate endDate);

    /**
     * 构建人工单汇总，可控制是否将待确认记录纳入当前页面指标。
     * 周报调用默认方法，仅统计已确认记录；人工单页面调用 includePending=true，便于导入后核对原始数据。
     */
    WorkOrderSummaryVo buildSummary(LocalDate beginDate, LocalDate endDate, boolean includePending);

}
