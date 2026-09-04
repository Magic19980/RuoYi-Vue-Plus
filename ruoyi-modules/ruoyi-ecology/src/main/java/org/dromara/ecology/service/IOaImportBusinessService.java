package org.dromara.ecology.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.ecology.domain.bo.OaImportBusinessConfigBo;
import org.dromara.ecology.domain.bo.OaImportAttachmentPreviewBo;
import org.dromara.ecology.domain.bo.OaImportDeptMappingBo;
import org.dromara.ecology.domain.bo.OaImportQueryBo;
import org.dromara.ecology.domain.bo.OaImportSubmitBo;
import org.dromara.ecology.domain.vo.OaImportBatchVo;
import org.dromara.ecology.domain.vo.OaAttachmentPreviewVo;
import org.dromara.ecology.domain.vo.OaImportApprovalPreviewVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/** 通用 Excel 导入、分组、附件生成和泛微提交流程服务。 */
public interface IOaImportBusinessService {

    PageResult<OaImportBatchVo> queryPage(OaImportQueryBo bo, PageQuery pageQuery);

    OaImportBatchVo queryBatch(Long batchId);

    void delete(Long batchId);

    OaImportBatchVo importData(Long configId, InputStream inputStream, String sourceFileName);

    OaImportBatchVo mapDepartments(Long batchId, OaImportDeptMappingBo bo);

    List<OaImportApprovalPreviewVo> previewApprovals(Long batchId);

    OaImportBatchVo submit(Long batchId, OaImportSubmitBo bo);

    OaAttachmentPreviewVo previewAttachment(Long batchId, OaImportAttachmentPreviewBo bo);

    ResponseEntity<byte[]> downloadAttachment(Long batchId, OaImportAttachmentPreviewBo bo);

    OaImportBatchVo uploadAttachment(Long batchId, String groupKey, MultipartFile file);
}
