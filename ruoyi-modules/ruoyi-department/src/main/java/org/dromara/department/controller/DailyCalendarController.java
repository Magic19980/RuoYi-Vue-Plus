package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.DailyCalendarOverrideBo;
import org.dromara.department.domain.vo.DailyCalendarOverrideVo;
import org.dromara.department.domain.vo.DailyCalendarVo;
import org.dromara.department.service.IDailyCalendarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** 科室日报日历接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/dailyReport/calendar")
public class DailyCalendarController extends BaseController {

    private final IDailyCalendarService dailyCalendarService;

    @SaCheckPermission("department:dailyReport:query")
    @GetMapping
    public R<DailyCalendarVo> calendar(@DateTimeFormat(pattern = "yyyy-MM-dd")
                                       @RequestParam(required = false) LocalDate month) {
        return R.ok(dailyCalendarService.queryCalendar(month));
    }

    @SaCheckPermission("department:dailyReport:query")
    @GetMapping("/override/list")
    public R<List<DailyCalendarOverrideVo>> overrideList(
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam LocalDate beginDate,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam LocalDate endDate) {
        return R.ok(dailyCalendarService.queryOverrides(beginDate, endDate));
    }

    @SaCheckPermission("department:dailyReport:edit")
    @Log(title = "日报日期例外规则", businessType = BusinessType.INSERT)
    @PostMapping("/override")
    public R<Void> addOverride(@Validated(AddGroup.class) @RequestBody DailyCalendarOverrideBo bo) {
        return toAjax(dailyCalendarService.saveOverride(bo));
    }

    @SaCheckPermission("department:dailyReport:edit")
    @Log(title = "日报日期例外规则", businessType = BusinessType.UPDATE)
    @PutMapping("/override")
    public R<Void> editOverride(@Validated(EditGroup.class) @RequestBody DailyCalendarOverrideBo bo) {
        return toAjax(dailyCalendarService.saveOverride(bo));
    }

    @SaCheckPermission("department:dailyReport:remove")
    @Log(title = "日报日期例外规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/override/{ids}")
    public R<Void> removeOverride(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(dailyCalendarService.deleteOverrides(Arrays.asList(ids)));
    }

}
