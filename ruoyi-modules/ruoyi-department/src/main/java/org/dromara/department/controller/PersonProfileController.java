package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.core.ExcelResult;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.PersonProfileBo;
import org.dromara.department.domain.bo.PersonProfileQueryBo;
import org.dromara.department.domain.vo.PersonProfileVo;
import org.dromara.department.domain.vo.PersonProfileImportVo;
import org.dromara.department.domain.vo.PersonUserOptionVo;
import org.dromara.department.service.IPersonProfileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
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
 * 科室人员业务档案接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/person")
public class PersonProfileController extends BaseController {

    private final IPersonProfileService personProfileService;

    @SaCheckPermission("department:person:list")
    @GetMapping("/list")
    public R<PageResult<PersonProfileVo>> list(PersonProfileQueryBo bo, PageQuery pageQuery) {
        return R.ok(personProfileService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:person:query")
    @GetMapping("/{id}")
    public R<PersonProfileVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(personProfileService.queryById(id));
    }

    @SaCheckPermission("department:person:query")
    @GetMapping("/userOptions")
    public R<List<PersonUserOptionVo>> userOptions() {
        return R.ok(personProfileService.queryUserOptions());
    }

    @SaCheckPermission("department:person:add")
    @Log(title = "科室人员档案", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PersonProfileBo bo) {
        return toAjax(personProfileService.insertByBo(bo));
    }

    @SaCheckPermission("department:person:edit")
    @Log(title = "科室人员档案", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PersonProfileBo bo) {
        return toAjax(personProfileService.updateByBo(bo));
    }

    @SaCheckPermission("department:person:remove")
    @Log(title = "科室人员档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(personProfileService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @SaCheckPermission("department:person:import")
    @Log(title = "科室人员档案", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> importData(@RequestPart("file") MultipartFile file) throws Exception {
        ExcelResult<PersonProfileImportVo> result = ExcelBuilder.read(file.getInputStream(), PersonProfileImportVo.class)
            .validate(true)
            .doRead();
        return R.ok(personProfileService.importData(result.getList()) + "。" + result.getAnalysis());
    }

    @SaCheckPermission("department:person:export")
    @Log(title = "科室人员档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PersonProfileQueryBo bo, HttpServletResponse response) {
        ExcelBuilder.of(personProfileService.queryList(bo), PersonProfileVo.class).sheetName("人员档案").toResponse(response);
    }

    @SaCheckPermission("department:person:import")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelBuilder.of(List.of(), PersonProfileImportVo.class).sheetName("人员档案导入模板").toResponse(response);
    }
}
