package org.dromara.department.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.DepartmentDocument;
import org.dromara.department.domain.DepartmentDocumentVersion;
import org.dromara.department.domain.DepartmentDocumentCategory;
import org.dromara.department.domain.DepartmentProject;
import org.dromara.department.domain.bo.DepartmentDocumentBo;
import org.dromara.department.domain.bo.DepartmentDocumentQueryBo;
import org.dromara.department.domain.vo.DepartmentDocumentVersionVo;
import org.dromara.department.domain.vo.DepartmentDocumentVo;
import org.dromara.department.mapper.DepartmentDocumentMapper;
import org.dromara.department.mapper.DepartmentDocumentCategoryMapper;
import org.dromara.department.mapper.DepartmentDocumentVersionMapper;
import org.dromara.department.mapper.DepartmentProjectMapper;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.IDepartmentDocumentService;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 科室资料业务实现。
 */
@RequiredArgsConstructor
@Service
public class DepartmentDocumentServiceImpl implements IDepartmentDocumentService {

    private static final String DEFAULT_STATUS = "PUBLISHED";
    private static final String DEFAULT_VISIBILITY = "DEPT";
    private static final String DELETED = "1";
    private static final String NORMAL = "0";
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_SUFFIXES = Set.of(
        ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".csv",
        ".jpg", ".jpeg", ".png", ".gif", ".zip", ".rar", ".7z"
    );

    private final DepartmentDocumentMapper documentMapper;
    private final DepartmentDocumentCategoryMapper categoryMapper;
    private final DepartmentDocumentVersionMapper versionMapper;
    private final DepartmentProjectMapper departmentProjectMapper;
    private final ISysOssService ossService;
    private final DepartmentAccessService departmentAccessService;

    @Override
    public PageResult<DepartmentDocumentVo> queryPageList(DepartmentDocumentQueryBo bo, PageQuery pageQuery) {
        DepartmentDocumentQueryBo query = bo == null ? new DepartmentDocumentQueryBo() : bo;
        return pageResult(documentMapper.selectPageList(pageQuery.build(), query, scopeDeptId(), canViewAll()));
    }

    @Override
    public PageResult<DepartmentDocumentVo> queryRecyclePageList(DepartmentDocumentQueryBo bo, PageQuery pageQuery) {
        DepartmentDocumentQueryBo query = bo == null ? new DepartmentDocumentQueryBo() : bo;
        return pageResult(documentMapper.selectRecyclePageList(pageQuery.build(), query, scopeDeptId(), canViewAll()));
    }

    @Override
    public DepartmentDocumentVo queryById(Long id) {
        getAccessibleDocument(id);
        return documentMapper.selectDetailById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentDocumentVo upload(DepartmentDocumentBo bo, MultipartFile file) {
        requireDept("上传资料");
        validateMetadata(bo);
        validateFile(file);

        DepartmentDocument entity = new DepartmentDocument();
        entity.setDeptId(departmentAccessService.currentDeptId());
        copyMetadata(bo, entity);
        SysOssVo oss = uploadOss(file, null);
        entity.setVersionNo(1);
        entity.setCurrentOssId(oss.getOssId());
        entity.setCurrentOriginalName(oss.getOriginalName());
        entity.setCurrentFileName(oss.getFileName());
        entity.setCurrentFileSuffix(oss.getFileSuffix());
        entity.setCurrentFileSize(file.getSize());
        entity.setCurrentContentType(file.getContentType());
        documentMapper.insert(entity);

        DepartmentDocumentVersion version = createVersion(entity.getId(), 1, oss, file, null);
        versionMapper.insert(version);
        entity.setCurrentVersionId(version.getId());
        documentMapper.updateById(entity);
        return documentMapper.selectDetailById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(DepartmentDocumentBo bo) {
        DepartmentDocument entity = getAccessibleDocument(bo.getId());
        validateMetadata(bo);
        copyMetadata(bo, entity);
        return documentMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentDocumentVersionVo uploadVersion(Long documentId, String versionNote, MultipartFile file) {
        DepartmentDocument entity = getAccessibleDocument(documentId);
        validateFile(file);
        SysOssVo oss = uploadOss(file, documentId);
        int versionNo = entity.getVersionNo() == null ? 1 : entity.getVersionNo() + 1;
        DepartmentDocumentVersion version = createVersion(documentId, versionNo, oss, file, StringUtils.trim(versionNote));
        versionMapper.insert(version);

        entity.setVersionNo(versionNo);
        entity.setCurrentVersionId(version.getId());
        entity.setCurrentOssId(oss.getOssId());
        entity.setCurrentOriginalName(oss.getOriginalName());
        entity.setCurrentFileName(oss.getFileName());
        entity.setCurrentFileSuffix(oss.getFileSuffix());
        entity.setCurrentFileSize(file.getSize());
        entity.setCurrentContentType(file.getContentType());
        documentMapper.updateById(entity);

        return versionMapper.selectListByDocumentId(documentId).stream()
            .filter(item -> Objects.equals(item.getId(), version.getId()))
            .findFirst()
            .orElseThrow(() -> new ServiceException("资料版本保存失败"));
    }

    @Override
    public List<DepartmentDocumentVersionVo> queryVersions(Long documentId) {
        getAccessibleDocument(documentId);
        return versionMapper.selectListByDocumentId(documentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            getAccessibleDocument(id);
        }
        // 只删除资料业务关系，OSS 文件和历史版本保留，便于回收站恢复。
        return documentMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean restoreByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            DepartmentDocument entity = documentMapper.selectAnyById(id);
            assertAccessible(entity, DELETED);
        }
        return documentMapper.restoreByIds(ids, LoginHelper.getUserId()) > 0;
    }

    @Override
    public ResponseEntity<byte[]> preview(Long id) {
        DepartmentDocument entity = getAccessibleDocument(id);
        return ossService.preview(entity.getCurrentOssId());
    }

    @Override
    public ResponseEntity<byte[]> download(Long id) {
        DepartmentDocument entity = getAccessibleDocument(id);
        return ossService.download(entity.getCurrentOssId());
    }

    private DepartmentDocumentVersion createVersion(Long documentId, int versionNo, SysOssVo oss,
                                                    MultipartFile file, String versionNote) {
        DepartmentDocumentVersion version = new DepartmentDocumentVersion();
        version.setDocumentId(documentId);
        version.setVersionNo(versionNo);
        version.setOssId(oss.getOssId());
        version.setOriginalName(oss.getOriginalName());
        version.setFileSuffix(oss.getFileSuffix());
        version.setFileSize(file.getSize());
        version.setContentType(file.getContentType());
        version.setVersionNote(versionNote);
        return version;
    }

    private SysOssVo uploadOss(MultipartFile file, Long documentId) {
        SysOssExt ossExt = new SysOssExt();
        ossExt.setBizType("DEPARTMENT_DOCUMENT");
        ossExt.setSource("departmentDocument");
        ossExt.setRefType("DEPARTMENT_DOCUMENT");
        if (documentId != null) {
            ossExt.setRefId(String.valueOf(documentId));
        }
        return ossService.upload(file, ossExt);
    }

    private void copyMetadata(DepartmentDocumentBo bo, DepartmentDocument entity) {
        entity.setProjectId(validateProject(bo.getProjectId()));
        entity.setCategoryId(validateCategory(bo.getCategoryId(), entity.getDeptId(), entity.getCategoryId()));
        entity.setTitle(StringUtils.trim(bo.getTitle()));
        entity.setDescription(StringUtils.trim(bo.getDescription()));
        entity.setTags(StringUtils.trim(bo.getTags()));
        entity.setVisibility(normalizeVisibility(bo.getVisibility()));
        entity.setStatus(normalizeStatus(bo.getStatus()));
        entity.setExpireDate(bo.getExpireDate());
    }

    private void validateMetadata(DepartmentDocumentBo bo) {
        if (bo == null) {
            throw new ServiceException("资料信息不能为空");
        }
        if (StringUtils.isBlank(bo.getTitle())) {
            throw new ServiceException("资料标题不能为空");
        }
        if (bo.getCategoryId() == null) {
            throw new ServiceException("资料分类不能为空");
        }
        if (bo.getTitle().length() > 200) {
            throw new ServiceException("资料标题长度超出限制");
        }
    }

    private Long validateCategory(Long categoryId, Long deptId, Long currentCategoryId) {
        DepartmentDocumentCategory category = categoryMapper.selectById(categoryId);
        if (category == null || !NORMAL.equals(category.getDelFlag())
            || (!"ENABLED".equals(category.getStatus()) && !Objects.equals(categoryId, currentCategoryId))
            || !Objects.equals(category.getDeptId(), deptId)) {
            throw new ServiceException("资料分类不存在、已停用或不属于当前科室");
        }
        return category.getId();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("资料文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ServiceException("资料文件不能超过50MB");
        }
        String suffix = suffix(file.getOriginalFilename());
        if (!ALLOWED_SUFFIXES.contains(suffix)) {
            throw new ServiceException("不支持的资料文件类型：" + suffix);
        }
    }

    private Long validateProject(Long projectId) {
        if (projectId == null) {
            return null;
        }
        DepartmentProject project = departmentProjectMapper.selectById(projectId);
        if (project == null || !NORMAL.equals(project.getDelFlag())
            || !departmentAccessService.canViewEntityDept(project.getDeptId(), "department:project:viewDept")) {
            throw new ServiceException("项目不存在或无权访问");
        }
        return project.getId();
    }

    private DepartmentDocument getAccessibleDocument(Long id) {
        DepartmentDocument entity = documentMapper.selectById(id);
        assertAccessible(entity, NORMAL);
        return entity;
    }

    private void assertAccessible(DepartmentDocument entity, String expectedDelFlag) {
        if (entity == null || !Objects.equals(entity.getDelFlag(), expectedDelFlag)) {
            throw new ServiceException("资料不存在或状态不允许操作");
        }
        if (!departmentAccessService.canViewEntityDept(entity.getDeptId(), "department:document:viewDept")) {
            throw new ServiceException("您没有访问该资料的权限");
        }
    }

    private String normalizeStatus(String status) {
        if ("DRAFT".equalsIgnoreCase(status) || "草稿".equals(status)) {
            return "DRAFT";
        }
        if ("ARCHIVED".equalsIgnoreCase(status) || "已归档".equals(status)) {
            return "ARCHIVED";
        }
        return DEFAULT_STATUS;
    }

    private String normalizeVisibility(String visibility) {
        // 一期按科室共享资料实现，私有资料权限模型留待后续角色/人员授权功能接入。
        return DEFAULT_VISIBILITY;
    }

    private String suffix(String fileName) {
        if (StringUtils.isBlank(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    }

    private PageResult<DepartmentDocumentVo> pageResult(com.baomidou.mybatisplus.extension.plugins.pagination.Page<DepartmentDocumentVo> page) {
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    private void requireDept(String action) {
        if (departmentAccessService.currentDeptId() == null) {
            throw new ServiceException("当前登录用户缺少部门信息，无法" + action);
        }
    }

    private Long scopeDeptId() {
        return departmentAccessService.scopeDeptId("department:document:viewDept");
    }

    private boolean canViewAll() {
        return false;
    }
}
