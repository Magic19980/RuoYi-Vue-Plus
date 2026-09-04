package org.dromara.ecology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.ecology.domain.bo.OaBusinessWorkflowBindingBo;
import org.dromara.ecology.domain.vo.OaBusinessWorkflowBindingVo;
import org.dromara.ecology.service.IOaBusinessWorkflowService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ecology/business-type")
public class OaBusinessWorkflowController extends BaseController {

    private final IOaBusinessWorkflowService service;

    @SaCheckPermission("ecology:businessType:list")
    @GetMapping("/workflow-bindings")
    public R<List<OaBusinessWorkflowBindingVo>> list() {
        return R.ok(service.queryList());
    }

    @SaCheckPermission("ecology:businessType:query")
    @GetMapping("/{businessType}/workflow-binding")
    public R<OaBusinessWorkflowBindingVo> get(@PathVariable @NotBlank String businessType) {
        return R.ok(service.queryByBusinessType(businessType));
    }

    @SaCheckPermission("ecology:businessType:edit")
    @PutMapping("/{businessType}/workflow-binding")
    public R<Void> save(@PathVariable @NotBlank String businessType,
                        @Valid @RequestBody OaBusinessWorkflowBindingBo bo) {
        return toAjax(service.save(businessType, bo));
    }

    @SaCheckPermission("ecology:businessType:edit")
    @DeleteMapping("/{businessType}/workflow-binding")
    public R<Void> delete(@PathVariable @NotBlank String businessType) {
        return toAjax(service.delete(businessType));
    }
}
