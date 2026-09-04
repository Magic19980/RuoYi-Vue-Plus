package org.dromara.ecology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.ecology.domain.bo.OaImportBusinessConfigBo;
import org.dromara.ecology.domain.vo.OaImportAttachmentTemplateVo;
import org.dromara.ecology.domain.vo.OaImportBusinessConfigVo;
import org.dromara.ecology.domain.vo.OaImportTemplatePreviewVo;
import org.dromara.ecology.service.IOaImportBusinessConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** 通用导入业务模板配置接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ecology/import-config")
public class OaImportBusinessConfigController extends BaseController {

    private final IOaImportBusinessConfigService service;

    @SaCheckPermission("ecology:importConfig:list")
    @GetMapping("/list")
    public R<List<OaImportBusinessConfigVo>> list(String businessType, Boolean enabledOnly) {
        return R.ok(service.queryList(businessType, Boolean.TRUE.equals(enabledOnly)));
    }

    @SaCheckPermission("ecology:importConfig:query")
    @GetMapping("/{id}")
    public R<OaImportBusinessConfigVo> getInfo(@NotNull(message = "业务模板主键不能为空") @PathVariable Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission(value = {"ecology:importConfig:add", "ecology:importConfig:edit"}, mode = SaMode.OR)
    @PostMapping(value = "/parse-template", consumes = "multipart/form-data")
    public R<OaImportTemplatePreviewVo> parseTemplate(@RequestPart("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return R.fail("Excel 模板不能为空");
        }
        return R.ok(service.parseTemplate(file.getInputStream()));
    }

    @SaCheckPermission(value = {"ecology:importConfig:add", "ecology:importConfig:edit"}, mode = SaMode.OR)
    @PostMapping(value = "/attachment-template", consumes = "multipart/form-data")
    public R<OaImportAttachmentTemplateVo> uploadAttachmentTemplate(@RequestPart("file") MultipartFile file) {
        return R.ok(service.uploadAttachmentTemplate(file));
    }

    @SaCheckPermission("ecology:importConfig:query")
    @PostMapping("/{id}/template")
    public void downloadTemplate(@NotNull(message = "业务模板主键不能为空") @PathVariable Long id, HttpServletResponse response) throws IOException {
        byte[] content = service.buildTemplate(id);
        String fileName = URLEncoder.encode("业务导入模板.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
    }

    @SaCheckPermission("ecology:importConfig:add")
    @Log(title = "通用导入业务模板", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody OaImportBusinessConfigBo bo) {
        return toAjax(service.insertByBo(bo));
    }

    @SaCheckPermission("ecology:importConfig:edit")
    @Log(title = "通用导入业务模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody OaImportBusinessConfigBo bo) {
        return toAjax(service.updateByBo(bo));
    }

    @SaCheckPermission("ecology:importConfig:remove")
    @Log(title = "通用导入业务模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "业务模板主键不能为空") @PathVariable Long id) {
        return toAjax(service.deleteById(id));
    }
}
