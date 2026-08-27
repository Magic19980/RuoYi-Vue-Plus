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
import org.dromara.department.domain.bo.DailyReportBo;
import org.dromara.department.domain.bo.DailyReportQueryBo;
import org.dromara.department.domain.vo.DailyReportImportVo;
import org.dromara.department.domain.vo.DailyReportVo;
import org.dromara.department.service.IDailyReportService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 科室日报接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/dailyReport")
public class DailyReportController extends BaseController {

    private final IDailyReportService dailyReportService;

    @SaCheckPermission("department:dailyReport:list")
    @GetMapping("/list")
    public R<PageResult<DailyReportVo>> list(DailyReportQueryBo bo, PageQuery pageQuery) {
        return R.ok(dailyReportService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:dailyReport:query")
    @GetMapping("/{id}")
    public R<DailyReportVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(dailyReportService.queryById(id));
    }

    @SaCheckPermission("department:dailyReport:add")
    @Log(title = "科室日报", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DailyReportBo bo) {
        return toAjax(dailyReportService.insertByBo(bo));
    }

    @SaCheckPermission("department:dailyReport:edit")
    @Log(title = "科室日报", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DailyReportBo bo) {
        return toAjax(dailyReportService.updateByBo(bo));
    }

    @SaCheckPermission("department:dailyReport:remove")
    @Log(title = "科室日报", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(dailyReportService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @SaCheckPermission("department:dailyReport:import")
    @Log(title = "科室日报", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> importData(@RequestPart("file") MultipartFile file) throws Exception {
        ExcelResult<DailyReportImportVo> result = ExcelBuilder.read(file.getInputStream(), DailyReportImportVo.class)
            .validate(true)
            .doRead();
        String message = dailyReportService.importData(result.getList());
        return R.ok(message + "。" + result.getAnalysis());
    }

    @SaCheckPermission("department:dailyReport:export")
    @Log(title = "科室日报", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DailyReportQueryBo bo, HttpServletResponse response) {
        dailyReportService.exportXlsx(bo, response);
    }

    @SaCheckPermission("department:dailyReport:import")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        dailyReportService.exportTemplate(response);
    }
}
