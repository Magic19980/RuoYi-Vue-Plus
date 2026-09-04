package org.dromara.ecology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.ecology.domain.bo.OaImportDeptMappingBo;
import org.dromara.ecology.domain.bo.OaImportAttachmentPreviewBo;
import org.dromara.ecology.domain.bo.OaImportQueryBo;
import org.dromara.ecology.domain.bo.OaImportSubmitBo;
import org.dromara.ecology.domain.vo.OaImportBatchVo;
import org.dromara.ecology.domain.vo.OaImportApprovalPreviewVo;
import org.dromara.ecology.domain.vo.OaAttachmentPreviewVo;
import org.dromara.ecology.domain.vo.OaImportBusinessConfigVo;
import org.dromara.ecology.service.IOaImportBusinessConfigService;
import org.dromara.ecology.service.IOaImportBusinessService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** 通用导入业务操作接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ecology/import-business")
public class OaImportBusinessController extends BaseController {

    private final IOaImportBusinessService service;
    private final IOaImportBusinessConfigService configService;

    /** 普通业务用户只读取启用的导入模板，不需要导入模板管理权限。 */
    @SaCheckPermission("ecology:importBusiness:list")
    @GetMapping("/configs")
    public R<List<OaImportBusinessConfigVo>> configs() {
        return R.ok(configService.queryList(null, true));
    }

    @SaCheckPermission("ecology:importBusiness:list")
    @GetMapping("/list")
    public R<PageResult<OaImportBatchVo>> list(OaImportQueryBo bo, PageQuery pageQuery) {
        return R.ok(service.queryPage(bo, pageQuery));
    }

    @SaCheckPermission("ecology:importBusiness:query")
    @GetMapping("/{id}")
    public R<OaImportBatchVo> getInfo(@NotNull(message = "导入批次不能为空") @PathVariable Long id) {
        return R.ok(service.queryBatch(id));
    }

    @SaCheckPermission("ecology:importBusiness:submit")
    @GetMapping("/{id}/approval-preview")
    public R<List<OaImportApprovalPreviewVo>> approvalPreview(
        @NotNull(message = "导入批次不能为空") @PathVariable Long id) {
        return R.ok(service.previewApprovals(id));
    }

    @SaCheckPermission("ecology:importBusiness:remove")
    @Log(title = "通用导入业务批次", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "导入批次不能为空") @PathVariable Long id) {
        service.delete(id);
        return R.ok();
    }

    @SaCheckPermission("ecology:importBusiness:import")
    @Log(title = "通用导入业务", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/{configId}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<OaImportBatchVo> importData(
        @NotNull(message = "业务模板不能为空") @PathVariable Long configId,
        @RequestPart("file") MultipartFile file) throws Exception {
        return R.ok(service.importData(configId, file.getInputStream(), file.getOriginalFilename()));
    }

    @SaCheckPermission("ecology:importBusiness:map")
    @Log(title = "通用导入业务部门匹配", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/department-mapping")
    public R<OaImportBatchVo> mapDepartments(
        @NotNull(message = "导入批次不能为空") @PathVariable Long id,
        @Validated @RequestBody OaImportDeptMappingBo bo) {
        return R.ok(service.mapDepartments(id, bo));
    }

    @SaCheckPermission("ecology:importBusiness:submit")
    @Log(title = "通用导入业务提交泛微", businessType = BusinessType.INSERT)
    @PostMapping("/{id}/submit")
    public R<OaImportBatchVo> submit(
        @NotNull(message = "导入批次不能为空") @PathVariable Long id,
        @Validated @RequestBody OaImportSubmitBo bo) {
        return R.ok(service.submit(id, bo));
    }

    @SaCheckPermission("ecology:importBusiness:submit")
    @PostMapping("/{id}/attachment-preview")
    public R<OaAttachmentPreviewVo> previewAttachment(
        @NotNull(message = "导入批次不能为空") @PathVariable Long id,
        @Validated @RequestBody OaImportAttachmentPreviewBo bo) {
        return R.ok(service.previewAttachment(id, bo));
    }

    /** 生成并下载当前分组附件；提交前只生成文件，不创建泛微申请。 */
    @SaCheckPermission("ecology:importBusiness:submit")
    @PostMapping(value = "/{id}/attachment-download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadAttachment(
        @NotNull(message = "导入批次不能为空") @PathVariable Long id,
        @Validated @RequestBody OaImportAttachmentPreviewBo bo) {
        return service.downloadAttachment(id, bo);
    }

    /** 上传用户在本地 Excel 中修改后的当前分组附件。 */
    @SaCheckPermission("ecology:importBusiness:submit")
    @PostMapping(value = "/{id}/attachment-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<OaImportBatchVo> uploadAttachment(
        @NotNull(message = "导入批次不能为空") @PathVariable Long id,
        @RequestParam("groupKey") String groupKey,
        @RequestPart("file") MultipartFile file) {
        return R.ok(service.uploadAttachment(id, groupKey, file));
    }
}
