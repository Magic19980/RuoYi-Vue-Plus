package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.ScoreCategoryBo;
import org.dromara.department.domain.vo.ScoreCategoryVo;
import org.dromara.department.service.IScoreCategoryService;
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

import java.util.Arrays;
import java.util.List;

/** SCORE 提案分类配置接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/scoreCategory")
public class ScoreCategoryController extends BaseController {

    private final IScoreCategoryService scoreCategoryService;

    @SaCheckPermission("department:scoreCategory:list")
    @GetMapping("/list")
    public R<List<ScoreCategoryVo>> list(@RequestParam(value = "enabledOnly", required = false, defaultValue = "false") boolean enabledOnly) {
        return R.ok(scoreCategoryService.queryTree(enabledOnly));
    }

    /** 提案填写页使用的启用分类树。 */
    @SaCheckPermission("department:scoreProposal:list")
    @GetMapping("/options")
    public R<List<ScoreCategoryVo>> options() {
        return R.ok(scoreCategoryService.queryTree(true));
    }

    @SaCheckPermission("department:scoreCategory:list")
    @GetMapping("/{id}")
    public R<ScoreCategoryVo> getInfo(@NotNull(message = "分类主键不能为空") @PathVariable Long id) {
        return R.ok(scoreCategoryService.queryById(id));
    }

    @SaCheckPermission("department:scoreCategory:add")
    @Log(title = "SCORE分类", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ScoreCategoryBo bo) {
        return toAjax(scoreCategoryService.insertByBo(bo));
    }

    @SaCheckPermission("department:scoreCategory:edit")
    @Log(title = "SCORE分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ScoreCategoryBo bo) {
        return toAjax(scoreCategoryService.updateByBo(bo));
    }

    @SaCheckPermission("department:scoreCategory:remove")
    @Log(title = "SCORE分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "分类主键不能为空") @PathVariable Long[] ids) {
        return toAjax(scoreCategoryService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
