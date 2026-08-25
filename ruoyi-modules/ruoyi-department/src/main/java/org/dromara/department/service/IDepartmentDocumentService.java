package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.DepartmentDocumentBo;
import org.dromara.department.domain.bo.DepartmentDocumentQueryBo;
import org.dromara.department.domain.vo.DepartmentDocumentVersionVo;
import org.dromara.department.domain.vo.DepartmentDocumentVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

/**
 * 科室资料业务接口。
 */
public interface IDepartmentDocumentService {

    PageResult<DepartmentDocumentVo> queryPageList(DepartmentDocumentQueryBo bo, PageQuery pageQuery);

    PageResult<DepartmentDocumentVo> queryRecyclePageList(DepartmentDocumentQueryBo bo, PageQuery pageQuery);

    DepartmentDocumentVo queryById(Long id);

    DepartmentDocumentVo upload(DepartmentDocumentBo bo, MultipartFile file);

    Boolean updateByBo(DepartmentDocumentBo bo);

    DepartmentDocumentVersionVo uploadVersion(Long documentId, String versionNote, MultipartFile file);

    List<DepartmentDocumentVersionVo> queryVersions(Long documentId);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    Boolean restoreByIds(Collection<Long> ids);

    ResponseEntity<byte[]> preview(Long id);

    ResponseEntity<byte[]> download(Long id);
}
