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

    /**
     * 分页查询工单台账。
     *
     * @param bo        工单查询条件
     * @param pageQuery 分页参数
     * @return 分页工单数据
     */
    PageResult<WorkOrderVo> queryPageList(WorkOrderQueryBo bo, PageQuery pageQuery);

    /**
     * 查询工单台账列表。
     *
     * @param bo 工单查询条件
     * @return 工单列表
     */
    List<WorkOrderVo> queryList(WorkOrderQueryBo bo);

    /**
     * 查询工单导出数据。
     *
     * @param bo 工单查询条件
     * @return 工单导出列表
     */
    List<WorkOrderExportVo> queryExportList(WorkOrderQueryBo bo);

    /**
     * 查询工单详情。
     *
     * @param id 工单主键
     * @return 工单详情
     */
    WorkOrderVo queryById(Long id);

    /**
     * 预览工单原始 PDF 文件。
     *
     * @param workOrderId 工单主键
     * @return PDF预览响应内容
     */
    ResponseEntity<byte[]> previewSourcePdf(Long workOrderId);

    /**
     * 查询工单解析明细。
     *
     * @param workOrderId 工单主键
     * @return 工单明细列表
     */
    List<WorkOrderDetailVo> queryDetails(Long workOrderId);

    /**
     * 修改工单解析明细。
     *
     * @param bo 工单明细修改参数
     * @return 是否修改成功
     */
    Boolean updateDetailByBo(WorkOrderDetailBo bo);

    /**
     * 删除工单解析明细。
     *
     * @param ids 明细主键集合
     * @return 是否删除成功
     */
    Boolean deleteDetails(Collection<Long> ids);

    /**
     * 新增人工录入工单。
     *
     * @param bo 工单新增参数
     * @return 是否新增成功
     */
    Boolean insertByBo(WorkOrderBo bo);

    /**
     * 修改工单。
     *
     * @param bo 工单修改参数
     * @return 是否修改成功
     */
    Boolean updateByBo(WorkOrderBo bo);

    /**
     * 删除允许删除的工单。
     *
     * @param ids 工单主键集合
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /**
     * 导入并解析工单 PDF 文件。
     *
     * @param file 待解析的PDF文件
     * @return 导入批次及解析结果
     */
    WorkOrderImportResultVo importPdf(MultipartFile file);

    /**
     * 构建指定日期范围的工单指标汇总。
     *
     * @param beginDate 统计开始日期，包含当天
     * @param endDate   统计结束日期，包含当天
     * @return 工单指标汇总
     */
    WorkOrderSummaryVo buildSummary(LocalDate beginDate, LocalDate endDate);

}
