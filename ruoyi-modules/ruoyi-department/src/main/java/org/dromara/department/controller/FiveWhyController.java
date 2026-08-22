package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
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
import org.dromara.department.domain.bo.FiveWhyBo;
import org.dromara.department.domain.bo.FiveWhyQueryBo;
import org.dromara.department.domain.bo.FiveWhyReviewBo;
import org.dromara.department.domain.vo.FiveWhyVo;
import org.dromara.department.service.IFiveWhyService;
import org.dromara.system.domain.vo.SysOssVo;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/** 5WHY分析接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/fiveWhy")
public class FiveWhyController extends BaseController {

    private final IFiveWhyService fiveWhyService;

    @SaCheckPermission("department:fiveWhy:list")
    @GetMapping("/list")
    public R<PageResult<FiveWhyVo>> list(FiveWhyQueryBo bo, PageQuery pageQuery) {
        return R.ok(fiveWhyService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:fiveWhy:query")
    @GetMapping("/{id}")
    public R<FiveWhyVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(fiveWhyService.queryById(id));
    }

    @SaCheckPermission("department:fiveWhy:add")
    @Log(title = "5WHY分析", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody FiveWhyBo bo) {
        return toAjax(fiveWhyService.insertByBo(bo));
    }

    @SaCheckPermission("department:fiveWhy:edit")
    @Log(title = "5WHY分析", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody FiveWhyBo bo) {
        return toAjax(fiveWhyService.updateByBo(bo));
    }

    @SaCheckPermission("department:fiveWhy:remove")
    @Log(title = "5WHY分析", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(fiveWhyService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @SaCheckPermission("department:fiveWhy:review")
    @Log(title = "5WHY分析审核", businessType = BusinessType.UPDATE)
    @PostMapping("/review")
    public R<Void> review(@Validated @RequestBody FiveWhyReviewBo bo) {
        return toAjax(fiveWhyService.review(bo));
    }

    @SaCheckPermission("department:fiveWhy:edit")
    @PostMapping(value = "/uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssVo> uploadImage(@RequestPart("file") MultipartFile file) {
        return R.ok(fiveWhyService.uploadImage(file));
    }

    @SaCheckPermission("department:fiveWhy:export")
    @Log(title = "5WHY分析", businessType = BusinessType.EXPORT)
    @PostMapping("/export/{id}")
    public void export(@NotNull(message = "主键不能为空") @PathVariable Long id, HttpServletResponse response) throws Exception {
        fiveWhyService.exportDocx(id, response);
    }
}
