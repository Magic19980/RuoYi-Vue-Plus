package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.DepartmentDocumentBo;
import org.dromara.department.domain.bo.DepartmentDocumentQueryBo;
import org.dromara.department.domain.vo.DepartmentDocumentVersionVo;
import org.dromara.department.domain.vo.DepartmentDocumentVideoPreviewVo;
import org.dromara.department.domain.vo.DepartmentDocumentVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

/**
 * 科室资料业务接口。
 */
public interface IDepartmentDocumentService {

    /**
     * 分页查询当前业务科室的资料。
     *
     * @param bo        资料查询条件
     * @param pageQuery 分页参数
     * @return 分页资料数据
     */
    PageResult<DepartmentDocumentVo> queryPageList(DepartmentDocumentQueryBo bo, PageQuery pageQuery);

    /**
     * 分页查询资料回收站。
     *
     * @param bo        资料查询条件
     * @param pageQuery 分页参数
     * @return 分页回收站资料
     */
    PageResult<DepartmentDocumentVo> queryRecyclePageList(DepartmentDocumentQueryBo bo, PageQuery pageQuery);

    /**
     * 查询资料详情。
     *
     * @param id 资料主键
     * @return 资料详情
     */
    DepartmentDocumentVo queryById(Long id);

    /**
     * 上传并创建资料。
     *
     * @param bo   资料元数据
     * @param file 资料文件
     * @return 创建后的资料详情
     */
    DepartmentDocumentVo upload(DepartmentDocumentBo bo, MultipartFile file);

    /**
     * 修改资料元数据。
     *
     * @param bo 资料修改参数
     * @return 是否修改成功
     */
    Boolean updateByBo(DepartmentDocumentBo bo);

    /**
     * 为资料上传新版本。
     *
     * @param documentId 资料主键
     * @param versionNote 版本说明
     * @param file        新版本文件
     * @return 新版本信息
     */
    DepartmentDocumentVersionVo uploadVersion(Long documentId, String versionNote, MultipartFile file);

    /**
     * 查询资料版本列表。
     *
     * @param documentId 资料主键
     * @return 版本列表
     */
    List<DepartmentDocumentVersionVo> queryVersions(Long documentId);

    /**
     * 删除资料至回收站。
     *
     * @param ids 资料主键集合
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /**
     * 从回收站恢复资料。
     *
     * @param ids 资料主键集合
     * @return 是否恢复成功
     */
    Boolean restoreByIds(Collection<Long> ids);

    /**
     * 获取资料预览内容。
     *
     * @param id 资料主键
     * @return 预览响应内容
     */
    ResponseEntity<byte[]> preview(Long id);

    /**
     * 获取视频资料的临时播放地址。
     *
     * @param id 资料主键
     * @return 视频播放信息
     */
    DepartmentDocumentVideoPreviewVo videoPreview(Long id);

    /**
     * 获取指定历史版本视频资料的临时播放地址。
     *
     * @param documentId 资料主键
     * @param versionId 版本主键
     * @return 视频播放信息
     */
    DepartmentDocumentVideoPreviewVo videoPreviewVersion(Long documentId, Long versionId);

    /**
     * 下载资料文件。
     *
     * @param id 资料主键
     * @return 文件下载响应内容
     */
    ResponseEntity<byte[]> download(Long id);
}
