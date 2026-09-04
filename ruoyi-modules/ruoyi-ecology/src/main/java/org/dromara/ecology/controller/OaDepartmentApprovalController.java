package org.dromara.ecology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.ecology.domain.bo.OaDepartmentApprovalBo;
import org.dromara.ecology.domain.vo.OaDepartmentApprovalVo;
import org.dromara.ecology.service.IOaApplicationService;
import org.dromara.ecology.service.IOaDepartmentApprovalService;
import org.dromara.system.domain.vo.SysDeptVo;
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

import java.util.List;

/** 泛微审批方案接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ecology/approval-plan")
public class OaDepartmentApprovalController extends BaseController {

    private final IOaDepartmentApprovalService service;
    private final IOaApplicationService applicationService;

    @SaCheckPermission("ecology:departmentApproval:list")
    @GetMapping("/list")
    public R<List<OaDepartmentApprovalVo>> list(Long workflowConfigId, String businessType, String sourceModule,
                                                 Long businessDeptId,
                                                 @RequestParam(defaultValue = "false") boolean enabledOnly) {
        return R.ok(service.queryList(workflowConfigId, businessType, sourceModule, businessDeptId, enabledOnly));
    }

    @SaCheckPermission("ecology:departmentApproval:list")
    @GetMapping("/organizations")
    public R<List<SysDeptVo>> organizations(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) List<Long> deptIds,
                                            @RequestParam(required = false) Long parentId) {
        return R.ok(applicationService.queryOaDepartments(keyword, deptIds, parentId));
    }

    @SaCheckPermission("ecology:departmentApproval:query")
    @GetMapping("/{id}")
    public R<OaDepartmentApprovalVo> getInfo(
        @NotNull(message = "审批方案主键不能为空") @PathVariable Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("ecology:departmentApproval:add")
    @Log(title = "泛微审批方案", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody OaDepartmentApprovalBo bo) {
        return toAjax(service.insertByBo(bo));
    }

    @SaCheckPermission("ecology:departmentApproval:edit")
    @Log(title = "泛微审批方案", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody OaDepartmentApprovalBo bo) {
        return toAjax(service.updateByBo(bo));
    }

    @SaCheckPermission("ecology:departmentApproval:remove")
    @Log(title = "泛微审批方案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "审批方案主键不能为空") @PathVariable Long id) {
        return toAjax(service.deleteById(id));
    }
}
