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
import org.dromara.department.domain.ScoreProposalReviewTask;
import org.dromara.department.domain.bo.ScoreProposalBo;
import org.dromara.department.domain.bo.ScoreProposalQueryBo;
import org.dromara.department.domain.bo.ScoreProposalReviewBo;
import org.dromara.department.domain.bo.PersonUserOptionQueryBo;
import org.dromara.department.domain.vo.PersonUserOptionVo;
import org.dromara.department.domain.vo.ScoreProposalMetricVo;
import org.dromara.department.domain.vo.ScoreProposalVo;
import org.dromara.department.mapper.PersonProfileMapper;
import org.dromara.department.mapper.ScoreProposalMapper;
import org.dromara.department.mapper.ScoreCategoryMapper;
import org.dromara.department.service.IScoreProposalService;
import org.dromara.department.service.IDepartmentTaskService;
import org.dromara.department.service.IScoreProposalReviewTaskService;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.DepartmentScope;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.api.MessageService;
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
import java.time.LocalDateTime;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/** SCORE提案业务实现。 */
@RequiredArgsConstructor
@Service
public class ScoreProposalServiceImpl implements IScoreProposalService {

    private static final String REVIEW_PENDING = "PENDING";
    private static final String REVIEW_DRAFT = "DRAFT";
    private static final String REVIEW_PENDING_CONFIRM = "PENDING_CONFIRM";
    private static final String REVIEW_APPROVED = "APPROVED";
    private static final String REVIEW_REJECTED = "REJECTED";

    private final ScoreProposalMapper scoreProposalMapper;
    private final ScoreCategoryMapper scoreCategoryMapper;
    private final PersonProfileMapper personProfileMapper;
    private final ScoreProposalXlsxService scoreProposalXlsxService;
    private final ISysOssService ossService;
    private final ISysDeptService sysDeptService;
    private final IDepartmentTaskService departmentTaskService;
    private final IScoreProposalReviewTaskService scoreProposalReviewTaskService;
    private final MessageService messageService;
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

    /** 企业参与人员可从用户管理中的全部有效用户选择。 */
    @Override
    public List<PersonUserOptionVo> queryUserOptions() {
        return personProfileMapper.selectUserOptions(DepartmentScope.all());
    }

    @Override
    public PageResult<PersonUserOptionVo> queryUserOptionsPage(PersonUserOptionQueryBo bo, PageQuery pageQuery) {
        Page<PersonUserOptionVo> page = pageQuery.build();
        Page<PersonUserOptionVo> result = personProfileMapper.selectAllUserOptionsPage(
            page,
            bo == null ? new PersonUserOptionQueryBo() : bo,
            DepartmentScope.all()
        );
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<PersonUserOptionVo> queryUserOptionsByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return personProfileMapper.selectUserOptionsByIds(userIds);
    }

    @Override
    public ScoreProposalMetricVo queryMetric(String month) {
        String metricMonth = month == null || month.trim().isEmpty() ? YearMonth.now().toString() : month.trim();
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(metricMonth);
        } catch (DateTimeParseException ex) {
            throw new ServiceException("统计月份格式必须为yyyy-MM");
        }

        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        LocalDateTime beginAt = monthStart.atStartOfDay();
        LocalDateTime endAt = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        Long deptId = requireDept();

        int memberCount = personProfileMapper.countMembersInMonth(deptId, monthStart, monthEnd);
        int approvedCount = scoreProposalMapper.countApprovedInMonth(deptId, beginAt, endAt);
        ScoreProposalMetricVo statusStats = scoreProposalMapper.selectStatusStats(deptId, beginAt, endAt);
        BigDecimal monthlyTarget = BigDecimal.valueOf(memberCount)
            .multiply(new BigDecimal("0.1"))
            .setScale(1, RoundingMode.HALF_UP);

        BigDecimal completionRate = null;
        int score = 0;
        if (monthlyTarget.signum() > 0) {
            completionRate = BigDecimal.valueOf(approvedCount)
                .divide(monthlyTarget, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
            if (completionRate.compareTo(BigDecimal.valueOf(120)) >= 0) {
                score = 2;
            } else if (completionRate.compareTo(BigDecimal.valueOf(100)) >= 0) {
                score = 1;
            } else if (completionRate.compareTo(BigDecimal.valueOf(80)) >= 0) {
                score = 0;
            } else {
                score = -2;
            }
        }

        ScoreProposalMetricVo metric = new ScoreProposalMetricVo();
        metric.setMonth(yearMonth.toString());
        metric.setMemberCount(memberCount);
        metric.setMonthlyTarget(monthlyTarget);
        metric.setApprovedCount(approvedCount);
        if (statusStats != null) {
            metric.setTotalCount(statusStats.getTotalCount());
            metric.setStatusApprovedCount(statusStats.getStatusApprovedCount());
            metric.setPendingCount(statusStats.getPendingCount());
            metric.setPendingConfirmCount(statusStats.getPendingConfirmCount());
            metric.setRejectedCount(statusStats.getRejectedCount());
        }
        metric.setCompletionRate(completionRate);
        metric.setScore(score);
        return metric;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ScoreProposalBo bo) {
        Long deptId = requireDept();
        ScoreProposal entity = new ScoreProposal();
        copyBo(bo, entity, false, deptId, isSubmit(bo));
        entity.setDeptId(deptId);
        entity.setReviewStatus(REVIEW_DRAFT);
        entity.setReviewComment(null);
        entity.setRevisionNo(0);
        if (StringUtils.isBlank(entity.getCompletionStatus())) {
            entity.setCompletionStatus("进行中");
        }
        if (scoreProposalMapper.insert(entity) <= 0) return false;
        if (isSubmit(bo)) return submitEntity(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ScoreProposalBo bo) {
        ScoreProposal entity = getAccessible(bo.getId());
        ensureEditable(entity);
        Long deptId = entity.getDeptId() == null ? requireDept() : entity.getDeptId();
        copyBo(bo, entity, true, deptId, isSubmit(bo));
        entity.setReviewStatus(REVIEW_DRAFT);
        entity.setReviewComment(null);
        if (StringUtils.isBlank(entity.getCompletionStatus())) {
            entity.setCompletionStatus("进行中");
        }
        if (scoreProposalMapper.updateById(entity) <= 0) return false;
        if (isSubmit(bo)) return submitEntity(entity);
        return true;
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
        Long userId = LoginHelper.getUserId();
        String action = StringUtils.trim(bo.getAction()).toUpperCase(Locale.ROOT);
        LocalDateTime now = LocalDateTime.now();
        if ("REVIEW_APPROVE".equals(action) || "REVIEW_REJECT".equals(action)) {
            if (!REVIEW_PENDING.equals(entity.getReviewStatus())) throw new ServiceException("当前提案不在待审核状态");
            ScoreProposalReviewTask task = scoreProposalReviewTaskService.requirePending(entity.getId(), entity.getRevisionNo(), "REVIEW", userId);
            boolean approved = "REVIEW_APPROVE".equals(action);
            entity.setReviewerUserId(userId);
            entity.setReviewedAt(now);
            entity.setReviewComment(bo.getReviewComment());
            entity.setReviewStatus(approved ? REVIEW_PENDING_CONFIRM : REVIEW_REJECTED);
            if (scoreProposalMapper.updateById(entity) <= 0) return false;
            scoreProposalReviewTaskService.complete(task.getId(), userId, approved ? "APPROVED" : "REJECTED", bo.getReviewComment());
            scoreProposalReviewTaskService.cancelOtherTasks(entity.getId(), entity.getRevisionNo(), "REVIEW", task.getId());
            if (approved) {
                scoreProposalReviewTaskService.createStageTasks(entity.getId(), entity.getDeptId(), entity.getRevisionNo(), "CONFIRM", List.of(userId), null);
                notifyUser(entity.getProposerUserId(), "SCORE提案审核通过，请完成现场确认。", entity, "CONFIRM");
            } else {
                notifyUser(entity.getProposerUserId(), "SCORE提案审核未通过，请查看审核意见。", entity, "REVIEW");
            }
            return true;
        }
        if ("CONFIRM_APPROVE".equals(action) || "CONFIRM_REJECT".equals(action)) {
            if (!REVIEW_PENDING_CONFIRM.equals(entity.getReviewStatus())) throw new ServiceException("当前提案不在待现场确认状态");
            ScoreProposalReviewTask task = scoreProposalReviewTaskService.requirePending(entity.getId(), entity.getRevisionNo(), "CONFIRM", userId);
            boolean approved = "CONFIRM_APPROVE".equals(action);
            entity.setReviewStatus(approved ? REVIEW_APPROVED : REVIEW_REJECTED);
            entity.setConfirmComment(bo.getReviewComment());
            entity.setConfirmerUserId(userId);
            entity.setConfirmedAt(now);
            if (scoreProposalMapper.updateById(entity) <= 0) return false;
            scoreProposalReviewTaskService.complete(task.getId(), userId, approved ? "APPROVED" : "REJECTED", bo.getReviewComment());
            notifyUser(entity.getProposerUserId(), approved ? "SCORE提案已通过现场确认。" : "SCORE提案现场确认未通过。", entity, "CONFIRM");
            return true;
        }
        throw new ServiceException("不支持的审核动作");
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
        ScoreProposalVo proposal = queryById(id);
        scoreProposalXlsxService.export(proposal, response);
    }

    @Override
    public String previewXlsx(Long id) throws Exception {
        ScoreProposal entity = getAccessible(id);
        if (entity.getReviewFileOssId() != null) {
            try {
                org.springframework.http.ResponseEntity<byte[]> response = ossService.download(entity.getReviewFileOssId());
                byte[] bytes = response == null ? null : response.getBody();
                if (bytes != null && bytes.length > 0) {
                    return scoreProposalXlsxService.preview(bytes, entity.getReviewFileName());
                }
            } catch (Exception ex) {
                throw new ServiceException("审核文件不存在或无法读取，请重新提交审核");
            }
        }
        return scoreProposalXlsxService.preview(toVo(entity));
    }

    private boolean isSubmit(ScoreProposalBo bo) {
        return "SUBMIT".equalsIgnoreCase(StringUtils.trim(bo.getSaveMode()));
    }

    private void ensureEditable(ScoreProposal entity) {
        if (REVIEW_PENDING.equals(entity.getReviewStatus()) || REVIEW_PENDING_CONFIRM.equals(entity.getReviewStatus())) {
            throw new ServiceException("提案正在审核中，不能直接修改，请等待审核完成");
        }
        if (REVIEW_APPROVED.equals(entity.getReviewStatus())) {
            throw new ServiceException("已通过提案不能直接修改，请另行创建新提案");
        }
    }

    /** 将已保存的草稿转换为一个不可变提交版本，并创建审核任务。 */
    private boolean submitEntity(ScoreProposal entity) {
        validateSubmission(entity);
        List<Long> reviewerIds = departmentTaskService.getReviewerUserIds("SCORE_PROPOSAL", entity.getDeptId());
        int revisionNo = entity.getRevisionNo() == null ? 1 : entity.getRevisionNo() + 1;
        entity.setRevisionNo(revisionNo);
        entity.setReviewStatus(REVIEW_PENDING);
        entity.setReviewComment(null);
        entity.setConfirmComment(null);
        entity.setReviewerUserId(null);
        entity.setConfirmerUserId(null);
        entity.setReviewedAt(null);
        entity.setConfirmedAt(null);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setSubmittedBy(LoginHelper.getUserId());
        scoreProposalMapper.updateById(entity);

        SysOssVo uploaded = null;
        try {
            refreshCurrentProposerRole(entity);
            ScoreProposalXlsxService.GeneratedXlsx generated = scoreProposalXlsxService.generate(toVo(entity));
            SysOssExt ext = new SysOssExt();
            ext.setBizType("DEPARTMENT_SCORE_PROPOSAL");
            ext.setSource("systemSubmit");
            ext.setRefType("SCORE_PROPOSAL");
            ext.setRefId(String.valueOf(entity.getId()));
            ext.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            ext.setRemark("SCORE提案提交审核文件，版本V" + revisionNo);
            uploaded = ossService.upload(new ByteArrayMultipartFile(generated.fileName(), generated.bytes()), ext);
            entity.setReviewFileOssId(uploaded.getOssId());
            entity.setReviewFileName(uploaded.getOriginalName());
            scoreProposalMapper.updateById(entity);
            scoreProposalReviewTaskService.createStageTasks(entity.getId(), entity.getDeptId(), revisionNo, "REVIEW", reviewerIds, null);
            notifyUsers(reviewerIds, "收到新的SCORE提案，请及时审核。", entity, "REVIEW");
            return true;
        } catch (Exception ex) {
            if (uploaded != null && uploaded.getOssId() != null) {
                try { ossService.deleteWithValidByIds(List.of(uploaded.getOssId()), false); } catch (Exception ignored) { }
            }
            throw ex instanceof ServiceException ? (ServiceException) ex : new ServiceException("提交审核失败：" + ex.getMessage());
        }
    }

    private void notifyUser(Long userId, String message, ScoreProposal entity, String stage) {
        if (userId != null) notifyUsers(List.of(userId), message, entity, stage);
    }

    private void notifyUsers(List<Long> userIds, String message, ScoreProposal entity, String stage) {
        if (userIds == null || userIds.isEmpty()) return;
        org.dromara.system.api.domain.PushPayloadDTO payload = org.dromara.system.api.domain.PushPayloadDTO.of(
            "MESSAGE", "BACKEND", message,
            Map.of("proposalId", entity.getId(), "stage", stage, "revisionNo", entity.getRevisionNo()));
        payload.setPath("/department/scoreProposal/proposal?id=" + entity.getId() + "&mode=review&stage=" + stage);
        // 消息失败不能回滚已完成的审核动作，任务表和提案状态才是业务事实来源。
        try { messageService.publishMessage(userIds, payload); } catch (Exception ignored) { }
    }

    private static class ByteArrayMultipartFile implements MultipartFile {
        private final String filename;
        private final byte[] content;

        private ByteArrayMultipartFile(String filename, byte[] content) {
            this.filename = filename;
            this.content = content == null ? new byte[0] : content;
        }

        @Override public String getName() { return "file"; }
        @Override public String getOriginalFilename() { return filename; }
        @Override public String getContentType() { return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; }
        @Override public boolean isEmpty() { return content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() { return content; }
        @Override public InputStream getInputStream() { return new java.io.ByteArrayInputStream(content); }
        @Override public void transferTo(File dest) throws IOException { try (FileOutputStream out = new FileOutputStream(dest)) { out.write(content); } }
    }

    private void copyBo(ScoreProposalBo bo, ScoreProposal entity, boolean updating, Long targetDeptId, boolean submitting) {
        entity.setCompanyName(bo.getCompanyName());
        entity.setTeamMemberUserIds(JsonUtils.toJsonString(resolveTeamMemberUserIds(bo.getTeamMemberUserIds())));
        applyProposer(bo, entity, targetDeptId, updating);
        entity.setProposerLevel(bo.getProposerLevel());
        entity.setDeptName(resolveDeptName(targetDeptId));
        applyImplementers(bo, entity, targetDeptId, updating);
        boolean noCategoryForDraft = !submitting
            && bo.getMainCategoryId() == null && bo.getSubCategoryId() == null
            && StringUtils.isBlank(bo.getMainCategory()) && StringUtils.isBlank(bo.getSubCategory());
        if (noCategoryForDraft) {
            entity.setMainCategoryId(null);
            entity.setSubCategoryId(null);
            entity.setMainCategory(null);
            entity.setSubCategory(null);
        } else {
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
        }
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

    private void validateSubmission(ScoreProposal entity) {
        if (entity.getMainCategoryId() == null || entity.getSubCategoryId() == null
            || StringUtils.isBlank(entity.getMainCategory()) || StringUtils.isBlank(entity.getSubCategory())) {
            throw new ServiceException("提交审核前请选择提案大类和提案小类");
        }
        if (StringUtils.isBlank(entity.getProblemDescription())) {
            throw new ServiceException("提交审核前请填写问题描述");
        }
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

    /** 企业参与人员只保存用户ID，人员姓名和印尼语姓名由用户管理实时提供。 */
    private List<Long> resolveTeamMemberUserIds(List<Long> userIds) {
        List<Long> selectedIds = userIds == null ? List.of() : userIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (selectedIds.isEmpty()) return selectedIds;

        Map<Long, PersonUserOptionVo> usersById = personProfileMapper.selectUserOptionsByIds(selectedIds).stream()
            .filter(item -> item.getUserId() != null)
            .collect(Collectors.toMap(PersonUserOptionVo::getUserId, item -> item, (left, right) -> left, LinkedHashMap::new));
        if (!usersById.keySet().containsAll(selectedIds)) {
            throw new ServiceException("企业参与人员必须来自用户管理中的有效人员");
        }
        return selectedIds;
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

    private List<Long> parseTeamMemberUserIds(String json) {
        if (StringUtils.isBlank(json)) {
            return List.of();
        }
        try {
            return JsonUtils.parseArray(json, Long.class);
        } catch (Exception ignored) {
            // 旧版本保存的是人员名称，新版本不再兼容名称快照，按未选择处理。
            return List.of();
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
        vo.setTeamMemberUserIds(parseTeamMemberUserIds(entity.getTeamMemberUserIds()));
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
        vo.setReviewedAt(entity.getReviewedAt());
        vo.setReviewFileOssId(entity.getReviewFileOssId());
        vo.setReviewFileName(entity.getReviewFileName());
        vo.setRevisionNo(entity.getRevisionNo());
        vo.setSubmittedAt(entity.getSubmittedAt());
        vo.setSubmittedBy(entity.getSubmittedBy());
        vo.setConfirmComment(entity.getConfirmComment());
        vo.setConfirmerUserId(entity.getConfirmerUserId());
        vo.setConfirmedAt(entity.getConfirmedAt());
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
