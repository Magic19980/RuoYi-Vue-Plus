package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.ScoreCategory;
import org.dromara.department.domain.ScoreProposal;
import org.dromara.department.domain.bo.ScoreProposalBo;
import org.dromara.department.domain.bo.ScoreProposalQueryBo;
import org.dromara.department.domain.bo.ScoreProposalReviewBo;
import org.dromara.department.domain.vo.ScoreProposalVo;
import org.dromara.department.mapper.ScoreProposalMapper;
import org.dromara.department.mapper.ScoreCategoryMapper;
import org.dromara.department.service.IScoreProposalService;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Objects;

/** SCORE提案业务实现。 */
@RequiredArgsConstructor
@Service
public class ScoreProposalServiceImpl implements IScoreProposalService {

    private static final String REVIEW_PENDING = "PENDING";
    private static final String REVIEW_APPROVED = "APPROVED";
    private static final String REVIEW_REJECTED = "REJECTED";

    private final ScoreProposalMapper scoreProposalMapper;
    private final ScoreCategoryMapper scoreCategoryMapper;
    private final ScoreProposalXlsxService scoreProposalXlsxService;
    private final ISysOssService ossService;

    @Override
    public PageResult<ScoreProposalVo> queryPageList(ScoreProposalQueryBo bo, PageQuery pageQuery) {
        ScoreProposalQueryBo query = bo == null ? new ScoreProposalQueryBo() : bo;
        Page<ScoreProposal> page = pageQuery.build();
        Page<ScoreProposal> result = scoreProposalMapper.selectPageList(page, query, scopeDeptId(), canViewAll());
        return PageResult.build(result.getRecords().stream().map(this::toVo).toList(), result.getTotal());
    }

    @Override
    public ScoreProposalVo queryById(Long id) {
        return toVo(getAccessible(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ScoreProposalBo bo) {
        requireDept();
        ScoreProposal entity = new ScoreProposal();
        copyBo(bo, entity, false);
        entity.setDeptId(LoginHelper.getDeptId());
        entity.setReviewStatus(REVIEW_PENDING);
        entity.setReviewComment(null);
        if (StringUtils.isBlank(entity.getCompletionStatus())) {
            entity.setCompletionStatus("进行中");
        }
        return scoreProposalMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ScoreProposalBo bo) {
        ScoreProposal entity = getAccessible(bo.getId());
        copyBo(bo, entity, true);
        entity.setReviewStatus(REVIEW_PENDING);
        entity.setReviewComment(null);
        if (StringUtils.isBlank(entity.getCompletionStatus())) {
            entity.setCompletionStatus("进行中");
        }
        return scoreProposalMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        for (Long id : ids) {
            getAccessible(id);
        }
        return scoreProposalMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean review(ScoreProposalReviewBo bo) {
        ScoreProposal entity = getAccessible(bo.getId());
        if (!REVIEW_APPROVED.equals(bo.getReviewStatus()) && !REVIEW_REJECTED.equals(bo.getReviewStatus())) {
            throw new ServiceException("审核结果只能是通过或驳回");
        }
        entity.setReviewStatus(bo.getReviewStatus());
        entity.setReviewComment(bo.getReviewComment());
        return scoreProposalMapper.updateById(entity) > 0;
    }

    @Override
    public SysOssVo uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("图片不能为空");
        }
        String originalName = StringUtils.isBlank(file.getOriginalFilename()) ? "image" : file.getOriginalFilename();
        String lowerName = originalName.toLowerCase();
        if (!(lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png")
            || lowerName.endsWith(".gif") || lowerName.endsWith(".bmp"))) {
            throw new ServiceException("只支持JPG、PNG、GIF、BMP图片");
        }
        SysOssExt ossExt = new SysOssExt();
        ossExt.setBizType("DEPARTMENT_SCORE");
        ossExt.setSource("userUpload");
        ossExt.setRefType("SCORE_PROPOSAL");
        return ossService.upload(file, ossExt);
    }

    @Override
    public void exportXlsx(Long id, HttpServletResponse response) throws Exception {
        scoreProposalXlsxService.export(queryById(id), response);
    }

    private void copyBo(ScoreProposalBo bo, ScoreProposal entity, boolean updating) {
        entity.setCompanyName(bo.getCompanyName());
        entity.setTeamMembers(bo.getTeamMembers());
        entity.setEmployeeNo(bo.getEmployeeNo());
        entity.setProposerName(bo.getProposerName());
        entity.setProposerRole(bo.getProposerRole());
        entity.setProposerLevel(bo.getProposerLevel());
        entity.setDeptName(bo.getDeptName());
        ScoreCategory mainCategory = resolveCategory(bo.getMainCategoryId(), bo.getMainCategory(), null, "提案大类");
        ScoreCategory subCategory = resolveCategory(bo.getSubCategoryId(), bo.getSubCategory(), mainCategory.getId(), "提案小类");
        if (!Objects.equals(mainCategory.getParentId(), 0L) || !Objects.equals(mainCategory.getCategoryLevel(), 1)) {
            throw new ServiceException("提案大类配置无效");
        }
        if (!Objects.equals(subCategory.getParentId(), mainCategory.getId()) || !Objects.equals(subCategory.getCategoryLevel(), 2)) {
            throw new ServiceException("提案小类不属于所选提案大类");
        }
        if (!updating || !sameCategory(entity.getMainCategoryId(), entity.getMainCategory(), mainCategory)) {
            ensureEnabled(mainCategory, "提案大类");
        }
        if (!updating || !sameCategory(entity.getSubCategoryId(), entity.getSubCategory(), subCategory)) {
            ensureEnabled(subCategory, "提案小类");
        }
        entity.setMainCategoryId(mainCategory.getId());
        entity.setSubCategoryId(subCategory.getId());
        entity.setMainCategory(mainCategory.getCategoryName());
        entity.setSubCategory(subCategory.getCategoryName());
        entity.setProblemDescription(bo.getProblemDescription());
        entity.setImprovementMeasure(bo.getImprovementMeasure());
        entity.setImplementerSupervisor(bo.getImplementerSupervisor());
        entity.setBeforeOssId(bo.getBeforeOssId());
        entity.setAfterOssId(bo.getAfterOssId());
        entity.setStartDate(bo.getStartDate());
        entity.setPlannedCompletionDate(bo.getPlannedCompletionDate());
        entity.setActualCompletionDate(bo.getActualCompletionDate());
        entity.setCompletionStatus(bo.getCompletionStatus());
        entity.setRemark(bo.getRemark());
    }

    private ScoreProposalVo toVo(ScoreProposal entity) {
        ScoreProposalVo vo = new ScoreProposalVo();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setMainCategoryId(entity.getMainCategoryId());
        vo.setSubCategoryId(entity.getSubCategoryId());
        vo.setCompanyName(entity.getCompanyName());
        vo.setTeamMembers(entity.getTeamMembers());
        vo.setEmployeeNo(entity.getEmployeeNo());
        vo.setProposerName(entity.getProposerName());
        vo.setProposerRole(entity.getProposerRole());
        vo.setProposerLevel(entity.getProposerLevel());
        vo.setDeptName(entity.getDeptName());
        vo.setMainCategory(entity.getMainCategory());
        vo.setSubCategory(entity.getSubCategory());
        vo.setProblemDescription(entity.getProblemDescription());
        vo.setImprovementMeasure(entity.getImprovementMeasure());
        vo.setImplementerSupervisor(entity.getImplementerSupervisor());
        vo.setBeforeOssId(entity.getBeforeOssId());
        vo.setAfterOssId(entity.getAfterOssId());
        vo.setStartDate(entity.getStartDate());
        vo.setPlannedCompletionDate(entity.getPlannedCompletionDate());
        vo.setActualCompletionDate(entity.getActualCompletionDate());
        vo.setCompletionStatus(entity.getCompletionStatus());
        vo.setRemark(entity.getRemark());
        vo.setReviewStatus(entity.getReviewStatus());
        vo.setReviewComment(entity.getReviewComment());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private ScoreCategory resolveCategory(Long categoryId, String categoryName, Long parentId, String label) {
        ScoreCategory category = categoryId == null ? null : scoreCategoryMapper.selectById(categoryId);
        if (category == null && StringUtils.isNotBlank(categoryName)) {
            category = scoreCategoryMapper.selectOne(Wrappers.<ScoreCategory>lambdaQuery()
                .eq(ScoreCategory::getParentId, parentId == null ? 0L : parentId)
                .eq(ScoreCategory::getCategoryName, StringUtils.trim(categoryName))
                .eq(ScoreCategory::getDelFlag, "0")
                .last("limit 1"));
        }
        if (category == null) {
            throw new ServiceException("请选择有效的" + label);
        }
        return category;
    }

    private boolean sameCategory(Long currentId, String currentName, ScoreCategory category) {
        return Objects.equals(currentId, category.getId())
            || (currentId == null && Objects.equals(currentName, category.getCategoryName()));
    }

    private void ensureEnabled(ScoreCategory category, String label) {
        if (!"ENABLED".equals(category.getStatus())) {
            throw new ServiceException("所选" + label + "已停用");
        }
    }

    private ScoreProposal getAccessible(Long id) {
        ScoreProposal entity = scoreProposalMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("SCORE提案不存在");
        }
        if (canViewAll() || Objects.equals(entity.getDeptId(), LoginHelper.getDeptId())) {
            return entity;
        }
        throw new ServiceException("您没有访问该SCORE提案的权限");
    }

    private void requireDept() {
        if (LoginHelper.getDeptId() == null) {
            throw new ServiceException("当前登录用户缺少部门信息，无法维护SCORE提案");
        }
    }

    private boolean canViewAll() {
        return LoginHelper.isSuperAdmin();
    }

    private Long scopeDeptId() {
        return canViewAll() ? null : LoginHelper.getDeptId();
    }
}
