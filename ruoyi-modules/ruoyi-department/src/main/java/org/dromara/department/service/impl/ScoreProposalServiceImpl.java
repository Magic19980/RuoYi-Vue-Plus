package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.ScoreCategory;
import org.dromara.department.domain.ScoreProposal;
import org.dromara.department.domain.bo.ScoreProposalBo;
import org.dromara.department.domain.bo.ScoreProposalQueryBo;
import org.dromara.department.domain.bo.ScoreProposalReviewBo;
import org.dromara.department.domain.vo.PersonUserOptionVo;
import org.dromara.department.domain.vo.ScoreProposalVo;
import org.dromara.department.mapper.PersonProfileMapper;
import org.dromara.department.mapper.ScoreProposalMapper;
import org.dromara.department.mapper.ScoreCategoryMapper;
import org.dromara.department.service.IScoreProposalService;
import org.dromara.department.service.IDepartmentTaskService;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.DepartmentScope;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.time.LocalDate;

/** SCORE提案业务实现。 */
@RequiredArgsConstructor
@Service
public class ScoreProposalServiceImpl implements IScoreProposalService {

    private static final String REVIEW_PENDING = "PENDING";
    private static final String REVIEW_APPROVED = "APPROVED";
    private static final String REVIEW_REJECTED = "REJECTED";

    private final ScoreProposalMapper scoreProposalMapper;
    private final ScoreCategoryMapper scoreCategoryMapper;
    private final PersonProfileMapper personProfileMapper;
    private final ScoreProposalXlsxService scoreProposalXlsxService;
    private final ISysOssService ossService;
    private final ISysDeptService sysDeptService;
    private final IDepartmentTaskService departmentTaskService;
    private final DepartmentAccessService departmentAccessService;

    @Override
    public PageResult<ScoreProposalVo> queryPageList(ScoreProposalQueryBo bo, PageQuery pageQuery) {
        ScoreProposalQueryBo query = bo == null ? new ScoreProposalQueryBo() : bo;
        Page<ScoreProposal> page = pageQuery.build();
        Page<ScoreProposal> result = scoreProposalMapper.selectPageList(page, query, scope());
        return PageResult.build(result.getRecords().stream().map(this::toVo).toList(), result.getTotal());
    }

    @Override
    public ScoreProposalVo queryById(Long id) {
        ScoreProposal entity = getAccessible(id);
        refreshCurrentProposerRole(entity);
        return toVo(entity);
    }

    @Override
    public List<PersonUserOptionVo> queryMemberOptions() {
        return personProfileMapper.selectMemberUserOptions(requireDept(), LocalDate.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ScoreProposalBo bo) {
        Long deptId = requireDept();
        ScoreProposal entity = new ScoreProposal();
        copyBo(bo, entity, false, deptId);
        entity.setDeptId(deptId);
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
        Long deptId = entity.getDeptId() == null ? requireDept() : entity.getDeptId();
        copyBo(bo, entity, true, deptId);
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
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            getAccessible(id);
        }
        return scoreProposalMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean review(ScoreProposalReviewBo bo) {
        ScoreProposal entity = getAccessible(bo.getId());
        departmentTaskService.checkReviewer("SCORE_PROPOSAL", entity.getDeptId());
        if (!REVIEW_APPROVED.equals(bo.getReviewStatus()) && !REVIEW_REJECTED.equals(bo.getReviewStatus())) {
            throw new ServiceException("审核结果只能是通过或驳回");
        }
        entity.setReviewStatus(bo.getReviewStatus());
        entity.setReviewComment(bo.getReviewComment());
        entity.setReviewerUserId(LoginHelper.getUserId());
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

    private void copyBo(ScoreProposalBo bo, ScoreProposal entity, boolean updating, Long targetDeptId) {
        entity.setCompanyName(bo.getCompanyName());
        entity.setTeamMembers(bo.getTeamMembers());
        applyProposer(bo, entity, targetDeptId, updating);
        entity.setProposerLevel(bo.getProposerLevel());
        entity.setDeptName(resolveDeptName(targetDeptId));
        applyImplementers(bo, entity, targetDeptId, updating);
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
        entity.setBeforeOssId(bo.getBeforeOssId());
        entity.setAfterOssId(bo.getAfterOssId());
        entity.setStartDate(bo.getStartDate());
        entity.setPlannedCompletionDate(bo.getPlannedCompletionDate());
        entity.setActualCompletionDate(bo.getActualCompletionDate());
        entity.setCompletionStatus(bo.getCompletionStatus());
        entity.setRemark(bo.getRemark());
    }

    /**
     * 提议人信息必须从当前科室有效人员档案读取，姓名和工号不接受前端自由修改。
     * 岗位由用户管理中的 sys_user_post/sys_post 实时读取，不在提案表中保存冗余值。
     */
    private void applyProposer(ScoreProposalBo bo, ScoreProposal entity, Long targetDeptId, boolean updating) {
        Long proposerUserId = bo.getProposerUserId();
        if (proposerUserId == null) {
            throw new ServiceException("请选择当前科室的提议人");
        }
        PersonUserOptionVo proposer = personProfileMapper.selectMemberUserOptions(targetDeptId, LocalDate.now()).stream()
            .filter(item -> Objects.equals(item.getUserId(), proposerUserId))
            .findFirst()
            .orElse(null);
        if (proposer == null) {
            if (updating && Objects.equals(entity.getProposerUserId(), proposerUserId)) {
                return;
            }
            throw new ServiceException("提议人必须是当前科室人员档案中的有效成员");
        }
        entity.setProposerUserId(proposer.getUserId());
        entity.setProposerName(StringUtils.isBlank(proposer.getNickName()) ? proposer.getUserName() : proposer.getNickName());
        entity.setEmployeeNo(proposer.getEmployeeNo());
    }

    /** 岗位由用户管理中的 sys_user_post 维护，提案详情直接读取用户当前岗位。 */
    private void refreshCurrentProposerRole(ScoreProposal entity) {
        if (entity.getProposerUserId() == null) {
            return;
        }
        PersonUserOptionVo proposer = personProfileMapper.selectUserOptionById(entity.getProposerUserId());
        entity.setProposerRole(proposer == null ? null : proposer.getJobTitle());
    }

    /**
     * 实施人和监督人必须从当前科室有效人员档案中选择，数据库同时保存用户ID和姓名快照。
     * 编辑历史提案时允许保留已经离开科室的原成员，但不允许新增已失效成员。
     */
    private void applyImplementers(ScoreProposalBo bo, ScoreProposal entity, Long targetDeptId, boolean updating) {
        if (bo.getImplementerUserIds() == null && updating && StringUtils.isNotBlank(entity.getImplementerSupervisor())) {
            return;
        }
        List<Long> selectedIds = bo.getImplementerUserIds() == null ? List.of() : bo.getImplementerUserIds().stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        List<Long> previousIds = parseUserIds(entity.getImplementerUserIds());
        Map<Long, PersonUserOptionVo> activeMembers = personProfileMapper.selectMemberUserOptions(targetDeptId, LocalDate.now()).stream()
            .filter(item -> item.getUserId() != null)
            .collect(Collectors.toMap(PersonUserOptionVo::getUserId, item -> item, (left, right) -> left, LinkedHashMap::new));
        List<String> names = new ArrayList<>();
        for (Long userId : selectedIds) {
            PersonUserOptionVo member = activeMembers.get(userId);
            if (member != null) {
                names.add(memberName(member));
                continue;
            }
            if (updating && previousIds.contains(userId)) {
                String historicalName = historicalName(entity.getImplementerSupervisor(), previousIds, userId);
                names.add(StringUtils.isBlank(historicalName) ? String.valueOf(userId) : historicalName);
                continue;
            }
            throw new ServiceException("实施人/监督人必须是当前科室人员档案中的有效成员");
        }
        entity.setImplementerUserIds(JsonUtils.toJsonString(selectedIds));
        entity.setImplementerSupervisor(String.join("、", names));
    }

    private String memberName(PersonUserOptionVo member) {
        return StringUtils.isBlank(member.getNickName()) ? member.getUserName() : member.getNickName();
    }

    private String historicalName(String snapshot, List<Long> previousIds, Long userId) {
        List<String> names = StringUtils.isBlank(snapshot) ? List.of() : Arrays.stream(snapshot.split("[、,，]"))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .toList();
        int index = previousIds.indexOf(userId);
        return index >= 0 && index < names.size() ? names.get(index) : null;
    }

    private List<Long> parseUserIds(String json) {
        if (StringUtils.isBlank(json)) {
            return List.of();
        }
        try {
            return JsonUtils.parseArray(json, Long.class);
        } catch (Exception ex) {
            throw new ServiceException("实施人/监督人数据格式错误，请联系管理员");
        }
    }

    private String resolveDeptName(Long deptId) {
        SysDeptVo dept = sysDeptService.selectDeptById(deptId);
        if (dept == null || StringUtils.isBlank(dept.getDeptName())) {
            throw new ServiceException("当前登录科室不存在，无法保存SCORE提案");
        }
        return dept.getDeptName();
    }

    private ScoreProposalVo toVo(ScoreProposal entity) {
        ScoreProposalVo vo = new ScoreProposalVo();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setMainCategoryId(entity.getMainCategoryId());
        vo.setSubCategoryId(entity.getSubCategoryId());
        vo.setProposerUserId(entity.getProposerUserId());
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
        vo.setImplementerUserIds(parseUserIds(entity.getImplementerUserIds()));
        vo.setBeforeOssId(entity.getBeforeOssId());
        vo.setAfterOssId(entity.getAfterOssId());
        vo.setStartDate(entity.getStartDate());
        vo.setPlannedCompletionDate(entity.getPlannedCompletionDate());
        vo.setActualCompletionDate(entity.getActualCompletionDate());
        vo.setCompletionStatus(entity.getCompletionStatus());
        vo.setRemark(entity.getRemark());
        vo.setReviewStatus(entity.getReviewStatus());
        vo.setReviewComment(entity.getReviewComment());
        vo.setReviewerUserId(entity.getReviewerUserId());
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
        if (departmentAccessService.canViewEntityDept(entity.getDeptId(), "department:score:viewDept")) {
            return entity;
        }
        throw new ServiceException("您没有访问该SCORE提案的权限");
    }

    private Long requireDept() {
        Long deptId = departmentAccessService.currentDeptId();
        if (deptId == null) {
            throw new ServiceException("当前登录用户缺少部门信息，无法维护SCORE提案");
        }
        return deptId;
    }

    private DepartmentScope scope() {
        return departmentAccessService.scope("department:score:viewDept");
    }
}
