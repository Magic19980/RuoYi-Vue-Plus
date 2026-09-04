package org.dromara.ecology.service;

import org.dromara.ecology.domain.bo.OaImportBusinessConfigBo;
import org.dromara.ecology.domain.vo.OaImportBusinessConfigVo;
import org.dromara.ecology.domain.vo.OaImportAttachmentTemplateVo;
import org.dromara.ecology.domain.vo.OaImportTemplatePreviewVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/** 通用导入业务模板配置服务。 */
public interface IOaImportBusinessConfigService {

    List<OaImportBusinessConfigVo> queryList(String businessType, boolean enabledOnly);

    OaImportBusinessConfigVo queryById(Long id);

    OaImportTemplatePreviewVo parseTemplate(InputStream inputStream);

    OaImportAttachmentTemplateVo uploadAttachmentTemplate(MultipartFile file);

    byte[] buildTemplate(Long id);

    Boolean insertByBo(OaImportBusinessConfigBo bo);

    Boolean updateByBo(OaImportBusinessConfigBo bo);

    Boolean deleteById(Long id);
}
