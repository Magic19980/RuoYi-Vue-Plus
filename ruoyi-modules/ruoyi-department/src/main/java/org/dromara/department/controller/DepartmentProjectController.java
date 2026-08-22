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
import org.dromara.department.domain.bo.DepartmentProjectBo;
import org.dromara.department.domain.bo.DepartmentProjectQueryBo;
import org.dromara.department.domain.vo.DepartmentProjectVo;
import org.dromara.department.service.IDepartmentProjectService;
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

/**
 * 科室项目主数据接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/project")
public class DepartmentProjectController extends BaseController {

    private final IDepartmentProjectService departmentProjectService;

    @SaCheckPermission("department:project:list")
    @GetMapping("/list")
    public R<PageResult<DepartmentProjectVo>> list(DepartmentProjectQueryBo bo, PageQuery pageQuery) {
        return R.ok(departmentProjectService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:project:list")
    @GetMapping("/options")
    public R<List<DepartmentProjectVo>> options() {
        return R.ok(departmentProjectService.queryOptions());
    }

    @SaCheckPermission("department:project:query")
    @GetMapping("/{id}")
    public R<DepartmentProjectVo> getInfo(@NotNull(message = "项目主键不能为空") @PathVariable Long id) {
        return R.ok(departmentProjectService.queryById(id));
    }

    @SaCheckPermission("department:project:add")
    @Log(title = "科室项目", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DepartmentProjectBo bo) {
        return toAjax(departmentProjectService.insertByBo(bo));
    }

    @SaCheckPermission("department:project:edit")
    @Log(title = "科室项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DepartmentProjectBo bo) {
        return toAjax(departmentProjectService.updateByBo(bo));
    }

    @SaCheckPermission("department:project:remove")
    @Log(title = "科室项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "项目主键不能为空") @PathVariable Long[] ids) {
        return toAjax(departmentProjectService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
