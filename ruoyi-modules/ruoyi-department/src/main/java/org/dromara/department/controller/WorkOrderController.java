package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.WorkOrderBo;
import org.dromara.department.domain.bo.WorkOrderDetailBo;
import org.dromara.department.domain.bo.WorkOrderQueryBo;
import org.dromara.department.domain.vo.WorkOrderDetailVo;
import org.dromara.department.domain.vo.WorkOrderExportVo;
import org.dromara.department.domain.vo.WorkOrderImportResultVo;
import org.dromara.department.domain.vo.WorkOrderSummaryVo;
import org.dromara.department.domain.vo.WorkOrderVo;
import org.dromara.department.service.IWorkOrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 工单台账接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/workOrder")
public class WorkOrderController extends BaseController {

    private final IWorkOrderService workOrderService;

    @SaCheckPermission("department:workOrder:list")
    @GetMapping("/list")
    public R<PageResult<WorkOrderVo>> list(WorkOrderQueryBo bo, PageQuery pageQuery) {
        return R.ok(workOrderService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:workOrder:query")
    @GetMapping("/{id}")
    public R<WorkOrderVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(workOrderService.queryById(id));
    }

    @SaCheckPermission("department:workOrder:query")
    @GetMapping("/{id}/details")
    public R<List<WorkOrderDetailVo>> details(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(workOrderService.queryDetails(id));
    }

    @SaCheckPermission("department:workOrder:edit")
    @Log(title = "人工统计明细", businessType = BusinessType.UPDATE)
    @PutMapping("/detail")
    public R<Void> editDetail(@Validated(EditGroup.class) @RequestBody WorkOrderDetailBo bo) {
        return toAjax(workOrderService.updateDetailByBo(bo));
    }

    @SaCheckPermission("department:workOrder:remove")
    @Log(title = "人工统计明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/detail/{ids}")
    public R<Void> removeDetail(@NotEmpty(message = "明细主键不能为空") @PathVariable Long[] ids) {
        return toAjax(workOrderService.deleteDetails(Arrays.asList(ids)));
    }

    @SaCheckPermission("department:workOrder:add")
    @Log(title = "工单台账", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody WorkOrderBo bo) {
        return toAjax(workOrderService.insertByBo(bo));
    }

    @SaCheckPermission("department:workOrder:edit")
    @Log(title = "工单台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody WorkOrderBo bo) {
        return toAjax(workOrderService.updateByBo(bo));
    }

    @SaCheckPermission("department:workOrder:remove")
    @Log(title = "工单台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(workOrderService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @SaCheckPermission("department:workOrder:import")
    @Log(title = "工单PDF导入", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/importPdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<WorkOrderImportResultVo> importPdf(@RequestPart("file") MultipartFile file) {
        return R.ok(workOrderService.importPdf(file));
    }

    @SaCheckPermission("department:workOrder:export")
    @Log(title = "工单台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(WorkOrderQueryBo bo, HttpServletResponse response) {
        List<WorkOrderExportVo> list = workOrderService.queryExportList(bo);
        ExcelBuilder.of(list, WorkOrderExportVo.class).sheetName("人工单明细台账").toResponse(response);
    }

    @SaCheckPermission("department:workOrder:query")
    @GetMapping("/summary")
    public R<WorkOrderSummaryVo> summary(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginDate,
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return R.ok(workOrderService.buildSummary(beginDate, endDate, true));
    }
}
