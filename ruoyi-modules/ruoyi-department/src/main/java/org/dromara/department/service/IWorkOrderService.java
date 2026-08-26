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
import org.springframework.http.ResponseEntity;
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

    ResponseEntity<byte[]> previewSourcePdf(Long workOrderId);

    List<WorkOrderDetailVo> queryDetails(Long workOrderId);

    Boolean updateDetailByBo(WorkOrderDetailBo bo);

    Boolean deleteDetails(Collection<Long> ids);

    Boolean insertByBo(WorkOrderBo bo);

    Boolean updateByBo(WorkOrderBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    WorkOrderImportResultVo importPdf(MultipartFile file);

    WorkOrderSummaryVo buildSummary(LocalDate beginDate, LocalDate endDate);

}
