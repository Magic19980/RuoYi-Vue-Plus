package org.dromara.department.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.ScoreProposalBo;
import org.dromara.department.domain.bo.ScoreProposalQueryBo;
import org.dromara.department.domain.bo.ScoreProposalReviewBo;
import org.dromara.department.domain.bo.PersonUserOptionQueryBo;
import org.dromara.department.domain.vo.PersonUserOptionVo;
import org.dromara.department.domain.vo.ScoreProposalMetricVo;
import org.dromara.department.domain.vo.ScoreProposalVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

/** SCORE提案业务接口。 */
public interface IScoreProposalService {

    /**
     * 按当前业务科室和查询条件分页查询提案。
     *
     * @param bo        提案查询条件
     * @param pageQuery 分页参数
     * @return 分页提案数据
     */
    PageResult<ScoreProposalVo> queryPageList(ScoreProposalQueryBo bo, PageQuery pageQuery);

    /**
     * 查询提案详情，并校验当前用户的数据权限。
     *
     * @param id 提案主键
     * @return 提案详情
     */
    ScoreProposalVo queryById(Long id);

    /**
     * 查询当前业务科室中可作为提案人的有效成员。
     *
     * @return 提案人选择项列表
     */
    List<PersonUserOptionVo> queryMemberOptions();

    /**
     * 查询当前用户可见的全部人员选项。
     *
     * @return 人员选择项列表
     */
    List<PersonUserOptionVo> queryUserOptions();

    /**
     * 分页查询人员选择器数据。
     *
     * @param bo        人员选择查询条件
     * @param pageQuery 分页参数
     * @return 分页人员选择项
     */
    PageResult<PersonUserOptionVo> queryUserOptionsPage(PersonUserOptionQueryBo bo, PageQuery pageQuery);

    /**
     * 按用户编号批量查询人员选项。
     *
     * @param userIds 用户主键集合
     * @return 人员选择项列表
     */
    List<PersonUserOptionVo> queryUserOptionsByIds(Collection<Long> userIds);

    /**
     * 查询指定月份的提案统计和精益评分。
     *
     * <p>月度统计排除暂存提案，其余状态均纳入提案数和状态统计。</p>
     */
    ScoreProposalMetricVo queryMetric(String month);

    /**
     * 新增提案草稿或提交提案。
     *
     * @param bo 提案新增参数
     * @return 是否新增成功
     */
    Boolean insertByBo(ScoreProposalBo bo);

    /**
     * 修改提案，具体可编辑范围由提案状态和权限共同决定。
     *
     * @param bo 提案修改参数
     * @return 是否修改成功
     */
    Boolean updateByBo(ScoreProposalBo bo);

    /**
     * 删除当前业务科室中允许删除的提案。
     *
     * @param ids 提案主键集合
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /**
     * 审核或现场确认提案。
     *
     * @param bo 审核处理参数
     * @return 是否处理成功
     */
    Boolean review(ScoreProposalReviewBo bo);

    /**
     * 上传提案前后改善图片。
     *
     * @param file 待上传的图片文件
     * @return 文件对象信息
     */
    SysOssVo uploadImage(MultipartFile file);

    /**
     * 导出提案Excel文件。
     *
     * @param id       提案主键
     * @param response HTTP响应对象
     * @throws Exception 导出或文件处理失败
     */
    void exportXlsx(Long id, HttpServletResponse response) throws Exception;

    /**
     * 生成提案Excel预览地址或内容。
     *
     * @param id 提案主键
     * @return 预览地址或预览内容
     * @throws Exception 预览生成或文件处理失败
     */
    String previewXlsx(Long id) throws Exception;
}
