package org.dromara.department.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HtmlUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.PlatformFeedback;
import org.dromara.department.domain.PlatformFeedbackActivity;
import org.dromara.department.domain.PlatformFeedbackComment;
import org.dromara.department.domain.bo.PlatformFeedbackBo;
import org.dromara.department.domain.bo.PlatformFeedbackCommentBo;
import org.dromara.department.domain.bo.PlatformFeedbackHandlerConfigBo;
import org.dromara.department.domain.bo.PlatformFeedbackProcessBo;
import org.dromara.department.domain.bo.PlatformFeedbackQueryBo;
import org.dromara.department.domain.vo.PlatformFeedbackActivityVo;
import org.dromara.department.domain.vo.PlatformFeedbackAttachmentVo;
import org.dromara.department.domain.vo.PlatformFeedbackCommentVo;
import org.dromara.department.domain.vo.PlatformFeedbackSummaryVo;
import org.dromara.department.domain.vo.PlatformFeedbackUserOptionVo;
import org.dromara.department.domain.vo.PlatformFeedbackVo;
import org.dromara.department.mapper.PlatformFeedbackActivityMapper;
import org.dromara.department.mapper.PlatformFeedbackCommentMapper;
import org.dromara.department.mapper.PlatformFeedbackMapper;
import org.dromara.system.api.domain.PushPayloadDTO;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.bo.SysConfigBo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.domain.vo.SysConfigVo;
import org.dromara.system.service.ISysConfigService;
import org.dromara.system.service.ISysMessageService;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 平台问题与建议业务实现。
 */
@RequiredArgsConstructor
@Service
public class PlatformFeedbackServiceImpl implements org.dromara.department.service.IPlatformFeedbackService {

    private static final String PERMISSION_MANAGE = "department:platformFeedback:manage";
    private static final String PERMISSION_PROCESS = "department:platformFeedback:process";
    private static final String HANDLER_CONFIG_KEY = "department.platformFeedback.handlers";
    private static final String HANDLER_CONFIG_NAME = "问题与建议处理人";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_WAITING_CONFIRM = "WAITING_CONFIRM";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_DUPLICATE = "DUPLICATE";
    private static final String STATUS_EVALUATING = "EVALUATING";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_DEFERRED = "DEFERRED";
    private static final String STATUS_NOT_ACCEPTED = "NOT_ACCEPTED";
    private static final String TYPE_SYSTEM = "SYSTEM";
    private static final String TYPE_DATA = "DATA";
    private static final String TYPE_PROCESS = "PROCESS";
    private static final String TYPE_QUESTION = "QUESTION";
    private static final String TYPE_SUGGESTION = "SUGGESTION";
    private static final String PRIORITY_LOW = "LOW";
    private static final String PRIORITY_NORMAL = "NORMAL";
    private static final String PRIORITY_HIGH = "HIGH";
    private static final String PRIORITY_URGENT = "URGENT";
    private static final int MAX_ATTACHMENTS = 5;
    private static final long MAX_ATTACHMENT_SIZE = 50L * 1024 * 1024;
    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Set<String> TYPES = Set.of(TYPE_SYSTEM, TYPE_DATA, TYPE_PROCESS, TYPE_QUESTION, TYPE_SUGGESTION);
    private static final Set<String> PRIORITIES = Set.of(PRIORITY_LOW, PRIORITY_NORMAL, PRIORITY_HIGH, PRIORITY_URGENT);
    private static final Set<String> STATUSES = Set.of(STATUS_PENDING, STATUS_PROCESSING, STATUS_WAITING_CONFIRM,
        STATUS_CLOSED, STATUS_REJECTED, STATUS_DUPLICATE, STATUS_EVALUATING, STATUS_ACCEPTED, STATUS_DEFERRED, STATUS_NOT_ACCEPTED);

    private final PlatformFeedbackMapper feedbackMapper;
    private final PlatformFeedbackCommentMapper commentMapper;
    private final PlatformFeedbackActivityMapper activityMapper;
    private final ISysOssService ossService;
    private final ISysConfigService sysConfigService;
    private final ISysMessageService messageService;

    @Override
    public PageResult<PlatformFeedbackVo> queryPageList(PlatformFeedbackQueryBo bo, PageQuery pageQuery) {
        PlatformFeedbackQueryBo query = bo == null ? new PlatformFeedbackQueryBo() : bo;
        PageQuery page = pageQuery == null ? new PageQuery() : pageQuery;
        Long userId = LoginHelper.getUserId();
        var result = feedbackMapper.selectPageList(page.build(), query, userId, isManager(), isConfiguredHandler(userId));
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public PlatformFeedbackVo queryById(Long id) {
        Long userId = LoginHelper.getUserId();
        PlatformFeedbackVo result = feedbackMapper.selectDetailById(id, userId, isManager(), isConfiguredHandler(userId));
        if (result == null) {
            throw new ServiceException("反馈不存在或您没有访问权限");
        }
        result.setAttachments(buildAttachments(result.getAttachmentOssIds()));
        result.setProcessAllowed(isProcessAllowed(userId));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(PlatformFeedbackBo bo) {
        validateForm(bo);
        List<Long> attachmentIds = parseAttachmentIds(bo.getAttachmentOssIds());
        validateAttachments(attachmentIds);
        PlatformFeedback entity = new PlatformFeedback();
        entity.setFeedbackType(normalizeType(bo.getFeedbackType()));
        entity.setTitle(clean(bo.getTitle()));
        entity.setDescription(clean(bo.getDescription()));
        entity.setModuleName(clean(bo.getModuleName()));
        entity.setPageTitle(clean(bo.getPageTitle()));
        entity.setPagePath(clean(bo.getPagePath()));
        entity.setBusinessRef(clean(bo.getBusinessRef()));
        entity.setReproduceSteps(clean(bo.getReproduceSteps()));
        entity.setExpectedResult(clean(bo.getExpectedResult()));
        entity.setActualResult(clean(bo.getActualResult()));
        entity.setImpactScope(normalizeValue(bo.getImpactScope(), "SELF"));
        entity.setPriority(normalizePriority(bo.getPriority()));
        entity.setStatus(STATUS_PENDING);
        entity.setAttachmentOssIds(joinAttachmentIds(attachmentIds));
        entity.setCreateDept(LoginHelper.getDeptId());
        entity.setCreateBy(LoginHelper.getUserId());
        // feedback_no 为非空字段，必须在首次 INSERT 前生成；同时显式复用主键保证编号稳定且唯一。
        entity.setId(IdGeneratorUtil.nextLongId());
        entity.setFeedbackNo("FB" + LocalDate.now().format(NO_FORMATTER) + entity.getId());
        if (feedbackMapper.insert(entity) <= 0) {
            return false;
        }
        addActivity(entity.getId(), "CREATED", "提交了" + typeLabel(entity.getFeedbackType()), null, STATUS_PENDING);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean process(PlatformFeedbackProcessBo bo) {
        if (bo == null || bo.getId() == null) {
            throw new ServiceException("反馈主键不能为空");
        }
        Long currentUserId = LoginHelper.getUserId();
        if (!isProcessAllowed(currentUserId)) {
            throw new ServiceException("只有已配置的反馈处理人可以保存处理进展");
        }
        PlatformFeedback entity = getAccessibleEntity(bo.getId());
        String nextStatus = StringUtils.isBlank(bo.getStatus()) ? entity.getStatus() : bo.getStatus().trim().toUpperCase();
        if (!STATUSES.contains(nextStatus)) {
            throw new ServiceException("不支持的反馈状态");
        }
        String previousStatus = entity.getStatus();
        entity.setStatus(nextStatus);
        String note = clean(bo.getNote());
        if (StringUtils.isNotBlank(note)) {
            entity.setResolutionNote(note);
        }
        if (STATUS_CLOSED.equals(nextStatus)) {
            entity.setClosedAt(LocalDateTime.now());
        } else if (STATUS_PENDING.equals(nextStatus) || STATUS_PROCESSING.equals(nextStatus)
            || STATUS_WAITING_CONFIRM.equals(nextStatus) || STATUS_EVALUATING.equals(nextStatus)) {
            entity.setClosedAt(null);
        }
        boolean updated = feedbackMapper.updateById(entity) > 0;
        if (!updated) {
            return false;
        }
        if (!Objects.equals(previousStatus, nextStatus)) {
            addActivity(entity.getId(), "STATUS_CHANGED", statusLabel(nextStatus), previousStatus, nextStatus);
            notifyUser(entity.getCreateBy(), "你的反馈状态已更新为“" + statusLabel(nextStatus) + "”：“" + entity.getTitle() + "”", entity.getId());
        }
        if (StringUtils.isNotBlank(note)) {
            addActivity(entity.getId(), "NOTE", note, previousStatus, nextStatus);
        }
        return true;
    }

    @Override
    public PageResult<PlatformFeedbackCommentVo> queryComments(Long feedbackId, PageQuery pageQuery) {
        getAccessible(feedbackId);
        PageQuery page = pageQuery == null ? new PageQuery(20, 1) : pageQuery;
        var result = commentMapper.selectPageByFeedbackId(page.build(), feedbackId, LoginHelper.getUserId());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addComment(Long feedbackId, PlatformFeedbackCommentBo bo) {
        PlatformFeedback entity = getAccessibleEntity(feedbackId);
        if (bo == null || StringUtils.isBlank(bo.getContent())) {
            throw new ServiceException("评论内容不能为空");
        }
        String content = clean(bo.getContent());
        if (StringUtils.isBlank(content)) {
            throw new ServiceException("评论内容不能为空");
        }
        PlatformFeedbackComment comment = new PlatformFeedbackComment();
        comment.setFeedbackId(feedbackId);
        comment.setContent(content);
        comment.setCreateDept(LoginHelper.getDeptId());
        comment.setCreateBy(LoginHelper.getUserId());
        boolean inserted = commentMapper.insert(comment) > 0;
        if (inserted) {
            addActivity(feedbackId, "COMMENT", "补充了一条处理评论", entity.getStatus(), entity.getStatus());
            notifyUser(entity.getCreateBy(), "有人回复了你的反馈：“" + entity.getTitle() + "”", feedbackId);
            notifyUser(entity.getAssigneeId(), "你负责的反馈有新的评论：“" + entity.getTitle() + "”", feedbackId);
        }
        return inserted;
    }

    @Override
    public List<PlatformFeedbackActivityVo> queryActivities(Long feedbackId) {
        getAccessible(feedbackId);
        return activityMapper.selectListByFeedbackId(feedbackId);
    }

    @Override
    public PlatformFeedbackSummaryVo querySummary() {
        Long userId = LoginHelper.getUserId();
        return feedbackMapper.selectSummary(userId, isManager(), isConfiguredHandler(userId));
    }

    @Override
    public List<PlatformFeedbackUserOptionVo> queryConfiguredHandlers() {
        return getConfiguredHandlerIds().stream()
            .map(feedbackMapper::selectActiveUserOption)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateConfiguredHandlers(PlatformFeedbackHandlerConfigBo bo) {
        assertManager();
        if (bo == null || bo.getUserIds() == null) {
            throw new ServiceException("请配置反馈处理人");
        }
        List<Long> requestedIds = bo.getUserIds();
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>(requestedIds);
        if (uniqueIds.size() != requestedIds.size() || uniqueIds.size() < 2 || uniqueIds.size() > 3 || uniqueIds.contains(null)) {
            throw new ServiceException("反馈处理人必须配置2至3名有效用户");
        }
        for (Long userId : uniqueIds) {
            if (feedbackMapper.selectActiveUserOption(userId) == null) {
                throw new ServiceException("反馈处理人不存在或已停用");
            }
        }
        String value = uniqueIds.stream().map(String::valueOf).reduce((left, right) -> left + "," + right).orElseThrow();
        SysConfigBo config = new SysConfigBo();
        config.setConfigName(HANDLER_CONFIG_NAME);
        config.setConfigKey(HANDLER_CONFIG_KEY);
        config.setConfigValue(value);
        config.setConfigType("Y");
        config.setRemark("由反馈管理员配置，允许2至3名用户保存问题与建议处理进展");
        SysConfigBo query = new SysConfigBo();
        query.setConfigKey(HANDLER_CONFIG_KEY);
        List<SysConfigVo> configs = sysConfigService.selectConfigList(query);
        if (configs.isEmpty()) {
            sysConfigService.insertConfig(config);
        } else {
            config.setConfigId(configs.get(0).getConfigId());
            sysConfigService.updateConfig(config);
        }
        return true;
    }

    @Override
    public List<PlatformFeedbackUserOptionVo> queryUserOptions(String keyword) {
        assertManager();
        return feedbackMapper.selectUserOptions(StringUtils.trim(keyword));
    }

    @Override
    public PlatformFeedbackAttachmentVo uploadAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("附件不能为空");
        }
        if (file.getSize() > MAX_ATTACHMENT_SIZE) {
            throw new ServiceException("附件不能超过50MB");
        }
        SysOssExt ext = new SysOssExt();
        ext.setBizType("PLATFORM_FEEDBACK");
        ext.setSource("userUpload");
        ext.setRefType("PLATFORM_FEEDBACK");
        ext.setIsTemp(false);
        SysOssVo oss = ossService.upload(file, ext);
        PlatformFeedbackAttachmentVo result = new PlatformFeedbackAttachmentVo();
        result.setOssId(oss.getOssId());
        result.setOriginalName(oss.getOriginalName());
        result.setFileSuffix(oss.getFileSuffix());
        result.setFileSize(file.getSize());
        result.setUrl(ossService.previewUrl(oss.getOssId()));
        return result;
    }

    private PlatformFeedback getAccessibleEntity(Long id) {
        PlatformFeedback entity = getAccessible(id);
        return entity;
    }

    private PlatformFeedback getAccessible(Long id) {
        if (id == null) {
            throw new ServiceException("反馈主键不能为空");
        }
        Long userId = LoginHelper.getUserId();
        PlatformFeedbackVo vo = feedbackMapper.selectDetailById(id, userId, isManager(), isConfiguredHandler(userId));
        if (vo == null) {
            throw new ServiceException("反馈不存在或您没有访问权限");
        }
        PlatformFeedback entity = feedbackMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("反馈不存在");
        }
        return entity;
    }

    private void validateForm(PlatformFeedbackBo bo) {
        if (bo == null) {
            throw new ServiceException("反馈内容不能为空");
        }
        if (!TYPES.contains(normalizeType(bo.getFeedbackType()))) {
            throw new ServiceException("不支持的反馈类型");
        }
        if (StringUtils.isBlank(bo.getTitle()) || StringUtils.isBlank(bo.getDescription())) {
            throw new ServiceException("标题和描述不能为空");
        }
    }

    private void validateAttachments(List<Long> ids) {
        if (ids.size() > MAX_ATTACHMENTS) {
            throw new ServiceException("最多上传5个附件");
        }
        Long userId = LoginHelper.getUserId();
        for (Long id : ids) {
            SysOssVo oss = ossService.getById(id);
            if (oss == null || (oss.getCreateBy() != null && !Objects.equals(oss.getCreateBy(), userId))) {
                throw new ServiceException("附件不存在或无权使用");
            }
        }
    }

    private List<PlatformFeedbackAttachmentVo> buildAttachments(String attachmentOssIds) {
        List<Long> ids = parseAttachmentIds(attachmentOssIds);
        List<PlatformFeedbackAttachmentVo> result = new ArrayList<>();
        for (Long id : ids) {
            SysOssVo oss = ossService.getById(id);
            if (oss == null) {
                continue;
            }
            PlatformFeedbackAttachmentVo item = new PlatformFeedbackAttachmentVo();
            item.setOssId(id);
            item.setOriginalName(oss.getOriginalName());
            item.setFileSuffix(oss.getFileSuffix());
            item.setUrl(ossService.previewUrl(id));
            result.add(item);
        }
        return result;
    }

    private List<Long> parseAttachmentIds(String value) {
        if (StringUtils.isBlank(value)) {
            return List.of();
        }
        Set<Long> ids = new LinkedHashSet<>();
        Arrays.stream(value.split("[,，]"))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .forEach(item -> {
                try {
                    ids.add(Long.valueOf(item));
                } catch (NumberFormatException ex) {
                    throw new ServiceException("附件参数无效");
                }
            });
        return new ArrayList<>(ids);
    }

    private String joinAttachmentIds(List<Long> ids) {
        return ids.isEmpty() ? null : ids.stream().map(String::valueOf).reduce((left, right) -> left + "," + right).orElse(null);
    }

    private void addActivity(Long feedbackId, String actionType, String actionNote, String fromStatus, String toStatus) {
        PlatformFeedbackActivity activity = new PlatformFeedbackActivity();
        activity.setFeedbackId(feedbackId);
        activity.setActionType(actionType);
        activity.setActionNote(actionNote);
        activity.setFromStatus(fromStatus);
        activity.setToStatus(toStatus);
        activity.setCreateDept(LoginHelper.getDeptId());
        activity.setCreateBy(LoginHelper.getUserId());
        activityMapper.insert(activity);
    }

    private void assertManager() {
        if (!isManager()) {
            throw new ServiceException("您没有管理反馈的权限");
        }
    }

    private boolean isManager() {
        return LoginHelper.isSuperAdmin() || StpUtil.hasPermission(PERMISSION_MANAGE);
    }

    private boolean isConfiguredHandler(Long userId) {
        return userId != null && getConfiguredHandlerIds().contains(userId);
    }

    private boolean isProcessAllowed(Long userId) {
        return isConfiguredHandler(userId)
            && (LoginHelper.isSuperAdmin() || StpUtil.hasPermission(PERMISSION_PROCESS));
    }

    private List<Long> getConfiguredHandlerIds() {
        String value = sysConfigService.selectConfigByKey(HANDLER_CONFIG_KEY);
        if (StringUtils.isBlank(value)) {
            return List.of();
        }
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (String item : value.split(",")) {
            try {
                if (StringUtils.isNotBlank(item)) {
                    ids.add(Long.valueOf(item.trim()));
                }
            } catch (NumberFormatException ignored) {
                // 忽略历史配置中的非法用户ID，避免影响反馈查询。
            }
        }
        return new ArrayList<>(ids);
    }

    private String clean(String value) {
        return value == null ? null : HtmlUtil.cleanHtmlTag(StringUtils.trim(value));
    }

    private String normalizeType(String value) {
        String type = StringUtils.isBlank(value) ? TYPE_QUESTION : value.trim().toUpperCase();
        return TYPES.contains(type) ? type : type;
    }

    private String normalizePriority(String value) {
        String priority = StringUtils.isBlank(value) ? PRIORITY_NORMAL : value.trim().toUpperCase();
        return PRIORITIES.contains(priority) ? priority : PRIORITY_NORMAL;
    }

    private String normalizeValue(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value.trim().toUpperCase();
    }

    private String typeLabel(String type) {
        return Map.of(TYPE_SYSTEM, "系统问题", TYPE_DATA, "数据问题", TYPE_PROCESS, "流程问题", TYPE_QUESTION, "使用疑问", TYPE_SUGGESTION, "改进建议")
            .getOrDefault(type, "问题与建议");
    }

    private String statusLabel(String status) {
        return Map.of(STATUS_PENDING, "待确认", STATUS_PROCESSING, "处理中", STATUS_WAITING_CONFIRM, "待提交人确认",
                STATUS_CLOSED, "已关闭", STATUS_REJECTED, "已驳回", STATUS_DUPLICATE, "重复问题", STATUS_EVALUATING, "评估中",
                STATUS_ACCEPTED, "已采纳", STATUS_DEFERRED, "暂不处理", STATUS_NOT_ACCEPTED, "不采纳")
            .getOrDefault(status, status);
    }

    private void notifyUser(Long userId, String message, Long feedbackId) {
        if (userId == null || Objects.equals(userId, LoginHelper.getUserId())) {
            return;
        }
        try {
            PushPayloadDTO payload = PushPayloadDTO.of("MESSAGE", "BACKEND", message,
                Map.of("feedbackId", feedbackId, "module", "PLATFORM_FEEDBACK"));
            payload.setPath("/department/platformFeedback/index?id=" + feedbackId);
            messageService.publishMessage(List.of(userId), payload);
        } catch (Exception ignored) {
            // 通知失败不影响反馈记录保存。
        }
    }
}
