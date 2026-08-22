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
import org.dromara.common.excel.core.ExcelResult;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.OperationRecordBo;
import org.dromara.department.domain.bo.OperationRecordQueryBo;
import org.dromara.department.domain.bo.OperationSystemBo;
import org.dromara.department.domain.vo.OperationRecordImportVo;
import org.dromara.department.domain.vo.OperationRecordVo;
import org.dromara.department.domain.vo.OperationSummaryVo;
import org.dromara.department.domain.vo.OperationSystemImportVo;
import org.dromara.department.domain.vo.OperationSystemVo;
import org.dromara.department.service.IOperationLedgerService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 运维台账接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/operationLedger")
public class OperationLedgerController extends BaseController {

    private final IOperationLedgerService operationLedgerService;

    @SaCheckPermission("department:operationLedger:list")
    @GetMapping("/list")
    public R<PageResult<OperationRecordVo>> list(OperationRecordQueryBo bo, PageQuery pageQuery) {
        return R.ok(operationLedgerService.queryRecordPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:operationLedger:query")
    @GetMapping("/{id}")
    public R<OperationRecordVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(operationLedgerService.queryRecordById(id));
    }

    @SaCheckPermission("department:operationLedger:add")
    @Log(title = "运维工作记录", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody OperationRecordBo bo) {
        return toAjax(operationLedgerService.insertRecord(bo));
    }

    @SaCheckPermission("department:operationLedger:edit")
    @Log(title = "运维工作记录", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody OperationRecordBo bo) {
        return toAjax(operationLedgerService.updateRecord(bo));
    }

    @SaCheckPermission("department:operationLedger:remove")
    @Log(title = "运维工作记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(operationLedgerService.deleteRecords(Arrays.asList(ids)));
    }

    @SaCheckPermission("department:operationLedger:import")
    @Log(title = "运维工作记录", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> importData(@RequestPart("file") MultipartFile file) throws Exception {
        ExcelResult<OperationRecordImportVo> result = ExcelBuilder.read(file.getInputStream(), OperationRecordImportVo.class)
            .sheetName("工作记录")
            .validate(true)
            .doRead();
        return R.ok(operationLedgerService.importRecords(result.getList()) + "。" + result.getAnalysis());
    }

    @SaCheckPermission("department:operationLedger:import")
    @Log(title = "系统在线率", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/importSystemData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> importSystemData(@RequestPart("file") MultipartFile file) throws Exception {
        ExcelResult<OperationSystemImportVo> result = ExcelBuilder.read(file.getInputStream(), OperationSystemImportVo.class)
            .sheetName("系统运维报告")
            .validate(true)
            .doRead();
        return R.ok(operationLedgerService.importSystems(result.getList()) + "。" + result.getAnalysis());
    }

    @SaCheckPermission("department:operationLedger:export")
    @Log(title = "运维工作记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(OperationRecordQueryBo bo, HttpServletResponse response) {
        List<OperationRecordVo> list = operationLedgerService.queryRecordList(bo);
        ExcelBuilder.of(list, OperationRecordVo.class).sheetName("运维工作记录").toResponse(response);
    }

    @SaCheckPermission("department:operationLedger:export")
    @PostMapping("/exportSystems")
    public void exportSystems(@RequestParam(required = false) LocalDate beginDate,
                              @RequestParam(required = false) LocalDate endDate,
                              @RequestParam(required = false) String systemName,
                              HttpServletResponse response) {
        List<OperationSystemVo> list = operationLedgerService.querySystemList(beginDate, endDate, systemName);
        ExcelBuilder.of(list, OperationSystemVo.class).sheetName("系统在线率").toResponse(response);
    }

    @SaCheckPermission("department:operationLedger:import")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelBuilder.of(new ArrayList<>(), OperationRecordImportVo.class).sheetName("工作记录").toResponse(response);
    }

    @SaCheckPermission("department:operationLedger:import")
    @PostMapping("/importSystemTemplate")
    public void importSystemTemplate(HttpServletResponse response) {
        ExcelBuilder.of(new ArrayList<>(), OperationSystemImportVo.class).sheetName("系统运维报告").toResponse(response);
    }

    @SaCheckPermission("department:operationLedger:list")
    @GetMapping("/system/list")
    public R<PageResult<OperationSystemVo>> systemList(@RequestParam(required = false) LocalDate beginDate,
                                                       @RequestParam(required = false) LocalDate endDate,
                                                       @RequestParam(required = false) String systemName,
                                                       PageQuery pageQuery) {
        return R.ok(operationLedgerService.querySystemPageList(beginDate, endDate, systemName, pageQuery));
    }

    @SaCheckPermission("department:operationLedger:query")
    @GetMapping("/system/{id}")
    public R<OperationSystemVo> systemInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(operationLedgerService.querySystemById(id));
    }

    @SaCheckPermission("department:operationLedger:add")
    @Log(title = "系统在线率", businessType = BusinessType.INSERT)
    @PostMapping("/system")
    public R<Void> addSystem(@Validated(AddGroup.class) @RequestBody OperationSystemBo bo) {
        return toAjax(operationLedgerService.insertSystem(bo));
    }

    @SaCheckPermission("department:operationLedger:edit")
    @Log(title = "系统在线率", businessType = BusinessType.UPDATE)
    @PutMapping("/system")
    public R<Void> editSystem(@Validated(EditGroup.class) @RequestBody OperationSystemBo bo) {
        return toAjax(operationLedgerService.updateSystem(bo));
    }

    @SaCheckPermission("department:operationLedger:remove")
    @Log(title = "系统在线率", businessType = BusinessType.DELETE)
    @DeleteMapping("/system/{ids}")
    public R<Void> removeSystem(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(operationLedgerService.deleteSystems(Arrays.asList(ids)));
    }

    @SaCheckPermission("department:operationLedger:list")
    @GetMapping("/summary")
    public R<OperationSummaryVo> summary(@RequestParam LocalDate beginDate, @RequestParam LocalDate endDate) {
        return R.ok(operationLedgerService.buildSummary(beginDate, endDate));
    }
}
