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
import org.dromara.ecology.domain.bo.OaFormWorkflowBo;
import org.dromara.ecology.domain.bo.OaWorkflowOptionBo;
import org.dromara.ecology.domain.vo.OaFormWorkflowVo;
import org.dromara.ecology.domain.vo.OaWorkflowConfigVo;
import org.dromara.ecology.domain.vo.OaWorkflowOptionVo;
import org.dromara.ecology.service.IOaWorkflowConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 泛微表单与审批方式配置接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ecology/workflow-config")
public class OaWorkflowConfigController extends BaseController {

    private final IOaWorkflowConfigService service;

    /** 提交页面使用的通用审批方式选项列表；表单由业务绑定解析。 */
    @SaCheckPermission("ecology:workflowConfig:list")
    @GetMapping("/list")
    public R<List<OaWorkflowConfigVo>> list(String businessType, Boolean enabledOnly) {
        return R.ok(service.queryList(businessType, Boolean.TRUE.equals(enabledOnly)));
    }

    /** 管理员维护的表单主配置列表。 */
    @SaCheckPermission("ecology:workflowConfig:list")
    @GetMapping("/forms")
    public R<List<OaFormWorkflowVo>> forms(Boolean enabledOnly) {
        return R.ok(service.queryFormList(Boolean.TRUE.equals(enabledOnly)));
    }

    @SaCheckPermission("ecology:workflowConfig:query")
    @GetMapping("/form/{id}")
    public R<OaFormWorkflowVo> getForm(@NotNull(message = "表单配置主键不能为空") @PathVariable Long id) {
        return R.ok(service.queryFormById(id));
    }

    @SaCheckPermission("ecology:workflowConfig:list")
    @GetMapping("/options")
    public R<List<OaWorkflowOptionVo>> options(Boolean enabledOnly) {
        return R.ok(service.queryOptions(Boolean.TRUE.equals(enabledOnly)));
    }

    /** 兼容提交、审批方案和导入配置页面按审批选项 ID 查询的接口。 */
    @SaCheckPermission("ecology:workflowConfig:query")
    @GetMapping("/{id}")
    public R<OaWorkflowConfigVo> getOption(@NotNull(message = "审批方式主键不能为空") @PathVariable Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("ecology:workflowConfig:add")
    @Log(title = "泛微表单配置", businessType = BusinessType.INSERT)
    @PostMapping("/form")
    public R<Void> addForm(@Validated(AddGroup.class) @RequestBody OaFormWorkflowBo bo) {
        return toAjax(service.insertFormByBo(bo));
    }

    @SaCheckPermission("ecology:workflowConfig:edit")
    @Log(title = "泛微表单配置", businessType = BusinessType.UPDATE)
    @PutMapping("/form")
    public R<Void> editForm(@Validated(EditGroup.class) @RequestBody OaFormWorkflowBo bo) {
        return toAjax(service.updateFormByBo(bo));
    }

    @SaCheckPermission("ecology:workflowConfig:add")
    @Log(title = "泛微审批方式配置", businessType = BusinessType.INSERT)
    @PostMapping("/option")
    public R<Void> addOption(@Validated(AddGroup.class) @RequestBody OaWorkflowOptionBo bo) {
        return toAjax(service.insertOptionByBo(bo));
    }

    @SaCheckPermission("ecology:workflowConfig:edit")
    @Log(title = "泛微审批方式配置", businessType = BusinessType.UPDATE)
    @PutMapping("/option")
    public R<Void> editOption(@Validated(EditGroup.class) @RequestBody OaWorkflowOptionBo bo) {
        return toAjax(service.updateOptionByBo(bo));
    }

    @SaCheckPermission("ecology:workflowConfig:remove")
    @Log(title = "泛微审批方式配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/option/{id}")
    public R<Void> removeOption(@NotNull(message = "审批方式主键不能为空") @PathVariable Long id) {
        return toAjax(service.deleteOptionById(id));
    }

    @SaCheckPermission("ecology:workflowConfig:remove")
    @Log(title = "泛微表单配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/form/{id}")
    public R<Void> removeForm(@NotNull(message = "表单配置主键不能为空") @PathVariable Long id) {
        return toAjax(service.deleteFormById(id));
    }
}
