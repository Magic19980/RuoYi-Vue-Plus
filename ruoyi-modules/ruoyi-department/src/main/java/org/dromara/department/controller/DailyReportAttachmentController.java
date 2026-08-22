package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.vo.DailyReportAttachmentVo;
import org.dromara.department.service.IDailyReportAttachmentService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 日报附件归档接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/dailyReport/attachment")
public class DailyReportAttachmentController extends BaseController {

    private final IDailyReportAttachmentService attachmentService;

    @SaCheckPermission("department:dailyReport:query")
    @GetMapping("/list/{reportId}")
    public R<List<DailyReportAttachmentVo>> list(@NotNull @PathVariable Long reportId) {
        return R.ok(attachmentService.listByReportId(reportId));
    }

    @SaCheckPermission("department:dailyReport:edit")
    @Log(title = "日报附件", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload/{reportId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<DailyReportAttachmentVo> upload(@NotNull @PathVariable Long reportId, @RequestPart("file") MultipartFile file) {
        return R.ok(attachmentService.upload(reportId, file));
    }

    @SaCheckPermission("department:dailyReport:edit")
    @Log(title = "日报附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull @PathVariable Long id) {
        return toAjax(attachmentService.remove(id));
    }
}
