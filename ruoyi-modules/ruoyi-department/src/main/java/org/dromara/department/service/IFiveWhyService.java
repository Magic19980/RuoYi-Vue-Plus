package org.dromara.department.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.FiveWhyBo;
import org.dromara.department.domain.bo.FiveWhyQueryBo;
import org.dromara.department.domain.bo.FiveWhyReviewBo;
import org.dromara.department.domain.vo.FiveWhyVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

/** 5WHY分析业务接口。 */
public interface IFiveWhyService {

    /**
     * 分页查询5WHY分析记录。
     *
     * @param bo        分析记录查询条件
     * @param pageQuery 分页参数
     * @return 分页分析记录
     */
    PageResult<FiveWhyVo> queryPageList(FiveWhyQueryBo bo, PageQuery pageQuery);

    /**
     * 查询5WHY分析详情。
     *
     * @param id 分析记录主键
     * @return 分析详情
     */
    FiveWhyVo queryById(Long id);

    /**
     * 新增5WHY分析记录。
     *
     * @param bo 分析记录新增参数
     * @return 是否新增成功
     */
    Boolean insertByBo(FiveWhyBo bo);

    /**
     * 修改5WHY分析记录。
     *
     * @param bo 分析记录修改参数
     * @return 是否修改成功
     */
    Boolean updateByBo(FiveWhyBo bo);

    /**
     * 删除5WHY分析记录。
     *
     * @param ids 分析记录主键集合
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /**
     * 审核5WHY分析记录。
     *
     * @param bo 审核参数
     * @return 是否审核成功
     */
    Boolean review(FiveWhyReviewBo bo);

    /**
     * 上传5WHY分析图片。
     *
     * @param file 待上传的图片文件
     * @return 文件对象信息
     */
    SysOssVo uploadImage(MultipartFile file);

    /**
     * 导出5WHY分析 Word 文件。
     *
     * @param id       分析记录主键
     * @param response HTTP响应对象
     * @throws Exception 导出或文件处理失败
     */
    void exportDocx(Long id, HttpServletResponse response) throws Exception;
}
