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
import org.dromara.ecology.domain.bo.OaBusinessTypeBo;
import org.dromara.ecology.domain.vo.OaBusinessTypeVo;
import org.dromara.ecology.service.IOaBusinessTypeService;
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

/** 泛微审批业务类型配置接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ecology/business-type")
public class OaBusinessTypeController extends BaseController {

    private final IOaBusinessTypeService service;

    @SaCheckPermission("ecology:businessType:list")
    @GetMapping("/list")
    public R<List<OaBusinessTypeVo>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "false") boolean enabledOnly) {
        return R.ok(service.queryList(keyword, enabledOnly));
    }

    @SaCheckPermission("ecology:businessType:query")
    @GetMapping("/{id}")
    public R<OaBusinessTypeVo> getInfo(@NotNull(message = "业务类型主键不能为空") @PathVariable Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("ecology:businessType:add")
    @Log(title = "泛微审批业务类型", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody OaBusinessTypeBo bo) {
        return toAjax(service.insertByBo(bo));
    }

    @SaCheckPermission("ecology:businessType:edit")
    @Log(title = "泛微审批业务类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody OaBusinessTypeBo bo) {
        return toAjax(service.updateByBo(bo));
    }

    @SaCheckPermission("ecology:businessType:remove")
    @Log(title = "泛微审批业务类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "业务类型主键不能为空") @PathVariable Long id) {
        return toAjax(service.disableById(id));
    }
}
