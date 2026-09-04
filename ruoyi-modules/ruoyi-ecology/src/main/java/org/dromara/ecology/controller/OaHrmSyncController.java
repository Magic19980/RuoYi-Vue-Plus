package org.dromara.ecology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.ecology.domain.vo.OaOrganizationTreeVo;
import org.dromara.ecology.domain.bo.OaHrmUserPasswordBo;
import org.dromara.ecology.domain.vo.OaHrmUserPasswordVo;
import org.dromara.ecology.domain.vo.OaSyncBatchVo;
import org.dromara.ecology.domain.vo.OaSyncDetailVo;
import org.dromara.ecology.domain.vo.OaSyncResultVo;
import org.dromara.ecology.service.IOaHrmSyncService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 泛微 HRM 组织与人员同步接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ecology/hrm-sync")
public class OaHrmSyncController extends BaseController {

    private final IOaHrmSyncService service;

    @SaCheckPermission("ecology:hrmSync:organization")
    @PostMapping("/organization")
    public R<OaSyncResultVo> syncOrganization(
        @RequestParam(defaultValue = "true") boolean full) {
        return R.ok(service.syncOrganization(full));
    }

    @SaCheckPermission("ecology:hrmSync:user")
    @PostMapping("/users")
    public R<OaSyncResultVo> syncUsers(
        @RequestParam(defaultValue = "true") boolean full) {
        return R.ok(service.syncUsers(full));
    }

    @SaCheckPermission("ecology:hrmSync:user")
    @GetMapping("/user-password/status")
    public R<OaHrmUserPasswordVo> userPasswordStatus() {
        return R.ok(service.queryUserPasswordStatus());
    }

    @SaCheckPermission("ecology:hrmSync:user")
    @PutMapping("/user-password")
    public R<Void> updateUserPassword(@Validated @RequestBody OaHrmUserPasswordBo bo) {
        service.updateUserPassword(bo);
        return R.ok();
    }

    @SaCheckPermission("ecology:hrmSync:list")
    @GetMapping("/organization-tree")
    public R<List<OaOrganizationTreeVo>> organizationTree(
        @RequestParam(defaultValue = "false") boolean includeDisabled,
        String subcompanyId) {
        return R.ok(service.queryOrganizationTree(includeDisabled, subcompanyId));
    }

    @SaCheckPermission("ecology:hrmSync:batch:list")
    @GetMapping("/batches")
    public R<PageResult<OaSyncBatchVo>> batches(String syncType, PageQuery pageQuery) {
        return R.ok(service.queryBatches(syncType, pageQuery));
    }

    @SaCheckPermission("ecology:hrmSync:detail:list")
    @GetMapping("/details")
    public R<PageResult<OaSyncDetailVo>> details(Long batchId, String detailStatus, PageQuery pageQuery) {
        return R.ok(service.queryDetails(batchId, detailStatus, pageQuery));
    }

}
