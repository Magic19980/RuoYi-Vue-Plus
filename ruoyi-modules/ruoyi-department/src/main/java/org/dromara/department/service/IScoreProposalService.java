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

    /** 按当前业务科室和查询条件分页查询提案。 */
    PageResult<ScoreProposalVo> queryPageList(ScoreProposalQueryBo bo, PageQuery pageQuery);

    /** 查询提案详情，并校验当前用户的数据权限。 */
    ScoreProposalVo queryById(Long id);

    /** 查询当前业务科室中可作为提案人的有效成员。 */
    List<PersonUserOptionVo> queryMemberOptions();

    /** 查询当前用户可见的全部人员选项。 */
    List<PersonUserOptionVo> queryUserOptions();

    /** 分页查询人员选择器数据。 */
    PageResult<PersonUserOptionVo> queryUserOptionsPage(PersonUserOptionQueryBo bo, PageQuery pageQuery);

    /** 按用户编号批量查询人员选项，保持传入顺序之外不承诺排序。 */
    List<PersonUserOptionVo> queryUserOptionsByIds(Collection<Long> userIds);

    /**
     * 查询指定月份的提案统计和精益评分。
     *
     * <p>月度统计排除暂存提案，其余状态均纳入提案数和状态统计。</p>
     */
    ScoreProposalMetricVo queryMetric(String month);

    /** 新增提案草稿或提交提案。 */
    Boolean insertByBo(ScoreProposalBo bo);

    /** 修改提案，具体可编辑范围由提案状态和权限共同决定。 */
    Boolean updateByBo(ScoreProposalBo bo);

    /** 删除当前业务科室中允许删除的提案。 */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /** 审核或现场确认提案。 */
    Boolean review(ScoreProposalReviewBo bo);

    /** 上传提案前后改善图片。 */
    SysOssVo uploadImage(MultipartFile file);

    /** 导出提案Excel文件。 */
    void exportXlsx(Long id, HttpServletResponse response) throws Exception;

    /** 生成提案Excel预览地址或内容。 */
    String previewXlsx(Long id) throws Exception;
}
