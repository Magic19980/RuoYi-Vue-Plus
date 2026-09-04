package org.dromara.ecology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.ecology.domain.bo.OaApplicationBo;
import org.dromara.ecology.domain.bo.OaApplicationQueryBo;
import org.dromara.ecology.domain.vo.OaApplicationVo;
import org.dromara.ecology.domain.vo.OaApprovalRulePreviewVo;
import org.dromara.ecology.domain.vo.OaAttachmentPreviewVo;
import org.dromara.ecology.domain.vo.OaProcessEventLogVo;
import org.dromara.ecology.service.IOaApplicationService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 泛微通用审批申请接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ecology/application")
public class OaApplicationController extends BaseController {

    private final IOaApplicationService service;

    @SaCheckPermission("ecology:application:list")
    @GetMapping("/list")
    public R<PageResult<OaApplicationVo>> list(OaApplicationQueryBo bo, PageQuery pageQuery) {
        return R.ok(service.queryPage(bo, pageQuery));
    }

    @SaCheckPermission("ecology:application:query")
    @GetMapping("/{id}")
    public R<OaApplicationVo> getInfo(@NotNull(message = "申请主键不能为空") @PathVariable Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("ecology:application:query")
    @GetMapping("/attachment-preview/{ossId}")
    public R<OaAttachmentPreviewVo> previewAttachment(
        @NotNull(message = "附件主键不能为空") @PathVariable Long ossId) {
        return R.ok(service.previewAttachment(ossId));
    }

    @SaCheckPermission("ecology:application:query")
    @GetMapping("/attachment-download/{ossId}")
    public ResponseEntity<byte[]> downloadAttachment(
        @NotNull(message = "附件主键不能为空") @PathVariable Long ossId) {
        return service.downloadAttachment(ossId);
    }

    @SaCheckPermission("ecology:application:add")
    @GetMapping("/departments")
    public R<List<SysDeptVo>> departments(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) List<Long> deptIds,
                                          @RequestParam(required = false) Long parentId) {
        return R.ok(service.queryOaDepartments(keyword, deptIds, parentId));
    }

    @SaCheckPermission("ecology:application:add")
    @Log(title = "泛微通用审批", businessType = BusinessType.INSERT)
    @PostMapping
    public R<OaApplicationVo> add(@Validated(AddGroup.class) @RequestBody OaApplicationBo bo) {
        return R.ok(service.save(bo));
    }

    @SaCheckPermission("ecology:application:edit")
    @Log(title = "泛微通用审批", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<OaApplicationVo> edit(@Validated(EditGroup.class) @RequestBody OaApplicationBo bo) {
        return R.ok(service.save(bo));
    }

    @SaCheckPermission("ecology:application:submit")
    @Log(title = "泛微通用审批", businessType = BusinessType.OTHER)
    @PostMapping("/{id}/submit")
    public R<OaApplicationVo> submit(@NotNull(message = "申请主键不能为空") @PathVariable Long id) {
        return R.ok(service.submit(id));
    }

    @SaCheckPermission("ecology:application:sync")
    @Log(title = "泛微通用审批", businessType = BusinessType.OTHER)
    @PostMapping("/{id}/sync")
    public R<OaApplicationVo> sync(@NotNull(message = "申请主键不能为空") @PathVariable Long id) {
        return R.ok(service.sync(id));
    }

    @SaCheckPermission("ecology:application:preview")
    @GetMapping("/{id}/participants/preview")
    public R<List<OaApprovalRulePreviewVo>> previewParticipants(
        @NotNull(message = "申请主键不能为空") @PathVariable Long id) {
        return R.ok(service.previewParticipants(id));
    }

    @SaCheckPermission("ecology:application:reconcile")
    @Log(title = "泛微审批对账", businessType = BusinessType.OTHER)
    @PostMapping("/reconcile")
    public R<Void> reconcile() {
        service.reconcileDue();
        return R.ok();
    }

    @SaCheckPermission("ecology:application:query")
    @GetMapping("/{id}/events")
    public R<List<OaProcessEventLogVo>> events(@NotNull(message = "申请主键不能为空") @PathVariable Long id) {
        return R.ok(service.queryEvents(id));
    }
}
