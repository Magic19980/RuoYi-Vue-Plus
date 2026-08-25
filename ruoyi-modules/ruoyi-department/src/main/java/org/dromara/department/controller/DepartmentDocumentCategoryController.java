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
import org.dromara.department.domain.bo.DepartmentDocumentCategoryBo;
import org.dromara.department.domain.bo.DepartmentDocumentCategoryQueryBo;
import org.dromara.department.domain.vo.DepartmentDocumentCategoryVo;
import org.dromara.department.service.IDepartmentDocumentCategoryService;
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

/** 科室资料分类配置接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/documentCategory")
public class DepartmentDocumentCategoryController extends BaseController {

    private final IDepartmentDocumentCategoryService categoryService;

    @SaCheckPermission("department:documentCategory:list")
    @GetMapping("/list")
    public R<PageResult<DepartmentDocumentCategoryVo>> list(DepartmentDocumentCategoryQueryBo bo, PageQuery pageQuery) {
        return R.ok(categoryService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:documentCategory:list")
    @GetMapping("/options")
    public R<List<DepartmentDocumentCategoryVo>> options() {
        return R.ok(categoryService.queryOptions());
    }

    @SaCheckPermission("department:documentCategory:list")
    @GetMapping("/tree")
    public R<List<DepartmentDocumentCategoryVo>> tree(DepartmentDocumentCategoryQueryBo bo) {
        return R.ok(categoryService.queryTreeList(bo));
    }

    @SaCheckPermission("department:documentCategory:query")
    @GetMapping("/{id}")
    public R<DepartmentDocumentCategoryVo> getInfo(@NotNull(message = "分类主键不能为空") @PathVariable Long id) {
        return R.ok(categoryService.queryById(id));
    }

    @SaCheckPermission("department:documentCategory:add")
    @Log(title = "资料分类", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DepartmentDocumentCategoryBo bo) {
        return toAjax(categoryService.insertByBo(bo));
    }

    @SaCheckPermission("department:documentCategory:edit")
    @Log(title = "资料分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DepartmentDocumentCategoryBo bo) {
        return toAjax(categoryService.updateByBo(bo));
    }

    @SaCheckPermission("department:documentCategory:remove")
    @Log(title = "资料分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "分类主键不能为空") @PathVariable Long[] ids) {
        return toAjax(categoryService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
