package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.WeeklyReportBo;
import org.dromara.department.domain.bo.WeeklyReportQueryBo;
import org.dromara.department.domain.vo.WeeklyReportSummaryVo;
import org.dromara.department.domain.vo.WeeklyReportVo;
import org.dromara.department.service.IWeeklyReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 科室周报接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/weeklyReport")
public class WeeklyReportController extends BaseController {

    private final IWeeklyReportService weeklyReportService;

    @SaCheckPermission("department:weeklyReport:list")
    @GetMapping("/list")
    public R<PageResult<WeeklyReportVo>> list(WeeklyReportQueryBo bo, PageQuery pageQuery) {
        return R.ok(weeklyReportService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:weeklyReport:summary")
    @GetMapping("/summary")
    public R<WeeklyReportSummaryVo> summary(@RequestParam(required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStart) {
        WeeklyReportBo bo = new WeeklyReportBo();
        bo.setWeekStart(weekStart == null ? LocalDate.now() : weekStart);
        return R.ok(weeklyReportService.buildSummary(bo));
    }

    @SaCheckPermission("department:weeklyReport:add")
    @Log(title = "科室周报", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public R<WeeklyReportVo> generate(@Validated(AddGroup.class) @RequestBody WeeklyReportBo bo) {
        return R.ok(weeklyReportService.generate(bo));
    }

    @SaCheckPermission("department:weeklyReport:query")
    @GetMapping("/{id}")
    public R<WeeklyReportVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(weeklyReportService.queryById(id));
    }

    @SaCheckPermission("department:weeklyReport:export")
    @GetMapping("/export/{id}")
    public void export(@NotNull(message = "主键不能为空") @PathVariable Long id, HttpServletResponse response) throws Exception {
        weeklyReportService.exportPptx(id, response);
    }
}
