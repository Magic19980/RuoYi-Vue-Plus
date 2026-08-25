package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.DepartmentDocumentBo;
import org.dromara.department.domain.bo.DepartmentDocumentQueryBo;
import org.dromara.department.domain.vo.DepartmentDocumentVersionVo;
import org.dromara.department.domain.vo.DepartmentDocumentVo;
import org.dromara.department.service.IDepartmentDocumentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 科室资料管理接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/document")
public class DepartmentDocumentController extends BaseController {

    private final IDepartmentDocumentService documentService;

    @SaCheckPermission("department:document:list")
    @GetMapping("/list")
    public R<PageResult<DepartmentDocumentVo>> list(DepartmentDocumentQueryBo bo, PageQuery pageQuery) {
        return R.ok(documentService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:document:list")
    @GetMapping("/recycle/list")
    public R<PageResult<DepartmentDocumentVo>> recycleList(DepartmentDocumentQueryBo bo, PageQuery pageQuery) {
        return R.ok(documentService.queryRecyclePageList(bo, pageQuery));
    }

    @SaCheckPermission("department:document:query")
    @GetMapping("/{id}")
    public R<DepartmentDocumentVo> getInfo(@NotNull(message = "资料主键不能为空") @PathVariable Long id) {
        return R.ok(documentService.queryById(id));
    }

    @SaCheckPermission("department:document:add")
    @Log(title = "科室资料", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<DepartmentDocumentVo> upload(
        @RequestParam String title,
        @RequestParam Long categoryId,
        @RequestParam(required = false) Long projectId,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) String tags,
        @RequestParam(required = false) String visibility,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate expireDate,
        @RequestPart("file") MultipartFile file) {
        DepartmentDocumentBo bo = new DepartmentDocumentBo();
        bo.setTitle(title);
        bo.setCategoryId(categoryId);
        bo.setProjectId(projectId);
        bo.setDescription(description);
        bo.setTags(tags);
        bo.setVisibility(visibility);
        bo.setStatus(status);
        bo.setExpireDate(expireDate);
        return R.ok(documentService.upload(bo, file));
    }

    @SaCheckPermission("department:document:edit")
    @Log(title = "科室资料", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DepartmentDocumentBo bo) {
        return toAjax(documentService.updateByBo(bo));
    }

    @SaCheckPermission("department:document:edit")
    @Log(title = "科室资料版本", businessType = BusinessType.INSERT)
    @PostMapping(value = "/version/{documentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<DepartmentDocumentVersionVo> uploadVersion(
        @NotNull @PathVariable Long documentId,
        @RequestParam(required = false) String versionNote,
        @RequestPart("file") MultipartFile file) {
        return R.ok(documentService.uploadVersion(documentId, versionNote, file));
    }

    @SaCheckPermission("department:document:query")
    @GetMapping("/versions/{documentId}")
    public R<List<DepartmentDocumentVersionVo>> versions(@NotNull @PathVariable Long documentId) {
        return R.ok(documentService.queryVersions(documentId));
    }

    @SaCheckPermission("department:document:remove")
    @Log(title = "科室资料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "资料主键不能为空") @PathVariable Long[] ids) {
        return toAjax(documentService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @SaCheckPermission("department:document:restore")
    @Log(title = "科室资料", businessType = BusinessType.UPDATE)
    @PutMapping("/restore/{ids}")
    public R<Void> restore(@NotEmpty(message = "资料主键不能为空") @PathVariable Long[] ids) {
        return toAjax(documentService.restoreByIds(Arrays.asList(ids)));
    }

    @SaCheckPermission("department:document:query")
    @GetMapping("/preview/{id}")
    public ResponseEntity<byte[]> preview(@NotNull @PathVariable Long id) {
        return documentService.preview(id);
    }

    @SaCheckPermission("department:document:download")
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@NotNull @PathVariable Long id) {
        return documentService.download(id);
    }
}
