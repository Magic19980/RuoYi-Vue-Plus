package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
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
import org.dromara.department.domain.bo.DepartmentConfigBo;
import org.dromara.department.domain.bo.DepartmentConfigQueryBo;
import org.dromara.department.domain.vo.DepartmentConfigVo;
import org.dromara.department.service.IDepartmentConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** 业务科室配置接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/config")
public class DepartmentConfigController extends BaseController {

    private final IDepartmentConfigService departmentConfigService;

    @SaCheckPermission("department:department:list")
    @GetMapping("/list")
    public R<PageResult<DepartmentConfigVo>> list(DepartmentConfigQueryBo bo, PageQuery pageQuery) {
        return R.ok(departmentConfigService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:department:list")
    @GetMapping("/available")
    public R<List<DepartmentConfigVo>> available() {
        return R.ok(departmentConfigService.queryAvailableDepartments());
    }

    @SaCheckPermission("department:department:query")
    @GetMapping("/{deptId}")
    public R<DepartmentConfigVo> getInfo(@NotNull(message = "科室ID不能为空") @PathVariable Long deptId) {
        return R.ok(departmentConfigService.queryById(deptId));
    }

    @SaCheckPermission("department:department:add")
    @Log(title = "业务科室配置", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DepartmentConfigBo bo) {
        return toAjax(departmentConfigService.insertByBo(bo));
    }

    @SaCheckPermission("department:department:edit")
    @Log(title = "业务科室配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DepartmentConfigBo bo) {
        return toAjax(departmentConfigService.updateByBo(bo));
    }

    @SaCheckPermission("department:department:remove")
    @Log(title = "业务科室配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptIds}")
    public R<Void> remove(@NotEmpty(message = "科室ID不能为空") @PathVariable Long[] deptIds) {
        return toAjax(departmentConfigService.disableByIds(Arrays.asList(deptIds)));
    }
}
