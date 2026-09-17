package org.dromara.department.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.enums.PushSourceEnum;
import org.dromara.common.core.enums.PushTypeEnum;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mail.config.properties.MailProperties;
import org.dromara.common.mail.core.MailBuilder;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.config.RoomBookingNotificationProperties;
import org.dromara.system.api.MessageService;
import org.dromara.system.api.UserService;
import org.dromara.system.api.domain.PushPayloadDTO;
import org.dromara.department.domain.RoomBooking;
import org.dromara.department.domain.RoomBookingBlock;
import org.dromara.department.domain.RoomBookingApproval;
import org.dromara.department.domain.RoomBookingBlacklist;
import org.dromara.department.domain.RoomBookingRecurrenceException;
import org.dromara.department.domain.RoomApprovalRule;
import org.dromara.department.domain.RoomQuotaPolicy;
import org.dromara.department.domain.RoomQuotaUsage;
import org.dromara.department.domain.RoomAmenity;
import org.dromara.department.domain.RoomAmenityRelation;
import org.dromara.department.domain.RoomBookingAttendee;
import org.dromara.department.domain.RoomBookingOccurrence;
import org.dromara.department.domain.RoomCalendarException;
import org.dromara.department.domain.RoomResource;
import org.dromara.department.domain.RoomResourceAcl;
import org.dromara.department.domain.RoomFloorPlan;
import org.dromara.department.domain.RoomFloorPlanVersion;
import org.dromara.department.domain.bo.RoomBookingBo;
import org.dromara.department.domain.bo.RoomBookingQueryBo;
import org.dromara.department.domain.bo.RoomCalendarExceptionBo;
import org.dromara.department.domain.bo.RoomCalendarExceptionQueryBo;
import org.dromara.department.domain.bo.RoomResourceBo;
import org.dromara.department.domain.bo.RoomResourceQueryBo;
import org.dromara.department.domain.bo.RoomResourceAclBo;
import org.dromara.department.domain.bo.RoomBookingExtendBo;
import org.dromara.department.domain.bo.RoomBookingBlockBo;
import org.dromara.department.domain.bo.RoomBookingBlacklistBo;
import org.dromara.department.domain.bo.RoomResourceMergeBo;
import org.dromara.department.domain.bo.RoomApprovalRuleBo;
import org.dromara.department.domain.bo.RoomQuotaPolicyBo;
import org.dromara.department.domain.bo.RoomAmenityBo;
import org.dromara.department.domain.bo.RoomFloorPlanBo;
import org.dromara.department.domain.bo.RoomFloorPlanQueryBo;
import org.dromara.department.domain.bo.RoomAnalyticsQueryBo;
import org.dromara.department.domain.vo.RoomCalendarExceptionVo;
import org.dromara.department.domain.vo.RoomBookingDetailVo;
import org.dromara.department.domain.vo.RoomBookingVo;
import org.dromara.department.domain.vo.RoomConflictResultVo;
import org.dromara.department.domain.vo.RoomConflictVo;
import org.dromara.department.domain.vo.RoomResourceVo;
import org.dromara.department.domain.vo.RoomUserOptionVo;
import org.dromara.department.domain.vo.RoomResourceAclVo;
import org.dromara.department.domain.vo.RoomBookingBlockVo;
import org.dromara.department.domain.vo.RoomFloorPlanVo;
import org.dromara.department.domain.vo.RoomFloorPlanVersionVo;
import org.dromara.department.domain.vo.RoomAnalyticsRoomVo;
import org.dromara.department.domain.vo.RoomAnalyticsSummaryVo;
import org.dromara.department.domain.vo.RoomAnalyticsVo;
import org.dromara.department.domain.vo.RoomAnalyticsDimensionVo;
import org.dromara.department.domain.vo.RoomAnalyticsHotSlotVo;
import org.dromara.department.domain.vo.RoomAnalyticsApprovalVo;
import org.dromara.department.domain.vo.RoomApprovalRuleVo;
import org.dromara.department.domain.vo.RoomQuotaPolicyVo;
import org.dromara.department.domain.vo.RoomQuotaUsageVo;
import org.dromara.department.domain.vo.RoomBookingBlacklistVo;
import org.dromara.department.domain.vo.RoomBookingRecurrenceExceptionVo;
import org.dromara.department.domain.vo.RoomQuotaUsageRecordVo;
import org.dromara.department.domain.vo.RoomAmenityVo;
import org.dromara.department.mapper.RoomBookingAttendeeMapper;
import org.dromara.department.mapper.RoomBookingBlacklistMapper;
import org.dromara.department.mapper.RoomBookingApprovalMapper;
import org.dromara.department.mapper.RoomBookingRecurrenceExceptionMapper;
import org.dromara.department.mapper.RoomBookingMapper;
import org.dromara.department.mapper.RoomBookingOccurrenceMapper;
import org.dromara.department.mapper.RoomCalendarExceptionMapper;
import org.dromara.department.mapper.RoomResourceMapper;
import org.dromara.department.mapper.RoomResourceAclMapper;
import org.dromara.department.mapper.RoomBookingBlockMapper;
import org.dromara.department.mapper.RoomApprovalRuleMapper;
import org.dromara.department.mapper.RoomQuotaPolicyMapper;
import org.dromara.department.mapper.RoomQuotaUsageMapper;
import org.dromara.department.mapper.RoomAmenityMapper;
import org.dromara.department.mapper.RoomAmenityRelationMapper;
import org.dromara.department.mapper.RoomFloorPlanMapper;
import org.dromara.department.mapper.RoomFloorPlanVersionMapper;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.IRoomBookingService;
import org.dromara.department.service.RoomBookingPermissionEvaluator;
import org.dromara.department.service.RoomBookingPrivacyEvaluator;
import org.dromara.department.service.RoomBookingApprovalEvaluator;
import org.dromara.department.service.RoomBookingRecurrenceCalculator;
import org.dromara.department.service.RoomBookingTransactionPublisher;
import org.dromara.department.service.RoomFloorPlanMapDataValidator;
import org.dromara.department.service.RoomFloorPlanMapDataSanitizer;
import org.dromara.department.service.RoomFloorPlanMapDataBindingValidator;
import org.dromara.department.service.RoomQuotaBillingEvaluator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** 房间资源与预约业务实现。 */
@RequiredArgsConstructor
@Service
public class RoomBookingServiceImpl implements IRoomBookingService {

    private static final String ROOM_ENABLED = "ENABLED";
    private static final String ROOM_MAINTENANCE = "MAINTENANCE";
    private static final String ROOM_DISABLED = "DISABLED";
    private static final String SCOPE_PUBLIC = "PUBLIC";
    private static final String SCOPE_DEPT = "DEPT";
    private static final String APPROVAL_NONE = "NONE";
    private static final String APPROVAL_REQUIRED = "APPROVAL";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_IN_USE = "IN_USE";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String PERMISSION_VIEW = "VIEW";
    private static final String PERMISSION_BOOK = "BOOK";
    private static final String PERMISSION_APPROVE = "APPROVE";
    private static final String SUBJECT_USER = "USER";
    private static final String SUBJECT_DEPT = "DEPT";
    private static final String SUBJECT_ROLE = "ROLE";
    private static final String SUBJECT_ALL = "ALL";
    private static final int CHECK_IN_GRACE_MINUTES = 15;
    private static final int DEFAULT_APPROVAL_TIMEOUT_MINUTES = 1440;
    private static final String OPERATION_SERIES = "SERIES";
    private static final String OPERATION_OCCURRENCE = "OCCURRENCE";
    private static final String EXCEPTION_HOLIDAY = "HOLIDAY";
    private static final String EXCEPTION_WORKDAY = "WORKDAY";
    private static final String RECURRENCE_NONE = "NONE";
    private static final int MAX_OCCURRENCES = 100;

    private record QuotaBucket(RoomQuotaPolicy policy, Long roomId, LocalDateTime periodStart, LocalDateTime periodEnd) {
    }

    private final RoomResourceMapper roomResourceMapper;
    private final RoomResourceAclMapper roomResourceAclMapper;
    private final RoomBookingBlacklistMapper blacklistMapper;
    private final RoomBookingBlockMapper roomBookingBlockMapper;
    private final RoomApprovalRuleMapper approvalRuleMapper;
    private final RoomQuotaPolicyMapper quotaPolicyMapper;
    private final RoomQuotaUsageMapper quotaUsageMapper;
    private final RoomAmenityMapper amenityMapper;
    private final RoomAmenityRelationMapper amenityRelationMapper;
    private final RoomFloorPlanMapper roomFloorPlanMapper;
    private final RoomFloorPlanVersionMapper roomFloorPlanVersionMapper;
    private final RoomBookingMapper roomBookingMapper;
    private final RoomBookingOccurrenceMapper occurrenceMapper;
    private final RoomBookingAttendeeMapper attendeeMapper;
    private final RoomBookingApprovalMapper approvalMapper;
    private final RoomBookingRecurrenceExceptionMapper recurrenceExceptionMapper;
    private final RoomCalendarExceptionMapper calendarExceptionMapper;
    private final DepartmentAccessService departmentAccessService;
    private final MessageService messageService;
    private final UserService userService;
    private final MailProperties mailProperties;
    private final RoomBookingNotificationProperties notificationProperties;

    @Override
    public PageResult<RoomResourceVo> queryRoomPage(RoomResourceQueryBo bo, PageQuery pageQuery) {
        Page<RoomResourceVo> page = pageQuery.build();
        Page<RoomResourceVo> result = roomResourceMapper.selectPageList(
            page,
            bo == null ? new RoomResourceQueryBo() : bo,
            departmentAccessService.currentDeptId(),
            LoginHelper.getUserId(),
            canManageAll(),
            StpUtil.hasPermission("department:room:manage")
        );
        return PageResult.build(decorateRoomList(result.getRecords()), result.getTotal());
    }

    @Override
    public List<RoomResourceVo> queryRoomOptions() {
        return decorateRoomList(roomResourceMapper.selectOptions(departmentAccessService.currentDeptId(), LoginHelper.getUserId(), canManageAll()));
    }

    @Override
    public List<RoomResourceVo> queryRoomViewOptions() {
        return decorateRoomList(roomResourceMapper.selectViewOptions(
            departmentAccessService.currentDeptId(), LoginHelper.getUserId(), canManageAll(),
            StpUtil.hasPermission("department:room:manage")
        ));
    }

    @Override
    public RoomResourceVo queryRoomById(Long id) {
        RoomResource room = getRoom(id, false);
        return toRoomVo(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertRoom(RoomResourceBo bo) {
        Long deptId = requireDepartment("新增房间");
        requireRoomManage();
        RoomResource entity = new RoomResource();
        entity.setDeptId(deptId);
        copyRoomBo(bo, entity);
        validateRoomPolicy(entity);
        checkRoomDuplicate(entity);
        boolean saved = roomResourceMapper.insert(entity) > 0;
        if (saved) {
            syncRoomAmenities(entity);
            notifyRoomResourceChanged("CREATE");
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRoom(RoomResourceBo bo) {
        requireRoomManage();
        RoomResource entity = getRoom(bo.getId(), true);
        copyRoomBo(bo, entity);
        validateRoomPolicy(entity);
        checkRoomDuplicate(entity);
        boolean saved = roomResourceMapper.updateById(entity) > 0;
        if (saved) {
            syncRoomAmenities(entity);
            notifyRoomResourceChanged("UPDATE");
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRooms(Collection<Long> ids) {
        requireRoomManage();
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            RoomResource room = getRoom(id, true);
            if (roomResourceMapper.countBookings(room.getId()) > 0) {
                throw new ServiceException("房间已有预约或历史使用记录，不能删除，请改为停用");
            }
        }
        boolean deleted = roomResourceMapper.deleteByIds(ids) > 0;
        if (deleted) {
            notifyRoomResourceChanged("DELETE");
        }
        return deleted;
    }

    @Override
    public List<RoomUserOptionVo> queryUserOptions() {
        return roomBookingMapper.selectUserOptions(departmentAccessService.currentDeptId(), LoginHelper.getUserId(), canManageAll());
    }

    @Override
    public List<RoomResourceAclVo> queryRoomAcls(Long roomId) {
        requireRoomManage();
        getRoom(roomId, true);
        return roomResourceAclMapper.selectVoByRoomId(roomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoomAcl(RoomResourceAclBo bo) {
        requireRoomManage();
        if (bo == null || bo.getRoomId() == null) {
            throw new ServiceException("房间主键不能为空");
        }
        getRoom(bo.getRoomId(), true);
        String subjectType = normalizeAclSubjectType(bo.getSubjectType());
        String permissionType = normalizeAclPermissionType(bo.getPermissionType());
        if (!SUBJECT_ALL.equals(subjectType) && bo.getSubjectId() == null) {
            throw new ServiceException("用户、科室或角色授权必须选择主体");
        }
        if (SUBJECT_ALL.equals(subjectType)) {
            bo.setSubjectId(null);
        }
        RoomResourceAcl entity = bo.getId() == null ? new RoomResourceAcl() : roomResourceAclMapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("房间授权不存在");
        }
        if (entity.getId() != null && !Objects.equals(entity.getRoomId(), bo.getRoomId())) {
            getRoom(entity.getRoomId(), true);
        }
        RoomResourceAcl duplicate = roomResourceAclMapper.selectOne(Wrappers.<RoomResourceAcl>lambdaQuery()
            .eq(RoomResourceAcl::getRoomId, bo.getRoomId())
            .eq(RoomResourceAcl::getSubjectType, subjectType)
            .isNull(Objects.equals(subjectType, SUBJECT_ALL), RoomResourceAcl::getSubjectId)
            .eq(!Objects.equals(subjectType, SUBJECT_ALL), RoomResourceAcl::getSubjectId, bo.getSubjectId())
            .eq(RoomResourceAcl::getPermissionType, permissionType)
            .ne(bo.getId() != null, RoomResourceAcl::getId, bo.getId()));
        if (duplicate != null) {
            throw new ServiceException("相同主体和授权动作已经存在");
        }
        entity.setRoomId(bo.getRoomId());
        entity.setSubjectType(subjectType);
        entity.setSubjectId(bo.getSubjectId());
        entity.setPermissionType(permissionType);
        boolean saved = bo.getId() == null ? roomResourceAclMapper.insert(entity) > 0 : roomResourceAclMapper.updateById(entity) > 0;
        if (saved) {
            notifyRoomResourceChanged("ACL_UPDATE");
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRoomAcls(Collection<Long> ids) {
        requireRoomManage();
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            RoomResourceAcl acl = roomResourceAclMapper.selectById(id);
            if (acl == null) {
                throw new ServiceException("房间授权不存在");
            }
            getRoom(acl.getRoomId(), true);
        }
        boolean deleted = roomResourceAclMapper.deleteByIds(ids) > 0;
        if (deleted) {
            notifyRoomResourceChanged("ACL_DELETE");
        }
        return deleted;
    }

    @Override
    public List<RoomBookingBlacklistVo> queryRoomBlacklists(Long roomId) {
        requireRoomManage();
        getRoom(roomId, true);
        return blacklistMapper.selectVoByRoomId(roomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoomBlacklist(RoomBookingBlacklistBo bo) {
        requireRoomManage();
        if (bo == null || bo.getRoomId() == null) {
            throw new ServiceException("房间主键不能为空");
        }
        getRoom(bo.getRoomId(), true);
        String subjectType = normalizeAclSubjectType(bo.getSubjectType());
        if (!SUBJECT_ALL.equals(subjectType) && bo.getSubjectId() == null) {
            throw new ServiceException("用户、科室或角色黑名单必须选择主体");
        }
        if (SUBJECT_ALL.equals(subjectType)) {
            bo.setSubjectId(null);
        }
        RoomBookingBlacklist entity = bo.getId() == null ? new RoomBookingBlacklist() : blacklistMapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("预约黑名单不存在");
        }
        if (entity.getId() != null && !Objects.equals(entity.getRoomId(), bo.getRoomId())) {
            getRoom(entity.getRoomId(), true);
        }
        RoomBookingBlacklist duplicate = blacklistMapper.selectOne(Wrappers.<RoomBookingBlacklist>lambdaQuery()
            .eq(RoomBookingBlacklist::getRoomId, bo.getRoomId())
            .eq(RoomBookingBlacklist::getSubjectType, subjectType)
            .isNull(Objects.equals(subjectType, SUBJECT_ALL), RoomBookingBlacklist::getSubjectId)
            .eq(!Objects.equals(subjectType, SUBJECT_ALL), RoomBookingBlacklist::getSubjectId, bo.getSubjectId())
            .ne(bo.getId() != null, RoomBookingBlacklist::getId, bo.getId()));
        if (duplicate != null) {
            throw new ServiceException("相同主体已经在该房间黑名单中");
        }
        entity.setRoomId(bo.getRoomId());
        entity.setSubjectType(subjectType);
        entity.setSubjectId(bo.getSubjectId());
        entity.setReason(StringUtils.isBlank(bo.getReason()) ? "房间预约黑名单" : StringUtils.trim(bo.getReason()));
        entity.setEnabled(bo.getEnabled() == null || bo.getEnabled());
        boolean saved = bo.getId() == null ? blacklistMapper.insert(entity) > 0 : blacklistMapper.updateById(entity) > 0;
        if (saved) {
            notifyRoomResourceChanged("BLACKLIST_UPDATE");
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRoomBlacklists(Collection<Long> ids) {
        requireRoomManage();
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            RoomBookingBlacklist entity = blacklistMapper.selectById(id);
            if (entity == null) {
                throw new ServiceException("预约黑名单不存在");
            }
            getRoom(entity.getRoomId(), true);
        }
        boolean deleted = blacklistMapper.deleteByIds(ids) > 0;
        if (deleted) {
            notifyRoomResourceChanged("BLACKLIST_DELETE");
        }
        return deleted;
    }

    @Override
    public List<RoomBookingBlockVo> queryRoomBlocks(Long roomId) {
        requireRoomManage();
        getRoom(roomId, true);
        return roomBookingBlockMapper.selectVoByRoomId(roomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoomBlock(RoomBookingBlockBo bo) {
        requireRoomManage();
        if (bo == null || bo.getRoomId() == null || bo.getStartAt() == null || bo.getEndAt() == null) {
            throw new ServiceException("房间、封锁开始和结束时间不能为空");
        }
        if (!bo.getEndAt().isAfter(bo.getStartAt())) {
            throw new ServiceException("封锁结束时间必须晚于开始时间");
        }
        getRoom(bo.getRoomId(), true);
        List<RoomBookingBlock> plannedBlocks = bo.getId() == null ? buildMaintenanceBlocks(bo) : List.of();
        if (bo.getId() != null) {
            ensureBlockWindowAvailable(bo.getRoomId(), bo.getStartAt(), bo.getEndAt(), bo.getId());
        } else {
            for (int index = 0; index < plannedBlocks.size(); index++) {
                RoomBookingBlock planned = plannedBlocks.get(index);
                ensureBlockWindowAvailable(bo.getRoomId(), planned.getStartAt(), planned.getEndAt(), null);
                for (int next = index + 1; next < plannedBlocks.size(); next++) {
                    RoomBookingBlock following = plannedBlocks.get(next);
                    if (planned.getStartAt().isBefore(following.getEndAt())
                        && planned.getEndAt().isAfter(following.getStartAt())) {
                        throw new ServiceException("维护计划自身存在重叠封锁时段：" + following.getStartAt());
                    }
                }
            }
        }
        RoomBookingBlock entity = bo.getId() == null ? new RoomBookingBlock() : roomBookingBlockMapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("封锁记录不存在");
        }
        if (entity.getId() != null && !Objects.equals(entity.getRoomId(), bo.getRoomId())) {
            getRoom(entity.getRoomId(), true);
        }
        entity.setRoomId(bo.getRoomId());
        entity.setStartAt(bo.getStartAt());
        entity.setEndAt(bo.getEndAt());
        entity.setReason(StringUtils.isBlank(bo.getReason()) ? "房间临时封锁" : StringUtils.trim(bo.getReason()));
        entity.setStatus("BLOCKED");
        entity.setRecurrenceType(normalizeRecurrenceType(bo.getRecurrenceType()));
        entity.setRecurrenceInterval(bo.getRecurrenceInterval() == null ? 1 : bo.getRecurrenceInterval());
        entity.setRecurrenceCount(bo.getRecurrenceCount());
        entity.setRecurrenceUntil(bo.getRecurrenceUntil());
        if (bo.getId() != null) {
            boolean updated = roomBookingBlockMapper.updateById(entity) > 0;
            if (updated) {
                notifyRoomResourceChanged("BLOCK_UPDATE");
            }
            return updated;
        }
        boolean saved = true;
        for (RoomBookingBlock planned : plannedBlocks) {
            saved = saved && roomBookingBlockMapper.insert(planned) > 0;
        }
        if (saved) {
            notifyRoomResourceChanged("BLOCK_CREATE");
        }
        return saved;
    }

    /** 封锁时段既不能重叠已有封锁，也不能覆盖有效预约实例。 */
    private void ensureBlockWindowAvailable(Long roomId,
                                             LocalDateTime startAt,
                                             LocalDateTime endAt,
                                             Long excludeBlockId) {
        List<RoomBookingBlock> blockConflicts = roomBookingBlockMapper.selectConflicts(roomId, startAt, endAt);
        if (blockConflicts.stream().anyMatch(item -> !Objects.equals(item.getId(), excludeBlockId))) {
            throw new ServiceException("该房间已有重叠的封锁时段：" + startAt);
        }
        if (!occurrenceMapper.selectConflicts(roomId, startAt, endAt, null).isEmpty()) {
            throw new ServiceException("维护封锁与已有预约冲突：" + startAt);
        }
    }

    /** 将维护计划展开成明确的封锁记录，预约冲突检查无需理解计划语法。 */
    private List<RoomBookingBlock> buildMaintenanceBlocks(RoomBookingBlockBo bo) {
        String type = normalizeRecurrenceType(bo.getRecurrenceType());
        int interval = bo.getRecurrenceInterval() == null ? 1 : bo.getRecurrenceInterval();
        if (interval < 1 || interval > 30) {
            throw new ServiceException("维护计划间隔必须在1至30之间");
        }
        int requestedCount = bo.getRecurrenceCount() == null ? 0 : bo.getRecurrenceCount();
        if (requestedCount < 0 || requestedCount > MAX_OCCURRENCES) {
            throw new ServiceException("维护计划次数必须在1至100之间");
        }
        if (!RECURRENCE_NONE.equals(type) && requestedCount == 0 && bo.getRecurrenceUntil() == null) {
            throw new ServiceException("维护计划请选择结束日期或填写重复次数");
        }
        long durationMinutes = java.time.Duration.between(bo.getStartAt(), bo.getEndAt()).toMinutes();
        List<RoomBookingBlock> result = new ArrayList<>();
        LocalDateTime start = bo.getStartAt();
        for (int index = 0; index < MAX_OCCURRENCES; index++) {
            if (requestedCount > 0 && index >= requestedCount) {
                break;
            }
            if (bo.getRecurrenceUntil() != null && start.toLocalDate().isAfter(bo.getRecurrenceUntil())) {
                break;
            }
            RoomBookingBlock item = new RoomBookingBlock();
            item.setRoomId(bo.getRoomId());
            item.setStartAt(start);
            item.setEndAt(start.plusMinutes(durationMinutes));
            item.setReason(StringUtils.isBlank(bo.getReason()) ? "房间维护计划" : StringUtils.trim(bo.getReason()));
            item.setStatus("BLOCKED");
            item.setRecurrenceType(type);
            item.setRecurrenceInterval(interval);
            item.setRecurrenceCount(bo.getRecurrenceCount());
            item.setRecurrenceUntil(bo.getRecurrenceUntil());
            result.add(item);
            if (RECURRENCE_NONE.equals(type)) {
                break;
            }
            start = switch (type) {
                case "DAILY" -> start.plusDays(interval);
                case "WEEKLY" -> start.plusWeeks(interval);
                case "MONTHLY" -> start.plusMonths(interval);
                default -> throw new ServiceException("不支持的维护计划类型：" + type);
            };
        }
        if (result.isEmpty()) {
            throw new ServiceException("维护计划至少需要生成一个封锁实例");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRoomBlocks(Collection<Long> ids) {
        requireRoomManage();
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            RoomBookingBlock block = roomBookingBlockMapper.selectById(id);
            if (block == null) {
                throw new ServiceException("封锁记录不存在");
            }
            getRoom(block.getRoomId(), true);
        }
        boolean deleted = roomBookingBlockMapper.deleteByIds(ids) > 0;
        if (deleted) {
            notifyRoomResourceChanged("BLOCK_DELETE");
        }
        return deleted;
    }

    @Override
    public List<RoomApprovalRuleVo> queryApprovalRules(Long roomId) {
        requireRoomManage();
        getRoom(roomId, true);
        return approvalRuleMapper.selectVoByRoomId(roomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveApprovalRule(RoomApprovalRuleBo bo) {
        requireRoomManage();
        if (bo == null || bo.getRoomId() == null) {
            throw new ServiceException("房间主键不能为空");
        }
        getRoom(bo.getRoomId(), true);
        String approverType = normalizeApprovalApproverType(bo.getApproverType());
        if (!SUBJECT_ALL.equals(approverType) && bo.getApproverId() == null) {
            throw new ServiceException("用户、科室或角色审批必须选择审批主体");
        }
        if (SUBJECT_ALL.equals(approverType)) {
            bo.setApproverId(null);
        }
        int stepNo = bo.getStepNo() == null ? 1 : bo.getStepNo();
        if (stepNo < 1 || stepNo > 20) {
            throw new ServiceException("审批级别必须在1到20之间");
        }
        RoomApprovalRule entity = bo.getId() == null ? new RoomApprovalRule() : approvalRuleMapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("审批规则不存在");
        }
        if (entity.getId() != null && !Objects.equals(entity.getRoomId(), bo.getRoomId())) {
            getRoom(entity.getRoomId(), true);
        }
        RoomApprovalRule duplicate = approvalRuleMapper.selectOne(Wrappers.<RoomApprovalRule>lambdaQuery()
            .eq(RoomApprovalRule::getRoomId, bo.getRoomId())
            .eq(RoomApprovalRule::getStepNo, stepNo)
            .ne(bo.getId() != null, RoomApprovalRule::getId, bo.getId()));
        if (duplicate != null) {
            throw new ServiceException("同一房间的审批级别不能重复");
        }
        entity.setRoomId(bo.getRoomId());
        entity.setStepNo(stepNo);
        entity.setApproverType(approverType);
        entity.setApproverId(bo.getApproverId());
        entity.setTimeoutMinutes(bo.getTimeoutMinutes() == null ? DEFAULT_APPROVAL_TIMEOUT_MINUTES : bo.getTimeoutMinutes());
        entity.setEnabled(bo.getEnabled() == null || bo.getEnabled());
        entity.setRemark(StringUtils.trim(bo.getRemark()));
        boolean saved = bo.getId() == null ? approvalRuleMapper.insert(entity) > 0 : approvalRuleMapper.updateById(entity) > 0;
        if (saved) {
            notifyRoomResourceChanged("APPROVAL_RULE_UPDATE");
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteApprovalRules(Collection<Long> ids) {
        requireRoomManage();
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            RoomApprovalRule rule = approvalRuleMapper.selectById(id);
            if (rule == null) {
                throw new ServiceException("审批规则不存在");
            }
            getRoom(rule.getRoomId(), true);
        }
        boolean deleted = approvalRuleMapper.deleteByIds(ids) > 0;
        if (deleted) {
            notifyRoomResourceChanged("APPROVAL_RULE_DELETE");
        }
        return deleted;
    }

    @Override
    public List<RoomQuotaPolicyVo> queryQuotaPolicies(Long roomId) {
        requireRoomManage();
        getRoom(roomId, true);
        return quotaPolicyMapper.selectVoByRoomId(roomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveQuotaPolicy(RoomQuotaPolicyBo bo) {
        requireRoomManage();
        if (bo == null || bo.getRoomId() == null) {
            throw new ServiceException("房间主键不能为空");
        }
        getRoom(bo.getRoomId(), true);
        String subjectType = normalizeQuotaSubjectType(bo.getSubjectType());
        String periodType = normalizeQuotaPeriodType(bo.getPeriodType());
        String overQuotaAction = normalizeQuotaAction(bo.getOverQuotaAction());
        if (!SUBJECT_ALL.equals(subjectType) && bo.getSubjectId() == null) {
            throw new ServiceException("用户、科室或角色配额必须选择主体");
        }
        if (SUBJECT_ALL.equals(subjectType)) {
            bo.setSubjectId(null);
        }
        if ((bo.getQuotaMinutes() == null || bo.getQuotaMinutes() <= 0)
            && (bo.getQuotaCount() == null || bo.getQuotaCount() <= 0)) {
            throw new ServiceException("分钟配额和次数配额至少配置一项");
        }
        RoomQuotaPolicy entity = bo.getId() == null ? new RoomQuotaPolicy() : quotaPolicyMapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("配额策略不存在");
        }
        if (entity.getId() != null && !Objects.equals(entity.getRoomId(), bo.getRoomId())) {
            getRoom(entity.getRoomId(), true);
        }
        RoomQuotaPolicy duplicate = quotaPolicyMapper.selectOne(Wrappers.<RoomQuotaPolicy>lambdaQuery()
            .eq(RoomQuotaPolicy::getRoomId, bo.getRoomId())
            .eq(RoomQuotaPolicy::getSubjectType, subjectType)
            .isNull(Objects.equals(subjectType, SUBJECT_ALL), RoomQuotaPolicy::getSubjectId)
            .eq(!Objects.equals(subjectType, SUBJECT_ALL), RoomQuotaPolicy::getSubjectId, bo.getSubjectId())
            .eq(RoomQuotaPolicy::getPeriodType, periodType)
            .ne(bo.getId() != null, RoomQuotaPolicy::getId, bo.getId()));
        if (duplicate != null) {
            throw new ServiceException("相同主体和配额周期的策略已经存在");
        }
        entity.setRoomId(bo.getRoomId());
        entity.setSubjectType(subjectType);
        entity.setSubjectId(bo.getSubjectId());
        entity.setPeriodType(periodType);
        entity.setQuotaMinutes(bo.getQuotaMinutes() == null ? 0 : bo.getQuotaMinutes());
        entity.setQuotaCount(bo.getQuotaCount() == null ? 0 : bo.getQuotaCount());
        entity.setOverQuotaAction(overQuotaAction);
        entity.setUnitPrice(bo.getUnitPrice() == null ? BigDecimal.ZERO : bo.getUnitPrice().max(BigDecimal.ZERO));
        entity.setRefundRate(bo.getRefundRate() == null ? BigDecimal.valueOf(100) : bo.getRefundRate().max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)));
        entity.setEnabled(bo.getEnabled() == null || bo.getEnabled());
        entity.setRemark(StringUtils.trim(bo.getRemark()));
        boolean saved = bo.getId() == null ? quotaPolicyMapper.insert(entity) > 0 : quotaPolicyMapper.updateById(entity) > 0;
        if (saved) {
            notifyRoomResourceChanged("QUOTA_UPDATE");
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteQuotaPolicies(Collection<Long> ids) {
        requireRoomManage();
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            RoomQuotaPolicy policy = quotaPolicyMapper.selectById(id);
            if (policy == null) {
                throw new ServiceException("配额策略不存在");
            }
            getRoom(policy.getRoomId(), true);
        }
        boolean deleted = quotaPolicyMapper.deleteByIds(ids) > 0;
        if (deleted) {
            notifyRoomResourceChanged("QUOTA_DELETE");
        }
        return deleted;
    }

    @Override
    public PageResult<RoomAmenityVo> queryAmenityPage(PageQuery pageQuery) {
        requireRoomManage();
        Page<RoomAmenityVo> result = amenityMapper.selectPageList(pageQuery.build(),
            departmentAccessService.currentDeptId(), canManageAll());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<RoomAmenityVo> queryAmenityOptions() {
        return amenityMapper.selectOptions(departmentAccessService.currentDeptId(), canManageAll());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertAmenity(RoomAmenityBo bo) {
        Long deptId = requireDepartment("新增房间设施");
        requireRoomManage();
        RoomAmenity entity = new RoomAmenity();
        entity.setDeptId(deptId);
        copyAmenityBo(bo, entity);
        checkAmenityDuplicate(entity);
        boolean inserted = amenityMapper.insert(entity) > 0;
        if (inserted) {
            notifyRoomResourceChanged("AMENITY_CREATE");
        }
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAmenity(RoomAmenityBo bo) {
        requireRoomManage();
        RoomAmenity entity = bo == null ? null : amenityMapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("房间设施不存在");
        }
        if (!canManageAll() && !Objects.equals(entity.getDeptId(), departmentAccessService.currentDeptId())) {
            throw new ServiceException("您没有管理该设施的权限");
        }
        copyAmenityBo(bo, entity);
        checkAmenityDuplicate(entity);
        boolean updated = amenityMapper.updateById(entity) > 0;
        if (updated) {
            notifyRoomResourceChanged("AMENITY_UPDATE");
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAmenities(Collection<Long> ids) {
        requireRoomManage();
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            RoomAmenity amenity = amenityMapper.selectById(id);
            if (amenity == null) {
                throw new ServiceException("房间设施不存在");
            }
            if (!canManageAll() && !Objects.equals(amenity.getDeptId(), departmentAccessService.currentDeptId())) {
                throw new ServiceException("您没有管理该设施的权限");
            }
            long relationCount = amenityRelationMapper.selectCount(Wrappers.<RoomAmenityRelation>lambdaQuery()
                .eq(RoomAmenityRelation::getAmenityId, id));
            if (relationCount > 0) {
                throw new ServiceException("设施已绑定房间，不能删除，请先解除房间关联");
            }
        }
        boolean deleted = amenityMapper.deleteByIds(ids) > 0;
        if (deleted) {
            notifyRoomResourceChanged("AMENITY_DELETE");
        }
        return deleted;
    }

    @Override
    public PageResult<RoomFloorPlanVo> queryFloorPlanPage(RoomFloorPlanQueryBo bo, PageQuery pageQuery) {
        Page<RoomFloorPlanVo> page = pageQuery.build();
        Page<RoomFloorPlanVo> result = roomFloorPlanMapper.selectPageList(
            page,
            bo == null ? new RoomFloorPlanQueryBo() : bo,
            departmentAccessService.currentDeptId(),
            LoginHelper.getUserId(),
            canManageAll(),
            StpUtil.hasPermission("department:room:manage")
        );
        sanitizeFloorPlanMapData(result.getRecords());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<RoomFloorPlanVo> queryFloorPlanOptions() {
        List<RoomFloorPlanVo> plans = roomFloorPlanMapper.selectOptions(
            departmentAccessService.currentDeptId(), LoginHelper.getUserId(), canManageAll(),
            StpUtil.hasPermission("department:room:manage") || canManageAll()
        );
        sanitizeFloorPlanMapData(plans);
        return plans;
    }

    @Override
    public RoomFloorPlanVo queryFloorPlanById(Long id) {
        RoomFloorPlan plan = getFloorPlan(id, false);
        RoomFloorPlanVo vo = toFloorPlanVo(plan);
        sanitizeFloorPlanMapData(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertFloorPlan(RoomFloorPlanBo bo) {
        Long deptId = requireDepartment("新增平面图");
        requireRoomManage();
        RoomFloorPlan entity = new RoomFloorPlan();
        entity.setDeptId(deptId);
        copyFloorPlanBo(bo, entity);
        validatePublishedFloorPlan(entity);
        checkFloorPlanDuplicate(entity);
        boolean inserted = roomFloorPlanMapper.insert(entity) > 0;
        if (inserted) {
            recordFloorPlanVersion(entity, "创建平面图");
            notifyRoomResourceChanged("FLOOR_PLAN_CREATE");
        }
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFloorPlan(RoomFloorPlanBo bo) {
        requireRoomManage();
        RoomFloorPlan entity = getFloorPlan(bo == null ? null : bo.getId(), true);
        copyFloorPlanBo(bo, entity);
        validatePublishedFloorPlan(entity);
        checkFloorPlanDuplicate(entity);
        boolean updated = roomFloorPlanMapper.updateById(entity) > 0;
        if (updated) {
            recordFloorPlanVersion(entity, "保存平面图");
            notifyRoomResourceChanged("FLOOR_PLAN_UPDATE");
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFloorPlans(Collection<Long> ids) {
        requireRoomManage();
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            RoomFloorPlan plan = getFloorPlan(id, true);
            long roomCount = roomResourceMapper.selectCount(Wrappers.<RoomResource>lambdaQuery()
                .eq(RoomResource::getFloorPlanId, plan.getId()));
            if (roomCount > 0) {
                throw new ServiceException("平面图已绑定房间，不能删除，请先解除房间绑定");
            }
        }
        boolean deleted = roomFloorPlanMapper.deleteByIds(ids) > 0;
        if (deleted) {
            notifyRoomResourceChanged("FLOOR_PLAN_DELETE");
        }
        return deleted;
    }

    @Override
    public List<RoomFloorPlanVersionVo> queryFloorPlanVersions(Long floorPlanId) {
        requireRoomManage();
        getFloorPlan(floorPlanId, true);
        return roomFloorPlanVersionMapper.selectByFloorPlanId(floorPlanId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishFloorPlan(Long floorPlanId) {
        return changeFloorPlanPublication(floorPlanId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unpublishFloorPlan(Long floorPlanId) {
        return changeFloorPlanPublication(floorPlanId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean restoreFloorPlanVersion(Long versionId) {
        requireRoomManage();
        RoomFloorPlanVersion snapshot = roomFloorPlanVersionMapper.selectSnapshotById(versionId);
        if (snapshot == null) {
            throw new ServiceException("平面图历史版本不存在");
        }
        RoomFloorPlan plan = getFloorPlan(snapshot.getFloorPlanId(), true);
        try {
            RoomFloorPlanMapDataValidator.validate(snapshot.getMapData());
        } catch (IllegalArgumentException exception) {
            throw new ServiceException("历史版本无法恢复：" + exception.getMessage());
        }
        plan.setPlanName(snapshot.getPlanName());
        plan.setBuildingName(snapshot.getBuildingName());
        plan.setFloorName(snapshot.getFloorName());
        plan.setFloorNo(snapshot.getFloorNo());
        plan.setMapImage(snapshot.getMapImage());
        plan.setMapData(snapshot.getMapData());
        plan.setRemark(snapshot.getRemark());
        plan.setStatus("DRAFT");
        if (roomFloorPlanMapper.updateById(plan) <= 0) {
            throw new ServiceException("平面图已被其他人修改，请刷新后重试");
        }
        recordFloorPlanVersion(plan, "从第" + snapshot.getVersionNo() + "版恢复为草稿");
        notifyRoomResourceChanged("FLOOR_PLAN_RESTORE");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean splitRoomGroup(Long roomId) {
        requireRoomManage();
        RoomResource room = getRoom(roomId, true);
        if (StringUtils.isBlank(room.getMergeGroup())) {
            return false;
        }
        room.setMergeGroup(null);
        boolean updated = roomResourceMapper.updateById(room) > 0;
        if (updated) {
            notifyRoomResourceChanged("ROOM_SPLIT");
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean mergeRoomGroup(RoomResourceMergeBo bo) {
        requireRoomManage();
        if (bo == null || bo.getRoomIds() == null || bo.getRoomIds().size() < 2 || StringUtils.isBlank(bo.getMergeGroup())) {
            throw new ServiceException("组合至少需要两个房间和一个组合编码");
        }
        List<Long> roomIds = normalizeRoomIds(bo.getRoomIds());
        if (roomIds.size() < 2) {
            throw new ServiceException("组合至少需要两个不同房间");
        }
        List<RoomResource> rooms = roomResourceMapper.selectBatchIds(roomIds);
        if (rooms.size() != roomIds.size()) {
            throw new ServiceException("选择的房间不存在");
        }
        for (RoomResource room : rooms) {
            getRoom(room.getId(), true);
            room.setMergeGroup(StringUtils.trim(bo.getMergeGroup()));
            roomResourceMapper.updateById(room);
        }
        notifyRoomResourceChanged("ROOM_MERGE");
        return true;
    }

    @Override
    public RoomAnalyticsVo queryRoomAnalytics(RoomAnalyticsQueryBo bo) {
        requireRoomManage();
        LocalDateTime endAt = bo == null || bo.getEndAt() == null ? LocalDateTime.now() : bo.getEndAt();
        LocalDateTime beginAt = bo == null || bo.getBeginAt() == null ? endAt.minusDays(30) : bo.getBeginAt();
        if (!endAt.isAfter(beginAt)) {
            throw new ServiceException("统计结束时间必须晚于开始时间");
        }
        boolean manageAll = canManageAll();
        Long deptId = departmentAccessService.currentDeptId();
        RoomAnalyticsSummaryVo summary = occurrenceMapper.selectAnalyticsSummary(beginAt, endAt, deptId, manageAll);
        if (summary == null) {
            summary = new RoomAnalyticsSummaryVo();
        }
        List<RoomAnalyticsRoomVo> rooms = occurrenceMapper.selectAnalyticsRooms(beginAt, endAt, deptId, manageAll);
        long periodMinutes = Math.max(1, java.time.Duration.between(beginAt, endAt).toMinutes());
        long roomCount = roomResourceMapper.selectCount(Wrappers.<RoomResource>lambdaQuery()
            .eq(!manageAll, RoomResource::getDeptId, deptId)
            .eq(RoomResource::getStatus, ROOM_ENABLED));
        BigDecimal denominator = BigDecimal.valueOf(periodMinutes).multiply(BigDecimal.valueOf(Math.max(1, roomCount)));
        summary.setUtilizationRate(rate(summary.getTotalMinutes(), denominator));
        summary.setTotalDurationMinutes(periodMinutes);
        summary.setAvailableMinutes(denominator.longValue());
        summary.setVacancyMinutes(Math.max(0, denominator.longValue() - (summary.getTotalMinutes() == null ? 0 : summary.getTotalMinutes())));
        for (RoomAnalyticsRoomVo room : rooms) {
            room.setUtilizationRate(rate(room.getBookedMinutes(), BigDecimal.valueOf(periodMinutes)));
        }
        List<RoomAnalyticsDimensionVo> departments = occurrenceMapper.selectAnalyticsDepartments(beginAt, endAt, deptId, manageAll);
        List<RoomAnalyticsDimensionVo> users = occurrenceMapper.selectAnalyticsUsers(beginAt, endAt, deptId, manageAll);
        List<RoomAnalyticsHotSlotVo> hotSlots = occurrenceMapper.selectAnalyticsHotSlots(beginAt, endAt, deptId, manageAll);
        departments.forEach(item -> item.setUtilizationRate(rate(item.getBookedMinutes(), BigDecimal.valueOf(periodMinutes))));
        users.forEach(item -> item.setUtilizationRate(rate(item.getBookedMinutes(), BigDecimal.valueOf(periodMinutes))));
        RoomAnalyticsVo result = new RoomAnalyticsVo();
        result.setBeginAt(beginAt);
        result.setEndAt(endAt);
        result.setSummary(summary);
        result.setRooms(rooms);
        result.setDepartments(departments);
        result.setUsers(users);
        result.setHotSlots(hotSlots);
        RoomAnalyticsApprovalVo approvalStats = occurrenceMapper.selectAnalyticsApproval(beginAt, endAt, deptId, manageAll);
        result.setApprovalStats(approvalStats == null ? new RoomAnalyticsApprovalVo() : approvalStats);
        return result;
    }

    @Override
    public PageResult<RoomCalendarExceptionVo> queryCalendarExceptionPage(RoomCalendarExceptionQueryBo bo, PageQuery pageQuery) {
        RoomCalendarExceptionQueryBo query = bo == null ? new RoomCalendarExceptionQueryBo() : bo;
        if (query.getBeginDate() == null) {
            query.setBeginDate(LocalDate.now().minusMonths(1));
        }
        if (query.getEndDate() == null) {
            query.setEndDate(LocalDate.now().plusMonths(18));
        }
        if (query.getEndDate().isBefore(query.getBeginDate())) {
            throw new ServiceException("例外结束日期不能早于开始日期");
        }
        Page<RoomCalendarExceptionVo> page = pageQuery.build();
        Page<RoomCalendarExceptionVo> result = calendarExceptionMapper.selectPageList(
            page, query, departmentAccessService.currentDeptId(), canManageAll()
        );
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertCalendarException(RoomCalendarExceptionBo bo) {
        requireRoomManage();
        Long deptId = requireDepartment("新增日历例外");
        RoomCalendarException entity = new RoomCalendarException();
        entity.setDeptId(deptId);
        copyCalendarExceptionBo(bo, entity);
        checkCalendarExceptionDuplicate(entity);
        boolean inserted = calendarExceptionMapper.insert(entity) > 0;
        if (inserted) {
            notifyRoomResourceChanged("CALENDAR_EXCEPTION_CREATE");
        }
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCalendarException(RoomCalendarExceptionBo bo) {
        requireRoomManage();
        RoomCalendarException entity = calendarExceptionMapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("日历例外不存在");
        }
        if (!canManageAll() && !Objects.equals(entity.getDeptId(), departmentAccessService.currentDeptId())) {
            throw new ServiceException("您没有管理该日历例外的权限");
        }
        copyCalendarExceptionBo(bo, entity);
        checkCalendarExceptionDuplicate(entity);
        boolean updated = calendarExceptionMapper.updateById(entity) > 0;
        if (updated) {
            notifyRoomResourceChanged("CALENDAR_EXCEPTION_UPDATE");
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCalendarExceptions(Collection<Long> ids) {
        requireRoomManage();
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            RoomCalendarException entity = calendarExceptionMapper.selectById(id);
            if (entity == null) {
                throw new ServiceException("日历例外不存在");
            }
            if (!canManageAll() && !Objects.equals(entity.getDeptId(), departmentAccessService.currentDeptId())) {
                throw new ServiceException("您没有管理该日历例外的权限");
            }
        }
        boolean deleted = calendarExceptionMapper.deleteByIds(ids) > 0;
        if (deleted) {
            notifyRoomResourceChanged("CALENDAR_EXCEPTION_DELETE");
        }
        return deleted;
    }

    @Override
    public List<RoomBookingRecurrenceExceptionVo> queryRecurrenceExceptions(Long bookingId) {
        RoomBooking entity = getBooking(bookingId);
        ensureBookingReadable(entity, occurrenceMapper.selectByBookingId(bookingId));
        if ("PRIVATE".equalsIgnoreCase(entity.getVisibility())
            && !Objects.equals(entity.getOrganizerId(), LoginHelper.getUserId())
            && !canManageAll()) {
            return List.of();
        }
        return recurrenceExceptionMapper.selectVoByBookingId(bookingId);
    }

    @Override
    public List<RoomQuotaUsageRecordVo> queryQuotaUsage(Long roomId) {
        requireRoomManage();
        getRoom(roomId, true);
        return quotaUsageMapper.selectVoByRoomId(roomId);
    }

    @Override
    public List<RoomBookingVo> queryCalendar(RoomBookingQueryBo bo) {
        RoomBookingQueryBo query = normalizeQuery(bo);
        List<RoomBookingVo> result = roomBookingMapper.selectCalendar(
            query,
            departmentAccessService.currentDeptId(),
            LoginHelper.getUserId(),
            canManageAll(),
            StpUtil.hasPermission("department:room:approve")
        );
        return decorateBookingList(result);
    }

    @Override
    public PageResult<RoomBookingVo> queryBookingPage(RoomBookingQueryBo bo, PageQuery pageQuery) {
        RoomBookingQueryBo query = normalizeQuery(bo);
        Page<RoomBookingVo> page = pageQuery.build();
        Page<RoomBookingVo> result = roomBookingMapper.selectPageList(
            page,
            query,
            departmentAccessService.currentDeptId(),
            LoginHelper.getUserId(),
            canManageAll(),
            StpUtil.hasPermission("department:room:approve")
        );
        return PageResult.build(decorateBookingList(result.getRecords()), result.getTotal());
    }

    @Override
    public long queryPendingBookingCount() {
        return roomBookingMapper.selectPendingCount(
            departmentAccessService.currentDeptId(),
            LoginHelper.getUserId(),
            canManageAll(),
            StpUtil.hasPermission("department:room:approve")
        );
    }

    @Override
    public RoomBookingDetailVo queryBookingById(Long id) {
        return queryBookingById(id, null);
    }

    @Override
    public RoomBookingDetailVo queryBookingById(Long id, Integer occurrenceNo) {
        RoomBooking entity = getBooking(id);
        List<RoomBookingOccurrence> allOccurrences = occurrenceMapper.selectByBookingId(id);
        List<RoomBookingOccurrence> occurrences = occurrenceNo == null
            ? allOccurrences
            : allOccurrences.stream().filter(item -> Objects.equals(item.getOccurrenceNo(), occurrenceNo)).toList();
        if (occurrences.isEmpty()) {
            throw new ServiceException("预约实例不存在");
        }
        ensureBookingReadable(entity, allOccurrences);
        Long currentUserId = LoginHelper.getUserId();
        boolean mine = Objects.equals(entity.getOrganizerId(), currentUserId);
        boolean attendee = isAttendee(entity.getId(), currentUserId);
        boolean privateDetail = RoomBookingPrivacyEvaluator.shouldMaskPrivate(
            entity.getVisibility(), mine, attendee, canManageAll()
        );
        RoomBookingDetailVo vo = new RoomBookingDetailVo();
        vo.setId(entity.getId());
        vo.setTitle(privateDetail ? "已被预约" : entity.getTitle());
        vo.setDescription(privateDetail ? null : entity.getDescription());
        vo.setVisibility(entity.getVisibility());
        RoomBookingOccurrence first = occurrences.get(0);
        vo.setStartAt(occurrenceNo == null ? entity.getBaseStartAt() : first.getStartAt());
        vo.setEndAt(occurrenceNo == null ? entity.getBaseEndAt() : first.getEndAt());
        vo.setRecurrenceType(entity.getRecurrenceType());
        vo.setRecurrenceInterval(entity.getRecurrenceInterval());
        vo.setRecurrenceUntil(entity.getRecurrenceUntil());
        vo.setRecurrenceCount(entity.getRecurrenceCount());
        vo.setStatus(occurrenceNo == null ? entity.getStatus() : first.getStatus());
        vo.setApprovalStep(first.getApprovalStep());
        vo.setApprovalDueAt(first.getApprovalDueAt());
        vo.setCheckInAt(first.getCheckInAt());
        vo.setCheckOutAt(first.getCheckOutAt());
        vo.setApprovalRequired(entity.getApprovalRequired());
        vo.setOrganizerId(privateDetail ? null : entity.getOrganizerId());
        vo.setOccurrenceNo(occurrenceNo);
        vo.setOccurrenceId(first.getId());
        boolean ownerOrParticipant = mine || attendee || canManageAll();
        List<Long> visibleRoomIds = occurrences.stream()
            .filter(item -> ownerOrParticipant || canReadBookingOccurrenceRoom(item))
            .map(RoomBookingOccurrence::getRoomId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        vo.setRoomIds(visibleRoomIds);
        vo.setAttendeeIds(privateDetail ? List.of() : attendeeMapper.selectByBookingId(id).stream().map(RoomBookingAttendee::getUserId).toList());
        vo.setApprovalRecords(privateDetail ? List.of() : approvalMapper.selectVoByBookingId(id));
        vo.setRecurrenceExceptions(privateDetail || RECURRENCE_NONE.equalsIgnoreCase(entity.getRecurrenceType())
            ? List.of() : recurrenceExceptionMapper.selectVoByBookingId(id));
        vo.setCanCheckIn(canCheckIn(first, mine || canManageAll() || attendee));
        vo.setCanCheckOut(canCheckOut(first, mine || canManageAll()));
        vo.setCanRelease(canRelease(first, mine || canManageAll()));
        vo.setCanExtend(canExtend(first, mine || canManageAll()));
        return vo;
    }

    @Override
    public String exportBookingIcal(Long id, Integer occurrenceNo) {
        RoomBookingDetailVo detail = queryBookingById(id, occurrenceNo);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String uid = detail.getId() + "-" + (detail.getOccurrenceNo() == null ? 0 : detail.getOccurrenceNo()) + "@department-room";
        return "BEGIN:VCALENDAR\r\n"
            + "VERSION:2.0\r\n"
            + "PRODID:-//Department Management//Room Booking//CN\r\n"
            + "CALSCALE:GREGORIAN\r\n"
            + "X-WR-TIMEZONE:Asia/Shanghai\r\n"
            + "BEGIN:VEVENT\r\n"
            + "UID:" + icalEscape(uid) + "\r\n"
            + "DTSTAMP:" + LocalDateTime.now().format(formatter) + "\r\n"
            + "DTSTART;TZID=Asia/Shanghai:" + detail.getStartAt().format(formatter) + "\r\n"
            + "DTEND;TZID=Asia/Shanghai:" + detail.getEndAt().format(formatter) + "\r\n"
            + "SUMMARY:" + icalEscape(detail.getTitle()) + "\r\n"
            + "DESCRIPTION:" + icalEscape(detail.getDescription()) + "\r\n"
            + "STATUS:" + (STATUS_CANCELLED.equals(detail.getStatus()) ? "CANCELLED" : "CONFIRMED") + "\r\n"
            + "END:VEVENT\r\nEND:VCALENDAR\r\n";
    }

    private String icalEscape(String value) {
        return StringUtils.isBlank(value) ? "" : value.replace("\\", "\\\\").replace(";", "\\;")
            .replace(",", "\\,").replace("\r", "").replace("\n", "\\n");
    }

    @Override
    public RoomConflictResultVo checkConflict(RoomBookingBo bo) {
        validateBookingBo(bo);
        List<RoomBookingOccurrence> occurrences = isOccurrenceOperation(bo)
            ? List.of(buildOccurrence(bo.getOccurrenceNo(), bo.getStartAt(), bo.getEndAt()))
            : buildOccurrences(bo);
        return checkConflictInternal(bo.getRoomIds(), occurrences, bo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveBooking(RoomBookingBo bo) {
        validateBookingBo(bo);
        if (bo.getId() != null && isOccurrenceOperation(bo)) {
            return saveOccurrenceBooking(bo);
        }
        Long userId = LoginHelper.getUserId();
        Long deptId = requireDepartment("创建预约");
        if (bo.getId() == null && StringUtils.isNotBlank(bo.getRequestKey())) {
            RoomBooking existing = roomBookingMapper.selectOne(Wrappers.<RoomBooking>lambdaQuery()
                .eq(RoomBooking::getOrganizerId, userId)
                .eq(RoomBooking::getRequestKey, StringUtils.trim(bo.getRequestKey()))
                .last("limit 1"));
            if (existing != null) {
                return true;
            }
        }
        List<Long> roomIds = normalizeRoomIds(bo.getRoomIds());
        List<RoomResource> rooms = getAccessibleRooms(roomIds);
        List<RoomBookingOccurrence> occurrences = buildOccurrences(bo);
        RoomConflictResultVo conflicts = checkConflictInternal(roomIds, occurrences, bo.getId());
        if (!conflicts.isAvailable()) {
            throw new ServiceException(formatConflicts(conflicts));
        }

        RoomBooking entity;
        if (bo.getId() == null) {
            entity = new RoomBooking();
            entity.setDeptId(deptId);
            entity.setOrganizerId(userId);
            entity.setRequestKey(StringUtils.trim(bo.getRequestKey()));
        } else {
            entity = getBooking(bo.getId());
            ensureBookingEditable(entity);
        }
        String previousStatus = entity.getStatus();
        copyBookingBo(bo, entity);
        entity.setDeptId(deptId);
        entity.setOrganizerId(entity.getOrganizerId() == null ? userId : entity.getOrganizerId());
        boolean quotaApproval = validateBookingQuota(roomIds, occurrences, bo.getId());
        boolean approvalRequired = quotaApproval || rooms.stream().anyMatch(this::requiresApproval);
        entity.setApprovalRequired(approvalRequired);
        entity.setStatus(approvalRequired ? STATUS_PENDING : STATUS_CONFIRMED);
        entity.setRejectedReason(null);
        entity.setApprovedBy(null);
        entity.setApprovedAt(null);
        List<RoomBookingOccurrence> oldOccurrences = entity.getId() == null
            ? List.of() : occurrenceMapper.selectByBookingId(entity.getId());
        if (entity.getId() != null && oldOccurrences.stream().anyMatch(item ->
            !STATUS_PENDING.equals(item.getStatus()) && !STATUS_CONFIRMED.equals(item.getStatus()))) {
            throw new ServiceException("预约系列已有开始、释放或完成的实例，请改为编辑未开始的单次实例");
        }
        Set<Integer> skippedOccurrenceNos = entity.getId() == null ? Set.of()
            : recurrenceExceptionMapper.selectList(Wrappers.<RoomBookingRecurrenceException>lambdaQuery()
                .eq(RoomBookingRecurrenceException::getBookingId, entity.getId())
                .eq(RoomBookingRecurrenceException::getStatus, "SKIPPED"))
                .stream().map(RoomBookingRecurrenceException::getOccurrenceNo).collect(Collectors.toSet());
        if (!oldOccurrences.isEmpty()) {
            recordQuotaUsage(oldOccurrences, entity.getOrganizerId(), entity.getDeptId(), "RELEASE", "预约系列重新生成，释放旧配额占用");
        }
        if (entity.getId() == null) {
            roomBookingMapper.insert(entity);
        } else {
            occurrenceMapper.delete(Wrappers.<RoomBookingOccurrence>lambdaQuery().eq(RoomBookingOccurrence::getBookingId, entity.getId()));
            attendeeMapper.delete(Wrappers.<RoomBookingAttendee>lambdaQuery().eq(RoomBookingAttendee::getBookingId, entity.getId()));
            roomBookingMapper.updateById(entity);
        }
        List<RoomBookingOccurrence> insertedOccurrences = new ArrayList<>();
        for (RoomBookingOccurrence occurrence : occurrences) {
            if (skippedOccurrenceNos.contains(occurrence.getOccurrenceNo())) {
                continue;
            }
            for (Long roomId : roomIds) {
                RoomBookingOccurrence item = new RoomBookingOccurrence();
                item.setBookingId(entity.getId());
                item.setRoomId(roomId);
                item.setOccurrenceNo(occurrence.getOccurrenceNo());
                item.setStartAt(occurrence.getStartAt());
                item.setEndAt(occurrence.getEndAt());
                item.setStatus(entity.getStatus());
                if (approvalRequired) {
                    initializeApproval(item, roomId);
                }
                occurrenceMapper.insert(item);
                insertedOccurrences.add(item);
            }
        }
        recordQuotaUsage(insertedOccurrences, entity.getOrganizerId(), entity.getDeptId(), "CONSUME", "创建或更新房间预约");
        Set<Long> attendeeIds = bo.getAttendeeIds() == null
            ? Set.of()
            : bo.getAttendeeIds().stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        for (Long attendeeId : attendeeIds) {
            RoomBookingAttendee attendee = new RoomBookingAttendee();
            attendee.setBookingId(entity.getId());
            attendee.setUserId(attendeeId);
            attendee.setAttendeeStatus("INVITED");
            attendeeMapper.insert(attendee);
        }
        recordBookingChange(entity.getId(), null, bo.getId() == null ? "SUBMIT" : "UPDATE",
            previousStatus, entity.getStatus(), null);
        notifyBookingUsers(entity, entity.getApprovalRequired() ? "房间预约已提交，等待审批。" : "房间预约已创建并确认。", "SUBMIT");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelBookings(Collection<Long> ids) {
        return cancelBookings(ids, OPERATION_SERIES, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelBookings(Collection<Long> ids, String operationScope, Integer occurrenceNo, String reason) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            RoomBooking entity = getBooking(id);
            ensureBookingEditable(entity);
            List<RoomBookingOccurrence> affectedOccurrences = occurrenceMapper.selectByBookingId(id).stream()
                .filter(item -> occurrenceNo == null || Objects.equals(item.getOccurrenceNo(), occurrenceNo))
                .filter(item -> STATUS_PENDING.equals(item.getStatus()) || STATUS_CONFIRMED.equals(item.getStatus()))
                .toList();
            String previousStatus = entity.getStatus();
            if (OPERATION_OCCURRENCE.equalsIgnoreCase(operationScope)) {
                if (occurrenceNo == null || occurrenceNo < 1) {
                    throw new ServiceException("请选择要取消的预约实例");
                }
                RoomBookingOccurrence cancelledOccurrence = occurrenceMapper.selectByBookingId(id).stream()
                    .filter(item -> Objects.equals(item.getOccurrenceNo(), occurrenceNo))
                    .findFirst().orElse(null);
                int updated = occurrenceMapper.update(null, Wrappers.<RoomBookingOccurrence>lambdaUpdate()
                    .eq(RoomBookingOccurrence::getBookingId, id)
                    .eq(RoomBookingOccurrence::getOccurrenceNo, occurrenceNo)
                    .in(RoomBookingOccurrence::getStatus, STATUS_PENDING, STATUS_CONFIRMED)
                    .set(RoomBookingOccurrence::getStatus, STATUS_CANCELLED)
                    .set(RoomBookingOccurrence::getCancellationReason, normalizeReason(reason, "预约人取消")));
                if (updated == 0) {
                    throw new ServiceException("当前预约实例不允许取消或不存在");
                }
                if (cancelledOccurrence != null && !RECURRENCE_NONE.equalsIgnoreCase(entity.getRecurrenceType())) {
                    RoomBookingRecurrenceException exception = recurrenceExceptionMapper
                        .selectByBookingAndOccurrence(id, occurrenceNo);
                    if (exception == null) {
                        exception = new RoomBookingRecurrenceException();
                        exception.setBookingId(id);
                        exception.setOccurrenceNo(occurrenceNo);
                        exception.setOccurrenceDate(cancelledOccurrence.getStartAt().toLocalDate());
                    }
                    exception.setReason(normalizeReason(reason, "预约人取消本次循环实例"));
                    exception.setStatus("SKIPPED");
                    if (exception.getId() == null) {
                        recurrenceExceptionMapper.insert(exception);
                    } else {
                        recurrenceExceptionMapper.updateById(exception);
                    }
                }
                syncBookingStatus(entity);
                recordBookingChange(id, occurrenceNo, "CANCEL", previousStatus, entity.getStatus(), normalizeReason(reason, "预约人取消"));
            } else {
                occurrenceMapper.update(null, Wrappers.<RoomBookingOccurrence>lambdaUpdate()
                    .eq(RoomBookingOccurrence::getBookingId, id)
                    .in(RoomBookingOccurrence::getStatus, STATUS_PENDING, STATUS_CONFIRMED)
                    .set(RoomBookingOccurrence::getStatus, STATUS_CANCELLED)
                    .set(RoomBookingOccurrence::getCancellationReason, normalizeReason(reason, "预约人取消")));
                syncBookingStatus(entity);
                recordBookingChange(id, null, "CANCEL", previousStatus, entity.getStatus(), normalizeReason(reason, "预约人取消"));
            }
            recordQuotaUsage(affectedOccurrences, entity.getOrganizerId(), entity.getDeptId(), "REFUND", normalizeReason(reason, "预约人取消"));
            notifyBookingUsers(entity, occurrenceNo == null ? "房间预约系列已取消。" : "房间预约的一次实例已取消。", "CANCEL");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveBooking(Long id) {
        return approveBooking(id, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveBooking(Long id, Integer occurrenceNo) {
        RoomBooking entity = getBooking(id);
        List<RoomBookingOccurrence> allOccurrences = occurrenceMapper.selectByBookingId(id);
        List<RoomBookingOccurrence> occurrences = selectOccurrences(allOccurrences, occurrenceNo);
        ensureBookingReadable(entity, allOccurrences);
        List<RoomBookingOccurrence> pendingOccurrences = occurrences.stream()
            .filter(item -> STATUS_PENDING.equals(item.getStatus()))
            .toList();
        ensureApprovalPermission(pendingOccurrences);
        if (pendingOccurrences.isEmpty()) {
            throw new ServiceException("当前预约状态不允许审批");
        }
        RoomConflictResultVo conflicts = checkConflictInternal(
            pendingOccurrences.stream().map(RoomBookingOccurrence::getRoomId).distinct().toList(),
            pendingOccurrences,
            id,
            false
        );
        if (!conflicts.isAvailable()) {
            throw new ServiceException(formatConflicts(conflicts));
        }
        LocalDateTime now = LocalDateTime.now();
        boolean completedAll = true;
        for (RoomBookingOccurrence occurrence : pendingOccurrences) {
            if (!STATUS_PENDING.equals(occurrence.getStatus())) {
                continue;
            }
            RoomApprovalRule currentRule = currentApprovalRule(occurrence);
            RoomApprovalRule nextRule = nextApprovalRule(occurrence);
            boolean finalStep = currentRule == null || nextRule == null;
            var update = Wrappers.<RoomBookingOccurrence>lambdaUpdate()
                .eq(RoomBookingOccurrence::getId, occurrence.getId())
                .eq(RoomBookingOccurrence::getStatus, STATUS_PENDING);
            if (finalStep) {
                update.set(RoomBookingOccurrence::getStatus, STATUS_CONFIRMED)
                    .set(RoomBookingOccurrence::getApprovalDueAt, null);
            } else {
                update.set(RoomBookingOccurrence::getApprovalStep, nextRule.getStepNo())
                    .set(RoomBookingOccurrence::getApprovalDueAt, approvalDueAt(now, nextRule));
                completedAll = false;
            }
            if (occurrenceMapper.update(null, update) == 0) {
                throw new ServiceException("预约状态已变化，请刷新后重试");
            }
            recordBookingChange(id, occurrence.getOccurrenceNo(), "APPROVE", STATUS_PENDING,
                finalStep ? STATUS_CONFIRMED : STATUS_PENDING,
                finalStep ? null : "已通过第" + currentStep(occurrence) + "级审批，进入第" + nextRule.getStepNo() + "级审批");
            if (finalStep) {
                entity.setApprovedBy(LoginHelper.getUserId());
                entity.setApprovedAt(now);
            }
        }
        syncBookingStatus(entity);
        String message = completedAll && STATUS_CONFIRMED.equals(entity.getStatus())
            ? (occurrenceNo == null ? "房间预约系列已完成全部审批。" : "房间预约的一次实例已完成全部审批。")
            : (occurrenceNo == null ? "房间预约已通过当前审批级别，进入下一审批级别。" : "房间预约的一次实例已进入下一审批级别。");
        notifyBookingUsers(entity, message, "APPROVE");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectBooking(Long id, String reason) {
        return rejectBooking(id, null, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectBooking(Long id, Integer occurrenceNo, String reason) {
        RoomBooking entity = getBooking(id);
        List<RoomBookingOccurrence> allOccurrences = occurrenceMapper.selectByBookingId(id);
        List<RoomBookingOccurrence> occurrences = selectOccurrences(allOccurrences, occurrenceNo);
        ensureBookingReadable(entity, allOccurrences);
        List<RoomBookingOccurrence> pendingOccurrences = occurrences.stream()
            .filter(item -> STATUS_PENDING.equals(item.getStatus()))
            .toList();
        ensureApprovalPermission(pendingOccurrences);
        if (pendingOccurrences.isEmpty()) {
            throw new ServiceException("当前预约状态不允许驳回");
        }
        String normalizedReason = normalizeReason(reason, "管理员驳回");
        String previousStatus = entity.getStatus();
        entity.setRejectedReason(normalizedReason);
        occurrenceMapper.update(null, Wrappers.<RoomBookingOccurrence>lambdaUpdate()
            .eq(RoomBookingOccurrence::getBookingId, id)
            .eq(occurrenceNo != null, RoomBookingOccurrence::getOccurrenceNo, occurrenceNo)
            .eq(RoomBookingOccurrence::getStatus, STATUS_PENDING)
            .set(RoomBookingOccurrence::getStatus, STATUS_REJECTED));
        syncBookingStatus(entity);
        recordBookingChange(id, occurrenceNo, "REJECT", previousStatus, entity.getStatus(), normalizedReason);
        notifyBookingUsers(entity, occurrenceNo == null ? "房间预约系列已被驳回：" + normalizedReason : "房间预约的一次实例已被驳回：" + normalizedReason, "REJECT");
        return true;
    }

    @Override
    public List<RoomBookingVo> queryBookingList(RoomBookingQueryBo bo) {
        return queryCalendar(bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkInBooking(Long id, Integer occurrenceNo) {
        RoomBooking entity = getBooking(id);
        List<RoomBookingOccurrence> occurrences = occurrenceMapper.selectByBookingId(id);
        ensureLifecycleActor(entity, true);
        List<RoomBookingOccurrence> occurrenceGroup = selectLifecycleGroup(occurrences, occurrenceNo, STATUS_CONFIRMED);
        RoomBookingOccurrence occurrence = occurrenceGroup.get(0);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(occurrence.getStartAt().minusMinutes(CHECK_IN_GRACE_MINUTES))) {
            throw new ServiceException("签到只能在预约开始前15分钟内进行");
        }
        if (!now.isBefore(occurrence.getEndAt())) {
            throw new ServiceException("预约已结束，不能签到");
        }
        int updated = occurrenceMapper.update(null, Wrappers.<RoomBookingOccurrence>lambdaUpdate()
            .eq(RoomBookingOccurrence::getBookingId, id)
            .eq(RoomBookingOccurrence::getOccurrenceNo, occurrence.getOccurrenceNo())
            .eq(RoomBookingOccurrence::getStatus, STATUS_CONFIRMED)
            .set(RoomBookingOccurrence::getStatus, STATUS_IN_USE)
            .set(RoomBookingOccurrence::getCheckInAt, now));
        if (updated != occurrenceGroup.size()) {
            throw new ServiceException("预约状态已变化，请刷新后重试");
        }
        String previousStatus = entity.getStatus();
        syncBookingStatus(entity);
        recordBookingChange(id, occurrence.getOccurrenceNo(), "CHECK_IN", previousStatus, entity.getStatus(), null);
        notifyBookingUsers(entity, "房间预约已签到，会议进行中。", "CHECK_IN");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkOutBooking(Long id, Integer occurrenceNo) {
        RoomBooking entity = getBooking(id);
        List<RoomBookingOccurrence> occurrences = occurrenceMapper.selectByBookingId(id);
        ensureLifecycleActor(entity, false);
        List<RoomBookingOccurrence> occurrenceGroup = selectLifecycleGroup(occurrences, occurrenceNo, STATUS_IN_USE);
        RoomBookingOccurrence occurrence = occurrenceGroup.get(0);
        LocalDateTime now = LocalDateTime.now();
        int updated = occurrenceMapper.update(null, Wrappers.<RoomBookingOccurrence>lambdaUpdate()
            .eq(RoomBookingOccurrence::getBookingId, id)
            .eq(RoomBookingOccurrence::getOccurrenceNo, occurrence.getOccurrenceNo())
            .eq(RoomBookingOccurrence::getStatus, STATUS_IN_USE)
            .set(RoomBookingOccurrence::getStatus, STATUS_COMPLETED)
            .set(RoomBookingOccurrence::getCheckOutAt, now));
        if (updated != occurrenceGroup.size()) {
            throw new ServiceException("预约状态已变化，请刷新后重试");
        }
        String previousStatus = entity.getStatus();
        syncBookingStatus(entity);
        recordBookingChange(id, occurrence.getOccurrenceNo(), "CHECK_OUT", previousStatus, entity.getStatus(), null);
        notifyBookingUsers(entity, "房间预约已签退，会议已完成。", "CHECK_OUT");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean releaseBooking(Long id, Integer occurrenceNo, String reason) {
        RoomBooking entity = getBooking(id);
        List<RoomBookingOccurrence> occurrences = occurrenceMapper.selectByBookingId(id);
        ensureLifecycleActor(entity, false);
        List<RoomBookingOccurrence> occurrenceGroup = selectLifecycleGroup(occurrences, occurrenceNo, STATUS_CONFIRMED, STATUS_IN_USE);
        RoomBookingOccurrence occurrence = occurrenceGroup.get(0);
        String normalizedReason = normalizeReason(reason, "预约人提前释放");
        String previousStatus = entity.getStatus();
        int updated = occurrenceMapper.update(null, Wrappers.<RoomBookingOccurrence>lambdaUpdate()
            .eq(RoomBookingOccurrence::getBookingId, id)
            .eq(RoomBookingOccurrence::getOccurrenceNo, occurrence.getOccurrenceNo())
            .in(RoomBookingOccurrence::getStatus, STATUS_CONFIRMED, STATUS_IN_USE)
            .set(RoomBookingOccurrence::getStatus, STATUS_RELEASED)
            .set(RoomBookingOccurrence::getCheckOutAt, LocalDateTime.now())
            .set(RoomBookingOccurrence::getCancellationReason, normalizedReason));
        if (updated != occurrenceGroup.size()) {
            throw new ServiceException("预约状态已变化，请刷新后重试");
        }
        syncBookingStatus(entity);
        recordBookingChange(id, occurrence.getOccurrenceNo(), "RELEASE", previousStatus, entity.getStatus(), normalizedReason);
        recordQuotaUsage(occurrenceGroup, entity.getOrganizerId(), entity.getDeptId(), "REFUND", normalizedReason);
        notifyBookingUsers(entity, "房间预约已提前释放。", "RELEASE");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean extendBooking(RoomBookingExtendBo bo) {
        if (bo == null || bo.getId() == null || bo.getEndAt() == null) {
            throw new ServiceException("预约和新的结束时间不能为空");
        }
        RoomBooking entity = getBooking(bo.getId());
        ensureLifecycleActor(entity, false);
        List<RoomBookingOccurrence> allOccurrences = occurrenceMapper.selectByBookingId(entity.getId());
        Integer occurrenceNo = bo.getOccurrenceNo() == null ? 1 : bo.getOccurrenceNo();
        List<RoomBookingOccurrence> occurrences = allOccurrences.stream()
            .filter(item -> Objects.equals(item.getOccurrenceNo(), occurrenceNo))
            .toList();
        if (occurrences.isEmpty() || occurrences.stream().anyMatch(item -> !STATUS_IN_USE.equals(item.getStatus()))) {
            throw new ServiceException("只有使用中的预约实例可以延长");
        }
        LocalDateTime oldEndAt = occurrences.stream().map(RoomBookingOccurrence::getEndAt).max(LocalDateTime::compareTo).orElse(null);
        if (oldEndAt == null || !bo.getEndAt().isAfter(oldEndAt)) {
            throw new ServiceException("新的结束时间必须晚于当前结束时间");
        }
        List<Long> roomIds = occurrences.stream().map(RoomBookingOccurrence::getRoomId).distinct().toList();
        List<RoomResource> rooms = getAccessibleRooms(roomIds);
        RoomBookingOccurrence requested = buildOccurrence(occurrenceNo, occurrences.get(0).getStartAt(), bo.getEndAt());
        RoomConflictResultVo conflicts = new RoomConflictResultVo();
        for (RoomResource room : rooms) {
            validateRoomWindow(room, requested.getStartAt(), requested.getEndAt(), conflicts, true);
        }
        RoomConflictResultVo occupied = checkConflictInternal(roomIds, List.of(requested), entity.getId());
        conflicts.getConflicts().addAll(occupied.getConflicts());
        conflicts.setAvailable(conflicts.getConflicts().isEmpty());
        if (!conflicts.isAvailable()) {
            throw new ServiceException(formatConflicts(conflicts));
        }
        int updated = occurrenceMapper.update(null, Wrappers.<RoomBookingOccurrence>lambdaUpdate()
            .eq(RoomBookingOccurrence::getBookingId, entity.getId())
            .eq(RoomBookingOccurrence::getOccurrenceNo, occurrenceNo)
            .eq(RoomBookingOccurrence::getStatus, STATUS_IN_USE)
            .set(RoomBookingOccurrence::getEndAt, bo.getEndAt()));
        if (updated != occurrences.size()) {
            throw new ServiceException("预约状态已变化，请刷新后重试");
        }
        if (Objects.equals(occurrenceNo, 1)) {
            entity.setBaseEndAt(bo.getEndAt());
        }
        String previousStatus = entity.getStatus();
        syncBookingStatus(entity);
        recordBookingChange(entity.getId(), occurrenceNo, "EXTEND", previousStatus, entity.getStatus(), "延长至 " + bo.getEndAt());
        notifyBookingUsers(entity, "房间预约已延长至 " + bo.getEndAt() + "。", "EXTEND");
        return true;
    }

    /** 定时处理未签到自动释放和超时自动完成，返回本轮处理数量。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processBookingLifecycle() {
        LocalDateTime now = LocalDateTime.now();
        int processed = 0;
        for (RoomBookingOccurrence occurrence : occurrenceMapper.selectApprovalTimeoutCandidates(now)) {
            List<RoomBookingOccurrence> bookingOccurrences = occurrenceMapper.selectByBookingId(occurrence.getBookingId());
            List<RoomBookingOccurrence> occurrenceGroup = bookingOccurrences.stream()
                .filter(item -> Objects.equals(item.getOccurrenceNo(), occurrence.getOccurrenceNo()))
                .filter(item -> STATUS_PENDING.equals(item.getStatus()))
                .toList();
            if (occurrenceGroup.isEmpty()) {
                continue;
            }
            int updated = occurrenceMapper.update(null, Wrappers.<RoomBookingOccurrence>lambdaUpdate()
                .eq(RoomBookingOccurrence::getBookingId, occurrence.getBookingId())
                .eq(RoomBookingOccurrence::getOccurrenceNo, occurrence.getOccurrenceNo())
                .eq(RoomBookingOccurrence::getStatus, STATUS_PENDING)
                .set(RoomBookingOccurrence::getStatus, STATUS_REJECTED)
                .set(RoomBookingOccurrence::getApprovalDueAt, null)
                .set(RoomBookingOccurrence::getCancellationReason, "当前审批级别超时，系统自动驳回"));
            if (updated == occurrenceGroup.size()) {
                RoomBooking entity = getBooking(occurrence.getBookingId());
                String previousStatus = entity.getStatus();
                entity.setRejectedReason("当前审批级别超时，系统自动驳回");
                syncBookingStatus(entity);
                recordQuotaUsage(occurrenceGroup, entity.getOrganizerId(), entity.getDeptId(), "REFUND", "当前审批级别超时，系统自动驳回");
                recordBookingChange(entity.getId(), occurrence.getOccurrenceNo(), "AUTO_REJECT", previousStatus, entity.getStatus(), "当前审批级别超时，系统自动驳回");
                notifyBookingUsers(entity, "房间预约审批超时，系统已自动驳回。", "AUTO_REJECT");
                processed++;
            }
        }
        for (RoomBookingOccurrence occurrence : occurrenceMapper.selectNoShowCandidates(now.minusMinutes(CHECK_IN_GRACE_MINUTES))) {
            List<RoomBookingOccurrence> bookingOccurrences = occurrenceMapper.selectByBookingId(occurrence.getBookingId());
            List<RoomBookingOccurrence> occurrenceGroup = bookingOccurrences.stream()
                .filter(item -> Objects.equals(item.getOccurrenceNo(), occurrence.getOccurrenceNo()))
                .filter(item -> STATUS_CONFIRMED.equals(item.getStatus()))
                .toList();
            if (occurrenceGroup.isEmpty()) {
                continue;
            }
            int updated = occurrenceMapper.update(null, Wrappers.<RoomBookingOccurrence>lambdaUpdate()
                .eq(RoomBookingOccurrence::getBookingId, occurrence.getBookingId())
                .eq(RoomBookingOccurrence::getOccurrenceNo, occurrence.getOccurrenceNo())
                .eq(RoomBookingOccurrence::getStatus, STATUS_CONFIRMED)
                .set(RoomBookingOccurrence::getStatus, STATUS_RELEASED)
                .set(RoomBookingOccurrence::getCheckOutAt, now)
                .set(RoomBookingOccurrence::getCancellationReason, "超过15分钟未签到，系统自动释放"));
            if (updated == occurrenceGroup.size()) {
                RoomBooking entity = getBooking(occurrence.getBookingId());
                String previousStatus = entity.getStatus();
                syncBookingStatus(entity);
                recordQuotaUsage(occurrenceGroup, entity.getOrganizerId(), entity.getDeptId(), "REFUND", "超过15分钟未签到，系统自动释放");
                recordBookingChange(entity.getId(), occurrence.getOccurrenceNo(), "AUTO_RELEASE", previousStatus, entity.getStatus(), "超过15分钟未签到，系统自动释放");
                notifyBookingUsers(entity, "预约超过15分钟未签到，房间已自动释放。", "AUTO_RELEASE");
                processed++;
            }
        }
        for (RoomBookingOccurrence occurrence : occurrenceMapper.selectExpiredInUseCandidates(now)) {
            List<RoomBookingOccurrence> bookingOccurrences = occurrenceMapper.selectByBookingId(occurrence.getBookingId());
            List<RoomBookingOccurrence> occurrenceGroup = bookingOccurrences.stream()
                .filter(item -> Objects.equals(item.getOccurrenceNo(), occurrence.getOccurrenceNo()))
                .filter(item -> STATUS_IN_USE.equals(item.getStatus()))
                .toList();
            if (occurrenceGroup.isEmpty()) {
                continue;
            }
            int updated = occurrenceMapper.update(null, Wrappers.<RoomBookingOccurrence>lambdaUpdate()
                .eq(RoomBookingOccurrence::getBookingId, occurrence.getBookingId())
                .eq(RoomBookingOccurrence::getOccurrenceNo, occurrence.getOccurrenceNo())
                .eq(RoomBookingOccurrence::getStatus, STATUS_IN_USE)
                .set(RoomBookingOccurrence::getStatus, STATUS_COMPLETED)
                .set(RoomBookingOccurrence::getCheckOutAt, occurrence.getEndAt()));
            if (updated == occurrenceGroup.size()) {
                RoomBooking entity = getBooking(occurrence.getBookingId());
                String previousStatus = entity.getStatus();
                syncBookingStatus(entity);
                recordBookingChange(entity.getId(), occurrence.getOccurrenceNo(), "AUTO_COMPLETE", previousStatus, entity.getStatus(), "预约结束自动签退");
                notifyBookingUsers(entity, "预约已到结束时间，系统自动完成签退。", "AUTO_COMPLETE");
                processed++;
            }
        }
        return processed;
    }

    /** 仅修改循环预约中的一个实例，不改变其它实例的时间和房间。 */
    private Boolean saveOccurrenceBooking(RoomBookingBo bo) {
        if (bo.getOccurrenceNo() == null || bo.getOccurrenceNo() < 1) {
            throw new ServiceException("请选择要修改的预约实例");
        }
        RoomBooking entity = getBooking(bo.getId());
        ensureBookingEditable(entity);
        List<RoomBookingOccurrence> allOccurrences = occurrenceMapper.selectByBookingId(entity.getId());
        List<RoomBookingOccurrence> oldOccurrences = selectOccurrences(allOccurrences, bo.getOccurrenceNo());
        if (oldOccurrences.isEmpty()) {
            throw new ServiceException("预约实例不存在");
        }
        if (oldOccurrences.stream().anyMatch(item -> !STATUS_PENDING.equals(item.getStatus()) && !STATUS_CONFIRMED.equals(item.getStatus()))) {
            throw new ServiceException("当前预约实例不允许修改");
        }
        List<Long> roomIds = normalizeRoomIds(bo.getRoomIds());
        List<RoomResource> rooms = getAccessibleRooms(roomIds);
        RoomBookingOccurrence requested = buildOccurrence(bo.getOccurrenceNo(), bo.getStartAt(), bo.getEndAt());
        RoomConflictResultVo conflicts = checkConflictInternal(roomIds, List.of(requested), entity.getId());
        if (!conflicts.isAvailable()) {
            throw new ServiceException(formatConflicts(conflicts));
        }
        String previousStatus = entity.getStatus();
        recordQuotaUsage(oldOccurrences, entity.getOrganizerId(), entity.getDeptId(), "RELEASE", "单次实例修改，释放旧配额占用");
        boolean quotaApproval = validateBookingQuota(roomIds, List.of(requested), entity.getId());
        boolean approvalRequired = quotaApproval || rooms.stream().anyMatch(this::requiresApproval);
        occurrenceMapper.deleteByIds(oldOccurrences.stream().map(RoomBookingOccurrence::getId).toList());
        for (Long roomId : roomIds) {
            RoomBookingOccurrence item = new RoomBookingOccurrence();
            item.setBookingId(entity.getId());
            item.setRoomId(roomId);
            item.setOccurrenceNo(bo.getOccurrenceNo());
            item.setStartAt(bo.getStartAt());
            item.setEndAt(bo.getEndAt());
            item.setStatus(approvalRequired ? STATUS_PENDING : STATUS_CONFIRMED);
            if (approvalRequired) {
                initializeApproval(item, roomId);
            }
            occurrenceMapper.insert(item);
            recordQuotaUsage(List.of(item), entity.getOrganizerId(), entity.getDeptId(), "CONSUME", "修改房间预约实例");
        }
        if (Objects.equals(bo.getOccurrenceNo(), 1)) {
            entity.setBaseStartAt(bo.getStartAt());
            entity.setBaseEndAt(bo.getEndAt());
        }
        entity.setApprovalRequired(Boolean.TRUE.equals(entity.getApprovalRequired()) || approvalRequired);
        syncBookingStatus(entity);
        recordBookingChange(entity.getId(), bo.getOccurrenceNo(), "UPDATE", previousStatus, entity.getStatus(), null);
        notifyBookingUsers(entity, approvalRequired ? "房间预约的一次实例已修改，等待审批。" : "房间预约的一次实例已修改。", "UPDATE");
        return true;
    }

    private List<RoomBookingOccurrence> selectOccurrences(List<RoomBookingOccurrence> occurrences, Integer occurrenceNo) {
        if (occurrenceNo == null) {
            return occurrences;
        }
        return occurrences.stream().filter(item -> Objects.equals(item.getOccurrenceNo(), occurrenceNo)).toList();
    }

    private RoomBookingOccurrence buildOccurrence(Integer occurrenceNo, LocalDateTime startAt, LocalDateTime endAt) {
        RoomBookingOccurrence occurrence = new RoomBookingOccurrence();
        occurrence.setOccurrenceNo(occurrenceNo == null ? 1 : occurrenceNo);
        occurrence.setStartAt(startAt);
        occurrence.setEndAt(endAt);
        return occurrence;
    }

    private boolean isOccurrenceOperation(RoomBookingBo bo) {
        return bo != null && OPERATION_OCCURRENCE.equalsIgnoreCase(bo.getOperationScope());
    }

    /** 根据实例状态同步系列摘要状态，支持同一循环系列部分取消或部分待审批。 */
    private void syncBookingStatus(RoomBooking entity) {
        List<RoomBookingOccurrence> occurrences = occurrenceMapper.selectByBookingId(entity.getId());
        List<String> statuses = occurrences.stream().map(RoomBookingOccurrence::getStatus).filter(Objects::nonNull).toList();
        String status;
        if (statuses.isEmpty() || statuses.stream().allMatch(STATUS_CANCELLED::equals)) {
            status = STATUS_CANCELLED;
        } else if (statuses.stream().allMatch(STATUS_RELEASED::equals)
            || statuses.stream().allMatch(item -> STATUS_CANCELLED.equals(item) || STATUS_RELEASED.equals(item))) {
            status = STATUS_RELEASED;
        } else if (statuses.stream().anyMatch(STATUS_PENDING::equals)) {
            status = STATUS_PENDING;
        } else if (statuses.stream().anyMatch(STATUS_IN_USE::equals)) {
            status = STATUS_IN_USE;
        } else if (statuses.stream().anyMatch(STATUS_CONFIRMED::equals)) {
            status = STATUS_CONFIRMED;
        } else if (statuses.stream().allMatch(STATUS_COMPLETED::equals)) {
            status = STATUS_COMPLETED;
        } else if (statuses.stream().anyMatch(STATUS_COMPLETED::equals)) {
            status = STATUS_COMPLETED;
        } else if (statuses.stream().allMatch(STATUS_RELEASED::equals)) {
            status = STATUS_RELEASED;
        } else {
            status = STATUS_REJECTED;
        }
        entity.setStatus(status);
        roomBookingMapper.updateById(entity);
    }

    private void recordBookingChange(Long bookingId,
                                     Integer occurrenceNo,
                                     String action,
                                     String fromStatus,
                                    String toStatus,
                                    String reason) {
        RoomBookingApproval record = new RoomBookingApproval();
        record.setBookingId(bookingId);
        record.setOccurrenceNo(occurrenceNo);
        record.setAction(action);
        record.setFromStatus(fromStatus);
        record.setToStatus(toStatus);
        record.setOperatorId(LoginHelper.getUserId());
        record.setReason(reason);
        approvalMapper.insert(record);
        notifyRoomBookingChanged(bookingId, action);
    }

    /**
     * 预约状态变化后广播轻量刷新事件。事件不携带预约详情，客户端会重新按当前用户权限
     * 拉取房间和当天日程，避免公共房间的占用状态只能等轮询才更新，也不会泄露私密预约信息。
     */
    private void notifyRoomBookingChanged(Long bookingId, String action) {
        if (bookingId == null) {
            return;
        }
        Runnable publish = () -> {
            try {
                PushPayloadDTO payload = PushPayloadDTO.of(PushTypeEnum.CUSTOM, PushSourceEnum.BACKEND, "", Map.of(
                    "module", "ROOM_BOOKING",
                    "bookingId", bookingId,
                    "action", action == null ? "UPDATE" : action,
                    "refresh", true
                ), "/department/room");
                messageService.publishAll(payload);
            } catch (Exception ignored) {
                // 推送不可用时，房间页仍会通过轮询和本地事件最终收敛状态。
            }
        };
        RoomBookingTransactionPublisher.afterCommitOrNow(publish);
    }

    /** 房间资源发生变化时广播轻量自定义事件；前端只据此重新拉取当前用户有权查看的数据。 */
    private void notifyRoomResourceChanged(String action) {
        Runnable publish = () -> {
            try {
                PushPayloadDTO payload = PushPayloadDTO.of(PushTypeEnum.CUSTOM, PushSourceEnum.BACKEND, "", Map.of(
                    "module", "ROOM_RESOURCE",
                    "action", action == null ? "UPDATE" : action
                ), "/department/room");
                messageService.publishAll(payload);
            } catch (Exception ignored) {
                // 资源数据已经成功写入，推送通道不可用时由前端轮询兜底。
            }
        };
        RoomBookingTransactionPublisher.afterCommitOrNow(publish);
    }

    /** 业务通知失败不能回滚预约事实，消息盒子会在推送服务可用时正常展示。 */
    private void notifyBookingUsers(RoomBooking entity, String message, String action) {
        if (entity == null || entity.getId() == null) {
            return;
        }
        LinkedHashSet<Long> userIds = new LinkedHashSet<>();
        if (entity.getOrganizerId() != null) {
            userIds.add(entity.getOrganizerId());
        }
        userIds.addAll(attendeeMapper.selectByBookingId(entity.getId()).stream()
            .map(RoomBookingAttendee::getUserId).filter(Objects::nonNull).toList());
        if (STATUS_PENDING.equals(entity.getStatus())) {
            List<RoomBookingOccurrence> pendingOccurrences = occurrenceMapper.selectByBookingId(entity.getId()).stream()
                .filter(item -> STATUS_PENDING.equals(item.getStatus())).toList();
            for (RoomBookingOccurrence occurrence : pendingOccurrences) {
                RoomApprovalRule rule = currentApprovalRule(occurrence);
                if (rule == null) {
                    userIds.addAll(roomResourceAclMapper.selectApproverIds(occurrence.getRoomId()));
                    userIds.addAll(roomBookingMapper.selectApproverIds());
                } else if (SUBJECT_ALL.equals(rule.getApproverType())) {
                    userIds.addAll(roomBookingMapper.selectApproverIds());
                } else {
                    userIds.addAll(approvalRuleMapper.selectApproverIds(occurrence.getRoomId(), currentStep(occurrence)));
                }
            }
        }
        if (userIds.isEmpty()) {
            return;
        }
        List<Long> recipients = List.copyOf(userIds);
        Long bookingId = entity.getId();
        Runnable publish = () -> {
            try {
                PushPayloadDTO payload = PushPayloadDTO.of("MESSAGE", "BACKEND", message,
                    Map.of("bookingId", bookingId, "action", action, "module", "ROOM_BOOKING"));
                payload.setPath("/department/room");
                messageService.publishMessage(recipients, payload);
            } catch (Exception ignored) {
                // 消息通道不可用时不影响预约、审批和取消结果。
            }
            notifyBookingEmail(recipients, message, bookingId);
            notifyBookingWecom(message, bookingId);
        };
        RoomBookingTransactionPublisher.afterCommitOrNow(publish);
    }

    /** 按系统 SMTP 开关发送邮件；未启用或用户没有邮箱时静默跳过。 */
    private void notifyBookingEmail(Collection<Long> userIds, String message, Long bookingId) {
        if (!Boolean.TRUE.equals(mailProperties.getEnabled()) || userIds == null || userIds.isEmpty()) {
            return;
        }
        try {
            List<String> addresses = userIds.stream()
                .map(userService::selectEmailById)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .toList();
            if (addresses.isEmpty()) {
                return;
            }
            MailBuilder.of()
                .to(addresses)
                .subject("房间预约通知")
                .text(message + "\n预约编号：" + bookingId + "\n请登录系统查看详情。")
                .send();
        } catch (Exception ignored) {
            // 邮件通道失败不影响已经落库的预约事实和系统消息。
        }
    }

    /** 向配置的企业微信群机器人推送文本通知；未配置时不发起网络请求。 */
    private void notifyBookingWecom(String message, Long bookingId) {
        String webhookUrl = notificationProperties.getWecomWebhookUrl();
        if (StringUtils.isBlank(webhookUrl)) {
            return;
        }
        try (HttpResponse response = HttpRequest.post(webhookUrl)
            .timeout(3000)
            .header("Content-Type", "application/json")
            .body(JsonUtils.toJsonString(Map.of(
                "msgtype", "text",
                "text", Map.of("content", message + "\n预约编号：" + bookingId)
            )))
            .execute()) {
            if (!response.isOk()) {
                // 机器人返回非 2xx 时仅记录通道失败，不回滚预约。
                return;
            }
        } catch (Exception ignored) {
            // 外部通知失败不影响预约、审批和取消结果。
        }
    }

    private RoomConflictResultVo checkConflictInternal(List<Long> roomIds,
                                                       List<RoomBookingOccurrence> occurrences,
                                                       Long excludeBookingId) {
        return checkConflictInternal(roomIds, occurrences, excludeBookingId, true);
    }

    /** 审批人可能没有普通 BOOK 权限，但仍需对自己授权审批的房间重新做冲突校验。 */
    private RoomConflictResultVo checkConflictInternal(List<Long> roomIds,
                                                       List<RoomBookingOccurrence> occurrences,
                                                       Long excludeBookingId,
                                                       boolean requireBookingPermission) {
        RoomConflictResultVo result = new RoomConflictResultVo();
        List<RoomResource> accessibleRooms = requireBookingPermission
            ? getAccessibleRooms(roomIds)
            : getApprovalAccessibleRooms(roomIds, occurrences);
        Map<Long, RoomResource> rooms = accessibleRooms.stream()
            .collect(Collectors.toMap(RoomResource::getId, room -> room));
        boolean recurringRequest = occurrences.stream()
            .map(RoomBookingOccurrence::getOccurrenceNo)
            .filter(Objects::nonNull)
            .distinct()
            .count() > 1;
        for (RoomBookingOccurrence occurrence : occurrences) {
            for (Long roomId : roomIds) {
                RoomResource room = rooms.get(roomId);
                if (room == null) {
                    addConflict(result, roomId, null, occurrence, "房间不存在或无权预约");
                    continue;
                }
                validateRoomWindow(room, occurrence.getStartAt(), occurrence.getEndAt(), result);
                if (recurringRequest && !Boolean.TRUE.equals(room.getAllowRecurring())) {
                    addConflict(result, roomId, room, occurrence, "房间不允许创建循环预约");
                }
                if (!result.isAvailable() && hasConflictFor(result, roomId, occurrence)) {
                    continue;
                }
                List<RoomBookingBlock> blocks = roomBookingBlockMapper.selectConflicts(
                    roomId, occurrence.getStartAt(), occurrence.getEndAt()
                );
                for (RoomBookingBlock block : blocks) {
                    addConflict(result, roomId, room, occurrence,
                        StringUtils.isBlank(block.getReason()) ? "该房间在此时段已被封锁" : "该房间在此时段已被封锁：" + block.getReason());
                    break;
                }
                if (!result.isAvailable() && hasConflictFor(result, roomId, occurrence)) {
                    continue;
                }
                List<RoomBookingOccurrence> conflicts = occurrenceMapper.selectConflicts(
                    roomId, occurrence.getStartAt(), occurrence.getEndAt(), excludeBookingId
                );
                for (RoomBookingOccurrence conflict : conflicts) {
                    addConflict(result, roomId, room, occurrence, "与已有预约时间冲突");
                    break;
                }
            }
        }
        result.setAvailable(result.getConflicts().isEmpty());
        return result;
    }

    private boolean hasConflictFor(RoomConflictResultVo result, Long roomId, RoomBookingOccurrence occurrence) {
        return result.getConflicts().stream().anyMatch(item -> Objects.equals(item.getRoomId(), roomId)
            && Objects.equals(item.getStartAt(), occurrence.getStartAt())
            && Objects.equals(item.getEndAt(), occurrence.getEndAt()));
    }

    private void validateRoomWindow(RoomResource room,
                                     LocalDateTime startAt,
                                     LocalDateTime endAt,
                                     RoomConflictResultVo result) {
        validateRoomWindow(room, startAt, endAt, result, false);
    }

    private void validateRoomWindow(RoomResource room,
                                    LocalDateTime startAt,
                                    LocalDateTime endAt,
                                    RoomConflictResultVo result,
                                    boolean allowStarted) {
        if (!RoomBookingPermissionEvaluator.isBookableStatus(room.getStatus())) {
            addConflict(result, room.getId(), room, startAt, endAt, "房间当前不可预约");
            return;
        }
        if (!allowStarted && startAt.isBefore(LocalDateTime.now())) {
            addConflict(result, room.getId(), room, startAt, endAt, "不能预约已经开始的时间");
        }
        if (room.getMaxAdvanceDays() != null && room.getMaxAdvanceDays() >= 0
            && startAt.toLocalDate().isAfter(LocalDate.now().plusDays(room.getMaxAdvanceDays()))) {
            addConflict(result, room.getId(), room, startAt, endAt, "超出房间允许的提前预约范围");
        }
        if (room.getMaxDurationMinutes() != null
            && java.time.Duration.between(startAt, endAt).toMinutes() > room.getMaxDurationMinutes()) {
            addConflict(result, room.getId(), room, startAt, endAt, "超过房间最长预约时长");
        }
        LocalTime open = room.getOpenTime() == null ? LocalTime.of(8, 0) : room.getOpenTime();
        LocalTime close = room.getCloseTime() == null ? LocalTime.of(22, 0) : room.getCloseTime();
        boolean crossDay = !startAt.toLocalDate().equals(endAt.toLocalDate());
        boolean allowCrossDay = Boolean.TRUE.equals(room.getAllowCrossDay());
        if (!RoomBookingPermissionEvaluator.isValidBookingWindow(startAt, endAt, open, close, allowCrossDay)) {
            addConflict(result, room.getId(), room, startAt, endAt,
                crossDay && !allowCrossDay ? "房间不允许跨日预约" : "不在房间营业时间内");
        }
        LocalDate current = startAt.toLocalDate();
        LocalDate last = endAt.toLocalDate();
        while (!current.isAfter(last)) {
            RoomCalendarException exception = calendarExceptionMapper.selectByDeptAndDate(room.getDeptId(), current);
            if (exception != null && EXCEPTION_HOLIDAY.equalsIgnoreCase(exception.getExceptionType())) {
                addConflict(result, room.getId(), room, startAt, endAt,
                    StringUtils.isBlank(exception.getExceptionName()) ? "该日期为节假日，不允许预约" : "该日期为节假日：" + exception.getExceptionName());
            } else if (!Boolean.TRUE.equals(room.getAllowWeekend()) && isWeekend(current)
                && (exception == null || !EXCEPTION_WORKDAY.equalsIgnoreCase(exception.getExceptionType()))) {
                addConflict(result, room.getId(), room, startAt, endAt, "房间不允许周末预约");
            }
            current = current.plusDays(1);
        }
    }

    private List<RoomBookingOccurrence> buildOccurrences(RoomBookingBo bo) {
        return RoomBookingRecurrenceCalculator.calculate(
            bo.getRecurrenceType(), bo.getStartAt(), bo.getEndAt(), bo.getRecurrenceInterval(),
            bo.getRecurrenceCount(), bo.getRecurrenceUntil(), MAX_OCCURRENCES
        ).stream().map(slot -> {
            RoomBookingOccurrence occurrence = new RoomBookingOccurrence();
            occurrence.setOccurrenceNo(slot.occurrenceNo());
            occurrence.setStartAt(slot.startAt());
            occurrence.setEndAt(slot.endAt());
            return occurrence;
        }).toList();
    }

    private String normalizeRecurrenceType(String value) {
        if (StringUtils.isBlank(value)) {
            return RECURRENCE_NONE;
        }
        String normalized = value.trim().toUpperCase();
        if (!Set.of(RECURRENCE_NONE, "DAILY", "WEEKLY", "MONTHLY").contains(normalized)) {
            throw new ServiceException("不支持的循环类型：" + value);
        }
        return normalized;
    }

    private void validateBookingBo(RoomBookingBo bo) {
        if (bo == null || bo.getStartAt() == null || bo.getEndAt() == null) {
            throw new ServiceException("预约开始和结束时间不能为空");
        }
        if (!bo.getEndAt().isAfter(bo.getStartAt())) {
            throw new ServiceException("预约结束时间必须晚于开始时间");
        }
        if (bo.getRoomIds() == null || normalizeRoomIds(bo.getRoomIds()).isEmpty()) {
            throw new ServiceException("至少选择一个房间");
        }
        if (StringUtils.isBlank(bo.getTitle())) {
            throw new ServiceException("会议主题不能为空");
        }
        if (!Boolean.TRUE.equals(bo.getTermsAccepted())) {
            throw new ServiceException("请先确认房间预约使用条款");
        }
    }

    private RoomBookingQueryBo normalizeQuery(RoomBookingQueryBo source) {
        RoomBookingQueryBo query = source == null ? new RoomBookingQueryBo() : source;
        if (query.getBeginAt() == null) {
            query.setBeginAt(LocalDate.now().atStartOfDay());
        }
        if (query.getEndAt() == null) {
            query.setEndAt(query.getBeginAt().plusDays(31));
        }
        if (!query.getEndAt().isAfter(query.getBeginAt())) {
            throw new ServiceException("日历结束时间必须晚于开始时间");
        }
        return query;
    }

    private List<RoomBookingVo> decorateBookingList(List<RoomBookingVo> list) {
        Long currentUserId = LoginHelper.getUserId();
        boolean manageAll = canManageAll();
        Set<Long> bookingIds = list.stream()
            .map(RoomBookingVo::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> attendeeBookingIds = currentUserId == null || bookingIds.isEmpty()
            ? Set.of()
            : new LinkedHashSet<>(attendeeMapper.selectBookingIdsByUserId(bookingIds, currentUserId));
        for (RoomBookingVo item : list) {
            boolean mine = Objects.equals(item.getOrganizerId(), currentUserId);
            boolean attendee = item.getId() != null && attendeeBookingIds.contains(item.getId());
            boolean active = STATUS_PENDING.equals(item.getStatus()) || STATUS_CONFIRMED.equals(item.getStatus());
            item.setMine(mine);
            item.setCanEdit(active && (mine || manageAll));
            item.setCanCancel(active && (mine || manageAll));
            item.setCanCheckIn(canCheckIn(item, mine || manageAll));
            item.setCanCheckOut(canCheckOut(item, mine || manageAll));
            item.setCanRelease(canRelease(item, mine || manageAll));
            item.setCanExtend(canExtend(item, mine || manageAll));
            item.setCanApprove(canApprove(item));
            if (RoomBookingPrivacyEvaluator.shouldMaskPrivate(item.getVisibility(), mine, attendee, manageAll)) {
                item.setTitle("已被预约");
                item.setDescription(null);
                item.setOrganizerName("已隐藏");
                item.setOrganizerId(null);
                item.setDeptName("已隐藏");
                item.setDeptId(null);
                item.setAttendeeNames(null);
            }
        }
        return list;
    }

    private String formatConflicts(RoomConflictResultVo conflicts) {
        String details = conflicts.getConflicts().stream().limit(3)
            .map(item -> (item.getRoomName() == null ? "所选房间" : item.getRoomName()) + "：" + item.getReason())
            .distinct().collect(Collectors.joining("；"));
        return "预约不可用" + (StringUtils.isBlank(details) ? "" : "：" + details);
    }

    private List<RoomResourceVo> decorateRoomList(List<RoomResourceVo> rooms) {
        if (rooms == null) {
            return List.of();
        }
        for (RoomResourceVo room : rooms) {
            room.setAmenityIds(amenityRelationMapper.selectAmenityIds(room.getId()));
            room.setAmenityNames(amenityRelationMapper.selectAmenityNames(room.getId()));
        }
        return rooms;
    }

    private void syncRoomAmenities(RoomResource room) {
        if (room == null || room.getAmenityIds() == null) {
            return;
        }
        List<Long> amenityIds = normalizeAmenityIds(room.getAmenityIds());
        for (Long amenityId : amenityIds) {
            RoomAmenity amenity = amenityMapper.selectById(amenityId);
            if (amenity == null || !"ENABLED".equalsIgnoreCase(amenity.getStatus())
                || (!canManageAll() && !Objects.equals(amenity.getDeptId(), departmentAccessService.currentDeptId()))) {
                throw new ServiceException("选择的房间设施不存在、已停用或无权使用");
            }
        }
        amenityRelationMapper.deleteByRoomId(room.getId());
        for (Long amenityId : amenityIds) {
            RoomAmenityRelation relation = new RoomAmenityRelation();
            relation.setRoomId(room.getId());
            relation.setAmenityId(amenityId);
            amenityRelationMapper.insert(relation);
        }
    }

    private List<Long> normalizeAmenityIds(Collection<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).collect(Collectors.collectingAndThen(
            Collectors.toCollection(LinkedHashSet::new), ArrayList::new
        ));
    }

    private void copyAmenityBo(RoomAmenityBo bo, RoomAmenity entity) {
        if (bo == null || StringUtils.isBlank(bo.getAmenityName())) {
            throw new ServiceException("设施名称不能为空");
        }
        entity.setAmenityName(StringUtils.trim(bo.getAmenityName()));
        entity.setCategory(StringUtils.isBlank(bo.getCategory()) ? "基础设施" : StringUtils.trim(bo.getCategory()));
        entity.setIcon(StringUtils.isBlank(bo.getIcon()) ? "Grid" : StringUtils.trim(bo.getIcon()));
        entity.setSortNo(bo.getSortNo() == null ? 0 : bo.getSortNo());
        String status = StringUtils.trim(bo.getStatus());
        entity.setStatus("DISABLED".equalsIgnoreCase(status) ? "DISABLED"
            : "DRAFT".equalsIgnoreCase(status) ? "DRAFT" : ROOM_ENABLED);
        entity.setRemark(StringUtils.trim(bo.getRemark()));
    }

    private void checkAmenityDuplicate(RoomAmenity entity) {
        RoomAmenity duplicate = amenityMapper.selectOne(Wrappers.<RoomAmenity>lambdaQuery()
            .eq(RoomAmenity::getDeptId, entity.getDeptId())
            .eq(RoomAmenity::getAmenityName, entity.getAmenityName())
            .ne(entity.getId() != null, RoomAmenity::getId, entity.getId()));
        if (duplicate != null) {
            throw new ServiceException("本部门已存在相同名称的房间设施");
        }
    }

    private void copyRoomBo(RoomResourceBo bo, RoomResource entity) {
        entity.setRoomCode(StringUtils.trim(bo.getRoomCode()));
        entity.setRoomName(StringUtils.trim(bo.getRoomName()));
        entity.setRoomType(StringUtils.trim(bo.getRoomType()));
        entity.setLocation(StringUtils.trim(bo.getLocation()));
        if (bo.getFloorPlanId() == null) {
            entity.setFloorPlanId(null);
        } else {
            getFloorPlan(bo.getFloorPlanId(), true);
            entity.setFloorPlanId(bo.getFloorPlanId());
        }
        entity.setBuildingName(StringUtils.trim(bo.getBuildingName()));
        entity.setFloorName(StringUtils.trim(bo.getFloorName()));
        entity.setAreaName(StringUtils.trim(bo.getAreaName()));
        entity.setMergeGroup(StringUtils.trim(bo.getMergeGroup()));
        entity.setDisplayImage(StringUtils.trim(bo.getDisplayImage()));
        entity.setPhotoUrls(StringUtils.trim(bo.getPhotoUrls()));
        entity.setUsageGuide(StringUtils.trim(bo.getUsageGuide()));
        entity.setManagerUserId(bo.getManagerUserId());
        entity.setDisplayScreen(bo.getDisplayScreen() == null || bo.getDisplayScreen());
        entity.setCapacity(bo.getCapacity() == null ? 1 : bo.getCapacity());
        entity.setAmenities(StringUtils.trim(bo.getAmenities()));
        if (bo.getAmenityIds() != null) {
            entity.setAmenityIds(normalizeAmenityIds(bo.getAmenityIds()));
        }
        String status = StringUtils.trim(bo.getStatus());
        entity.setStatus(StringUtils.isBlank(status)
            ? (entity.getStatus() == null ? ROOM_ENABLED : entity.getStatus())
            : normalizeRoomStatus(status));
        entity.setScopeType(SCOPE_DEPT.equalsIgnoreCase(bo.getScopeType()) ? SCOPE_DEPT : SCOPE_PUBLIC);
        entity.setOpenTime(bo.getOpenTime() == null ? LocalTime.of(8, 0) : bo.getOpenTime());
        entity.setCloseTime(bo.getCloseTime() == null ? LocalTime.of(22, 0) : bo.getCloseTime());
        entity.setAllowWeekend(bo.getAllowWeekend() == null || bo.getAllowWeekend());
        entity.setAllowCrossDay(Boolean.TRUE.equals(bo.getAllowCrossDay()));
        entity.setApprovalMode(APPROVAL_REQUIRED.equalsIgnoreCase(bo.getApprovalMode()) ? APPROVAL_REQUIRED : APPROVAL_NONE);
        entity.setMaxAdvanceDays(bo.getMaxAdvanceDays() == null ? 90 : bo.getMaxAdvanceDays());
        entity.setMaxDurationMinutes(bo.getMaxDurationMinutes() == null ? 240 : bo.getMaxDurationMinutes());
        entity.setAllowRecurring(bo.getAllowRecurring() == null || bo.getAllowRecurring());
        entity.setRemark(bo.getRemark());
    }

    private void validateRoomPolicy(RoomResource entity) {
        if (entity == null || !RoomBookingPermissionEvaluator.isValidOpeningWindow(entity.getOpenTime(), entity.getCloseTime())) {
            throw new ServiceException("房间营业结束时间必须晚于开始时间");
        }
        if (entity.getMaxAdvanceDays() == null || entity.getMaxAdvanceDays() < 0) {
            throw new ServiceException("提前预约天数不能小于0");
        }
        if (entity.getMaxDurationMinutes() == null || entity.getMaxDurationMinutes() <= 0) {
            throw new ServiceException("最长预约时长必须大于0");
        }
    }

    private void copyFloorPlanBo(RoomFloorPlanBo bo, RoomFloorPlan entity) {
        if (bo == null) {
            throw new ServiceException("平面图参数不能为空");
        }
        if (StringUtils.isNotBlank(bo.getMapData()) && !JsonUtils.isJsonObject(bo.getMapData())) {
            throw new ServiceException("平面图图形数据必须是合法的 JSON 对象");
        }
        try {
            RoomFloorPlanMapDataValidator.validate(bo.getMapData());
        } catch (IllegalArgumentException exception) {
            throw new ServiceException(exception.getMessage());
        }
        entity.setPlanName(StringUtils.trim(bo.getPlanName()));
        entity.setBuildingName(StringUtils.trim(bo.getBuildingName()));
        entity.setFloorName(StringUtils.trim(bo.getFloorName()));
        entity.setFloorNo(bo.getFloorNo() == null ? 1 : bo.getFloorNo());
        entity.setMapImage(StringUtils.trim(bo.getMapImage()));
        entity.setMapData(StringUtils.trim(bo.getMapData()));
        String status = StringUtils.trim(bo.getStatus());
        if (StringUtils.isBlank(status)) {
            status = entity.getId() == null ? "DRAFT" : entity.getStatus();
        }
        entity.setStatus("DISABLED".equalsIgnoreCase(status) ? "DISABLED"
            : "DRAFT".equalsIgnoreCase(status) ? "DRAFT" : ROOM_ENABLED);
        entity.setRemark(StringUtils.trim(bo.getRemark()));
    }

    private void validatePublishedFloorPlan(RoomFloorPlan plan) {
        if (plan == null || !ROOM_ENABLED.equalsIgnoreCase(plan.getStatus())) {
            return;
        }
        try {
            RoomFloorPlanMapDataValidator.validateForPublish(plan.getMapData());
            Set<String> floorRoomIds = roomResourceMapper.selectList(Wrappers.<RoomResource>lambdaQuery()
                    .select(RoomResource::getId)
                    .eq(RoomResource::getFloorPlanId, plan.getId())
                    .eq(RoomResource::getDelFlag, "0"))
                .stream()
                .map(RoomResource::getId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
            RoomFloorPlanMapDataBindingValidator.validatePublishedBindings(plan.getMapData(), floorRoomIds);
        } catch (IllegalArgumentException exception) {
            throw new ServiceException("已发布平面图不符合发布校验：" + exception.getMessage());
        }
    }

    private Boolean changeFloorPlanPublication(Long floorPlanId, boolean publish) {
        requireRoomManage();
        RoomFloorPlan plan = getFloorPlan(floorPlanId, true);
        String targetStatus = publish ? ROOM_ENABLED : "DRAFT";
        if (targetStatus.equalsIgnoreCase(plan.getStatus())) {
            return true;
        }
        plan.setStatus(targetStatus);
        validatePublishedFloorPlan(plan);
        if (roomFloorPlanMapper.updateById(plan) <= 0) {
            throw new ServiceException("平面图已被其他人修改，请刷新后重试");
        }
        recordFloorPlanVersion(plan, publish ? "发布平面图" : "撤回为草稿");
        notifyRoomResourceChanged(publish ? "FLOOR_PLAN_PUBLISH" : "FLOOR_PLAN_UNPUBLISH");
        return true;
    }

    private void recordFloorPlanVersion(RoomFloorPlan plan, String versionNote) {
        if (plan == null || plan.getId() == null) {
            return;
        }
        RoomFloorPlanVersion snapshot = new RoomFloorPlanVersion();
        snapshot.setFloorPlanId(plan.getId());
        Integer maxVersionNo = roomFloorPlanVersionMapper.selectMaxVersionNo(plan.getId());
        snapshot.setVersionNo((maxVersionNo == null ? 0 : maxVersionNo) + 1);
        snapshot.setPlanName(plan.getPlanName());
        snapshot.setBuildingName(plan.getBuildingName());
        snapshot.setFloorName(plan.getFloorName());
        snapshot.setFloorNo(plan.getFloorNo());
        snapshot.setMapImage(plan.getMapImage());
        snapshot.setMapData(plan.getMapData());
        snapshot.setStatus(plan.getStatus());
        snapshot.setRemark(plan.getRemark());
        snapshot.setVersionNote(StringUtils.trim(versionNote));
        roomFloorPlanVersionMapper.insert(snapshot);
    }

    private void checkFloorPlanDuplicate(RoomFloorPlan entity) {
        RoomFloorPlan duplicate = roomFloorPlanMapper.selectOne(Wrappers.<RoomFloorPlan>lambdaQuery()
            .eq(RoomFloorPlan::getDeptId, entity.getDeptId())
            .eq(RoomFloorPlan::getBuildingName, entity.getBuildingName())
            .eq(RoomFloorPlan::getFloorNo, entity.getFloorNo())
            .ne(entity.getId() != null, RoomFloorPlan::getId, entity.getId()));
        if (duplicate != null) {
            throw new ServiceException("该建筑物和楼层序号已经配置平面图");
        }
    }

    private void copyBookingBo(RoomBookingBo bo, RoomBooking entity) {
        entity.setTitle(StringUtils.trim(bo.getTitle()));
        entity.setDescription(bo.getDescription());
        entity.setVisibility("PRIVATE".equalsIgnoreCase(bo.getVisibility()) ? "PRIVATE" : "PUBLIC");
        entity.setRecurrenceType(StringUtils.isBlank(bo.getRecurrenceType()) ? RECURRENCE_NONE : bo.getRecurrenceType().toUpperCase());
        entity.setRecurrenceInterval(bo.getRecurrenceInterval() == null ? 1 : bo.getRecurrenceInterval());
        entity.setRecurrenceUntil(bo.getRecurrenceUntil());
        entity.setRecurrenceCount(bo.getRecurrenceCount());
        entity.setBaseStartAt(bo.getStartAt());
        entity.setBaseEndAt(bo.getEndAt());
    }

    private void checkRoomDuplicate(RoomResource entity) {
        RoomResource duplicate = roomResourceMapper.selectOne(Wrappers.<RoomResource>lambdaQuery()
            .eq(RoomResource::getDeptId, entity.getDeptId())
            .eq(RoomResource::getRoomCode, entity.getRoomCode())
            .ne(entity.getId() != null, RoomResource::getId, entity.getId()));
        if (duplicate != null) {
            throw new ServiceException("本部门已存在相同房间编号");
        }
    }

    private RoomResource getRoom(Long id, boolean requireManageForPrivate) {
        RoomResource room = roomResourceMapper.selectById(id);
        if (room == null) {
            throw new ServiceException("房间不存在");
        }
        boolean sameDept = Objects.equals(room.getDeptId(), departmentAccessService.currentDeptId());
        if (requireManageForPrivate) {
            if (!canManageAll() && !sameDept) {
                throw new ServiceException("您没有管理该房间的权限");
            }
        } else if (!canAccessRoom(room, PERMISSION_VIEW)
            && !(sameDept && StpUtil.hasPermission("department:room:manage"))) {
            throw new ServiceException("您没有访问该房间的权限");
        }
        return room;
    }

    private RoomFloorPlan getFloorPlan(Long id, boolean requireManageForPrivate) {
        if (id == null) {
            throw new ServiceException("平面图主键不能为空");
        }
        RoomFloorPlan plan = roomFloorPlanMapper.selectById(id);
        if (plan == null) {
            throw new ServiceException("平面图不存在");
        }
        if (requireManageForPrivate) {
            if (!canManageAll() && !Objects.equals(plan.getDeptId(), departmentAccessService.currentDeptId())) {
                throw new ServiceException("您没有管理该平面图的权限");
            }
        } else if (!canManageAll()) {
            boolean accessible = roomFloorPlanMapper.selectOptions(
                departmentAccessService.currentDeptId(), LoginHelper.getUserId(), false,
                StpUtil.hasPermission("department:room:manage")
            ).stream().anyMatch(item -> Objects.equals(item.getId(), id));
            if (!accessible) {
                throw new ServiceException("您没有访问该平面图的权限");
            }
        }
        return plan;
    }

    private List<RoomResource> getAccessibleRooms(Collection<Long> ids) {
        List<Long> normalized = normalizeRoomIds(ids);
        List<RoomResource> rooms = normalized.isEmpty() ? List.of() : roomResourceMapper.selectBatchIds(normalized);
        if (rooms.size() != normalized.size()) {
            throw new ServiceException("选择的房间不存在");
        }
        for (RoomResource room : rooms) {
            if (!RoomBookingPermissionEvaluator.isBookableStatus(room.getStatus())) {
                throw new ServiceException("房间当前不可预约：" + room.getRoomName());
            }
            if (blacklistMapper.countMatched(room.getId(), LoginHelper.getUserId(), departmentAccessService.currentDeptId()) > 0) {
                throw new ServiceException("您在该房间预约黑名单中：" + room.getRoomName());
            }
            if (!canAccessRoom(room, PERMISSION_BOOK)) {
                throw new ServiceException("您没有预约该房间的权限：" + room.getRoomName());
            }
        }
        return rooms;
    }

    private List<RoomResource> getApprovalAccessibleRooms(Collection<Long> ids,
                                                           List<RoomBookingOccurrence> occurrences) {
        List<Long> normalized = normalizeRoomIds(ids);
        List<RoomResource> rooms = normalized.isEmpty() ? List.of() : roomResourceMapper.selectBatchIds(normalized);
        if (rooms.size() != normalized.size()) {
            throw new ServiceException("选择的房间不存在");
        }
        Long userId = LoginHelper.getUserId();
        Long deptId = departmentAccessService.currentDeptId();
        for (RoomResource room : rooms) {
            boolean ruleMember = occurrences != null && occurrences.stream()
                .filter(item -> Objects.equals(item.getRoomId(), room.getId()))
                .map(this::currentApprovalRule)
                .filter(Objects::nonNull)
                .anyMatch(rule -> isApprovalRuleMember(rule, userId, deptId));
            if (!canManageAll() && !ruleMember
                && !canAccessRoom(room, PERMISSION_VIEW)
                && !canAccessRoom(room, PERMISSION_APPROVE)) {
                throw new ServiceException("您没有访问该审批房间的权限：" + room.getRoomName());
            }
        }
        return rooms;
    }

    private RoomBooking getBooking(Long id) {
        RoomBooking entity = roomBookingMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("预约不存在");
        }
        return entity;
    }

    private void ensureBookingReadable(RoomBooking entity, List<RoomBookingOccurrence> occurrences) {
        Long userId = LoginHelper.getUserId();
        if (Objects.equals(entity.getOrganizerId(), userId) || canManageAll() || isAttendee(entity.getId(), userId)) {
            return;
        }
        for (RoomBookingOccurrence occurrence : occurrences) {
            if (canReadBookingOccurrenceRoom(occurrence)) return;
        }
        throw new ServiceException("您没有访问该预约的权限");
    }

    private boolean canReadBookingOccurrenceRoom(RoomBookingOccurrence occurrence) {
        if (occurrence == null || occurrence.getRoomId() == null) {
            return false;
        }
        RoomResource room = roomResourceMapper.selectById(occurrence.getRoomId());
        if (room == null) return false;
        if (canAccessRoom(room, PERMISSION_VIEW)) return true;
        if (!STATUS_PENDING.equals(occurrence.getStatus())) {
            return false;
        }
        if (canAccessRoom(room, PERMISSION_APPROVE)) {
            return true;
        }
        RoomApprovalRule current = currentApprovalRule(occurrence);
        return current != null && isApprovalRuleMember(current, LoginHelper.getUserId(), departmentAccessService.currentDeptId());
    }

    private void ensureBookingEditable(RoomBooking entity) {
        if (!Objects.equals(entity.getOrganizerId(), LoginHelper.getUserId()) && !canManageAll()) {
            throw new ServiceException("只能编辑或取消自己的预约");
        }
        if (STATUS_CANCELLED.equals(entity.getStatus()) || STATUS_REJECTED.equals(entity.getStatus())) {
            throw new ServiceException("当前预约状态不允许修改");
        }
    }

    private RoomBookingOccurrence selectLifecycleOccurrence(List<RoomBookingOccurrence> occurrences,
                                                            Integer occurrenceNo,
                                                            String... statuses) {
        Set<String> allowed = Set.of(statuses);
        return occurrences.stream()
            .filter(item -> occurrenceNo == null || Objects.equals(item.getOccurrenceNo(), occurrenceNo))
            .filter(item -> allowed.contains(item.getStatus()))
            .findFirst()
            .orElseThrow(() -> new ServiceException("当前预约实例不允许执行该操作或不存在"));
    }

    /** 组合预约的生命周期动作必须覆盖同一序号下的全部房间，避免只释放一间房。 */
    private List<RoomBookingOccurrence> selectLifecycleGroup(List<RoomBookingOccurrence> occurrences,
                                                              Integer occurrenceNo,
                                                              String... statuses) {
        Set<String> allowed = Set.of(statuses);
        Integer targetOccurrenceNo = occurrenceNo;
        if (targetOccurrenceNo == null) {
            targetOccurrenceNo = occurrences.stream()
                .filter(item -> allowed.contains(item.getStatus()))
                .map(RoomBookingOccurrence::getOccurrenceNo)
                .findFirst()
                .orElseThrow(() -> new ServiceException("当前预约实例不允许执行该操作或不存在"));
        }
        Integer selectedNo = targetOccurrenceNo;
        List<RoomBookingOccurrence> group = occurrences.stream()
            .filter(item -> Objects.equals(item.getOccurrenceNo(), selectedNo))
            .toList();
        if (group.isEmpty() || group.stream().anyMatch(item -> !allowed.contains(item.getStatus()))) {
            throw new ServiceException("当前预约实例不允许执行该操作或不存在");
        }
        return group;
    }

    private void ensureLifecycleActor(RoomBooking entity, boolean attendeeAllowed) {
        Long userId = LoginHelper.getUserId();
        if (canManageAll() || Objects.equals(entity.getOrganizerId(), userId)) {
            return;
        }
        if (attendeeAllowed && isAttendee(entity.getId(), userId)) {
            return;
        }
        throw new ServiceException("只有预约组织人、参与者或房间管理员可以执行该操作");
    }

    private boolean isAttendee(Long bookingId, Long userId) {
        return userId != null && attendeeMapper.selectByBookingId(bookingId).stream()
            .anyMatch(item -> Objects.equals(item.getUserId(), userId));
    }

    private boolean canCheckIn(RoomBookingOccurrence occurrence, boolean actor) {
        if (!actor || occurrence == null || !STATUS_CONFIRMED.equals(occurrence.getStatus())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(occurrence.getStartAt().minusMinutes(CHECK_IN_GRACE_MINUTES))
            && now.isBefore(occurrence.getEndAt());
    }

    private boolean canCheckIn(RoomBookingVo item, boolean actor) {
        if (!actor || item == null || !STATUS_CONFIRMED.equals(item.getStatus())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(item.getStartAt().minusMinutes(CHECK_IN_GRACE_MINUTES))
            && now.isBefore(item.getEndAt());
    }

    private boolean canCheckOut(RoomBookingOccurrence occurrence, boolean actor) {
        return actor && occurrence != null && STATUS_IN_USE.equals(occurrence.getStatus());
    }

    private boolean canCheckOut(RoomBookingVo item, boolean actor) {
        return actor && item != null && STATUS_IN_USE.equals(item.getStatus());
    }

    private boolean canRelease(RoomBookingOccurrence occurrence, boolean actor) {
        return actor && occurrence != null && (STATUS_CONFIRMED.equals(occurrence.getStatus()) || STATUS_IN_USE.equals(occurrence.getStatus()));
    }

    private boolean canRelease(RoomBookingVo item, boolean actor) {
        return actor && item != null && (STATUS_CONFIRMED.equals(item.getStatus()) || STATUS_IN_USE.equals(item.getStatus()));
    }

    private boolean canExtend(RoomBookingOccurrence occurrence, boolean actor) {
        return actor && occurrence != null && STATUS_IN_USE.equals(occurrence.getStatus());
    }

    private boolean canExtend(RoomBookingVo item, boolean actor) {
        return actor && item != null && STATUS_IN_USE.equals(item.getStatus());
    }

    private void ensureApprovalPermission(List<RoomBookingOccurrence> occurrences) {
        Long userId = LoginHelper.getUserId();
        Long deptId = departmentAccessService.currentDeptId();
        for (RoomBookingOccurrence occurrence : occurrences) {
            if (!STATUS_PENDING.equals(occurrence.getStatus())) {
                continue;
            }
            List<RoomApprovalRule> rules = approvalRuleMapper.selectEnabledByRoomId(occurrence.getRoomId());
            if (!rules.isEmpty()) {
                RoomApprovalRule current = currentApprovalRule(occurrence, rules);
                if (current == null || !isApprovalRuleMember(current, userId, deptId)) {
                    throw new ServiceException("您不是当前审批级别的审批人");
                }
                continue;
            }
            long configured = roomResourceAclMapper.countByRoomAndPermission(occurrence.getRoomId(), PERMISSION_APPROVE);
            if (!canApproveAll()
                && (configured == 0 || roomResourceAclMapper.countGranted(occurrence.getRoomId(), PERMISSION_APPROVE, userId, deptId) == 0)) {
                throw new ServiceException("您没有审批该房间预约的权限");
            }
        }
    }

    private boolean requiresApproval(RoomResource room) {
        return room != null && (APPROVAL_REQUIRED.equals(room.getApprovalMode())
            || !approvalRuleMapper.selectEnabledByRoomId(room.getId()).isEmpty());
    }

    /** 校验用户在每个配额周期内的累计次数和分钟数，返回是否需要转人工审批。 */
    private boolean validateBookingQuota(List<Long> roomIds,
                                         List<RoomBookingOccurrence> occurrences,
                                         Long excludeBookingId) {
        Long userId = LoginHelper.getUserId();
        Long deptId = departmentAccessService.currentDeptId();
        if (userId == null || roomIds == null || occurrences == null || occurrences.isEmpty()) {
            return false;
        }
        Map<String, QuotaBucket> buckets = new LinkedHashMap<>();
        Map<String, long[]> requested = new LinkedHashMap<>();
        for (RoomBookingOccurrence occurrence : occurrences) {
            for (Long roomId : roomIds) {
                List<RoomQuotaPolicy> policies = selectEffectiveQuotaPolicies(roomId, userId, deptId);
                for (RoomQuotaPolicy policy : policies) {
                    LocalDateTime periodStart = quotaPeriodStart(occurrence.getStartAt(), policy.getPeriodType());
                    LocalDateTime periodEnd = quotaPeriodEnd(periodStart, policy.getPeriodType());
                    String key = roomId + ":" + policy.getId() + ":" + periodStart;
                    buckets.putIfAbsent(key, new QuotaBucket(policy, roomId, periodStart, periodEnd));
                    LocalDateTime clippedStart = occurrence.getStartAt().isAfter(periodStart) ? occurrence.getStartAt() : periodStart;
                    LocalDateTime clippedEnd = occurrence.getEndAt().isBefore(periodEnd) ? occurrence.getEndAt() : periodEnd;
                    long minutes = Math.max(0, java.time.Duration.between(clippedStart, clippedEnd).toMinutes());
                    long[] values = requested.computeIfAbsent(key, ignored -> new long[2]);
                    values[0] += minutes;
                    values[1]++;
                }
            }
        }
        boolean needsApproval = false;
        for (Map.Entry<String, QuotaBucket> entry : buckets.entrySet()) {
            QuotaBucket bucket = entry.getValue();
            RoomQuotaUsageVo usage = quotaPolicyMapper.selectUsage(bucket.roomId(), userId,
                bucket.periodStart(), bucket.periodEnd(), excludeBookingId);
            long[] values = requested.get(entry.getKey());
            long usedMinutes = usage == null || usage.getUsedMinutes() == null ? 0 : usage.getUsedMinutes();
            long usedCount = usage == null || usage.getUsedCount() == null ? 0 : usage.getUsedCount();
            int quotaMinutes = bucket.policy().getQuotaMinutes() == null ? 0 : bucket.policy().getQuotaMinutes();
            int quotaCount = bucket.policy().getQuotaCount() == null ? 0 : bucket.policy().getQuotaCount();
            boolean overMinutes = quotaMinutes > 0 && usedMinutes + values[0] > quotaMinutes;
            boolean overCount = quotaCount > 0 && usedCount + values[1] > quotaCount;
            if (!overMinutes && !overCount) {
                continue;
            }
            if ("APPROVAL".equalsIgnoreCase(bucket.policy().getOverQuotaAction())) {
                needsApproval = true;
                continue;
            }
            throw new ServiceException("已超过房间配额限制：" + quotaDescription(bucket.policy(), overMinutes, overCount));
        }
        return needsApproval;
    }

    private List<RoomQuotaPolicy> selectEffectiveQuotaPolicies(Long roomId, Long userId, Long deptId) {
        List<RoomQuotaPolicy> policies = quotaPolicyMapper.selectEnabledByRoomId(roomId);
        Map<String, RoomQuotaPolicy> effective = new LinkedHashMap<>();
        for (RoomQuotaPolicy policy : policies) {
            if (!isQuotaPolicyMember(policy, userId, deptId)) {
                continue;
            }
            RoomQuotaPolicy previous = effective.get(policy.getPeriodType());
            if (previous == null || quotaSubjectPriority(policy.getSubjectType()) < quotaSubjectPriority(previous.getSubjectType())) {
                effective.put(policy.getPeriodType(), policy);
            }
        }
        return new ArrayList<>(effective.values());
    }

    private boolean isQuotaPolicyMember(RoomQuotaPolicy policy, Long userId, Long deptId) {
        return switch (policy.getSubjectType()) {
            case SUBJECT_ALL -> true;
            case SUBJECT_USER -> Objects.equals(policy.getSubjectId(), userId);
            case SUBJECT_DEPT -> Objects.equals(policy.getSubjectId(), deptId);
            case SUBJECT_ROLE -> userId != null && policy.getSubjectId() != null
                && quotaPolicyMapper.countRoleMember(policy.getSubjectId(), userId) > 0;
            default -> false;
        };
    }

    private int quotaSubjectPriority(String subjectType) {
        return switch (subjectType) {
            case SUBJECT_USER -> 1;
            case SUBJECT_DEPT -> 2;
            case SUBJECT_ROLE -> 3;
            default -> 4;
        };
    }

    private LocalDateTime quotaPeriodStart(LocalDateTime value, String periodType) {
        LocalDate date = value.toLocalDate();
        String normalizedPeriodType = normalizeQuotaPeriodType(periodType);
        if ("WEEK".equals(normalizedPeriodType)) {
            date = date.minusDays(date.getDayOfWeek().getValue() - 1L);
        } else if ("MONTH".equals(normalizedPeriodType)) {
            date = date.withDayOfMonth(1);
        }
        return date.atStartOfDay();
    }

    private LocalDateTime quotaPeriodEnd(LocalDateTime periodStart, String periodType) {
        return switch (normalizeQuotaPeriodType(periodType)) {
            case "WEEK" -> periodStart.plusWeeks(1);
            case "MONTH" -> periodStart.plusMonths(1);
            default -> periodStart.plusDays(1);
        };
    }

    private String quotaDescription(RoomQuotaPolicy policy, boolean overMinutes, boolean overCount) {
        List<String> descriptions = new ArrayList<>();
        if (overMinutes) {
            descriptions.add(policy.getQuotaMinutes() + "分钟");
        }
        if (overCount) {
            descriptions.add(policy.getQuotaCount() + "次");
        }
        return ("DAY".equalsIgnoreCase(policy.getPeriodType()) ? "每日" : "WEEK".equalsIgnoreCase(policy.getPeriodType()) ? "每周" : "每月")
            + String.join("、", descriptions);
    }

    /** 记录配额生命周期流水；实时配额仍由预约实例状态计算，流水不会参与二次扣减。 */
    private void recordQuotaUsage(List<RoomBookingOccurrence> occurrences,
                                  Long userId,
                                  Long deptId,
                                  String action,
                                  String reason) {
        if (occurrences == null || occurrences.isEmpty()) {
            return;
        }
        for (RoomBookingOccurrence occurrence : occurrences) {
            if (occurrence == null || occurrence.getRoomId() == null || occurrence.getStartAt() == null || occurrence.getEndAt() == null) {
                continue;
            }
            int minutes = (int) Math.max(0, java.time.Duration.between(occurrence.getStartAt(), occurrence.getEndAt()).toMinutes());
            RoomQuotaUsage usage = new RoomQuotaUsage();
            usage.setRoomId(occurrence.getRoomId());
            usage.setBookingId(occurrence.getBookingId());
            usage.setOccurrenceNo(occurrence.getOccurrenceNo());
            usage.setUserId(userId);
            usage.setAction(action);
            usage.setMinutes(minutes);
            usage.setBookingCount(1);
            usage.setAmount(calculateQuotaAmount(occurrence.getRoomId(), userId, deptId, minutes, action));
            usage.setReason(StringUtils.trim(reason));
            quotaUsageMapper.insert(usage);
        }
    }

    private BigDecimal calculateQuotaAmount(Long roomId, Long userId, Long deptId, int minutes, String action) {
        RoomQuotaPolicy policy = RoomQuotaBillingEvaluator.selectBillingPolicy(
            selectEffectiveQuotaPolicies(roomId, userId, deptId));
        return RoomQuotaBillingEvaluator.calculate(policy, minutes, action);
    }

    private void initializeApproval(RoomBookingOccurrence occurrence, Long roomId) {
        List<RoomApprovalRule> rules = approvalRuleMapper.selectEnabledByRoomId(roomId);
        if (rules.isEmpty()) {
            return;
        }
        RoomApprovalRule first = rules.get(0);
        occurrence.setApprovalStep(first.getStepNo());
        occurrence.setApprovalDueAt(approvalDueAt(LocalDateTime.now(), first));
    }

    private RoomApprovalRule currentApprovalRule(RoomBookingOccurrence occurrence) {
        return currentApprovalRule(occurrence, approvalRuleMapper.selectEnabledByRoomId(occurrence.getRoomId()));
    }

    private RoomApprovalRule currentApprovalRule(RoomBookingOccurrence occurrence, List<RoomApprovalRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        int stepNo = occurrence.getApprovalStep() == null ? rules.get(0).getStepNo() : occurrence.getApprovalStep();
        return rules.stream().filter(item -> Objects.equals(item.getStepNo(), stepNo)).findFirst().orElse(null);
    }

    private RoomApprovalRule nextApprovalRule(RoomBookingOccurrence occurrence) {
        List<RoomApprovalRule> rules = approvalRuleMapper.selectEnabledByRoomId(occurrence.getRoomId());
        RoomApprovalRule current = currentApprovalRule(occurrence, rules);
        if (current == null) {
            return null;
        }
        return rules.stream().filter(item -> item.getStepNo() > current.getStepNo()).findFirst().orElse(null);
    }

    private int currentStep(RoomBookingOccurrence occurrence) {
        RoomApprovalRule current = currentApprovalRule(occurrence);
        return current == null ? (occurrence.getApprovalStep() == null ? 1 : occurrence.getApprovalStep()) : current.getStepNo();
    }

    private LocalDateTime approvalDueAt(LocalDateTime from, RoomApprovalRule rule) {
        int timeout = rule.getTimeoutMinutes() == null ? DEFAULT_APPROVAL_TIMEOUT_MINUTES : rule.getTimeoutMinutes();
        return timeout <= 0 ? null : from.plusMinutes(timeout);
    }

    private boolean isApprovalRuleMember(RoomApprovalRule rule, Long userId, Long deptId) {
        if (rule == null) {
            // A pending occurrence whose configured step no longer exists must not
            // become approvable by falling through to a permissive default.
            return false;
        }
        boolean roleMember = userId != null && rule.getApproverId() != null
            && SUBJECT_ROLE.equalsIgnoreCase(rule.getApproverType())
            && approvalRuleMapper.countRoleMember(rule.getApproverId(), userId) > 0;
        return RoomBookingApprovalEvaluator.isMember(
            rule.getApproverType(), rule.getApproverId(), userId, deptId, roleMember, canApproveAll()
        );
    }

    private boolean canApproveAll() {
        return canManageAll() || StpUtil.hasPermission("department:room:approve");
    }

    private boolean canApprove(RoomBookingVo item) {
        if (item == null || !STATUS_PENDING.equals(item.getStatus())) {
            return false;
        }
        RoomBookingOccurrence occurrence = new RoomBookingOccurrence();
        occurrence.setRoomId(item.getRoomId());
        occurrence.setApprovalStep(item.getApprovalStep());
        List<RoomApprovalRule> rules = approvalRuleMapper.selectEnabledByRoomId(item.getRoomId());
        if (!rules.isEmpty()) {
            return isApprovalRuleMember(currentApprovalRule(occurrence, rules), LoginHelper.getUserId(), departmentAccessService.currentDeptId());
        }
        return canManageAll() || StpUtil.hasPermission("department:room:approve")
            || roomResourceAclMapper.countGranted(item.getRoomId(), PERMISSION_APPROVE,
            LoginHelper.getUserId(), departmentAccessService.currentDeptId()) > 0;
    }

    private boolean canAccessRoom(RoomResource room, String permissionType) {
        if (canManageAll() || (PERMISSION_APPROVE.equals(permissionType)
            && StpUtil.hasPermission("department:room:approve"))) {
            return true;
        }
        Long deptId = departmentAccessService.currentDeptId();
        long configured = roomResourceAclMapper.countByRoomAndPermission(room.getId(), permissionType);
        boolean granted = configured > 0 && roomResourceAclMapper.countGranted(room.getId(), permissionType, LoginHelper.getUserId(), deptId) > 0;
        return RoomBookingPermissionEvaluator.canAccess(room.getScopeType(), room.getDeptId(), deptId, configured > 0, granted);
    }

    private String normalizeAclSubjectType(String subjectType) {
        String value = subjectType == null ? SUBJECT_USER : subjectType.toUpperCase();
        return switch (value) {
            case SUBJECT_DEPT -> SUBJECT_DEPT;
            case SUBJECT_ROLE -> SUBJECT_ROLE;
            case SUBJECT_ALL -> SUBJECT_ALL;
            default -> SUBJECT_USER;
        };
    }

    private String normalizeApprovalApproverType(String approverType) {
        String value = approverType == null ? SUBJECT_USER : approverType.toUpperCase();
        return switch (value) {
            case SUBJECT_DEPT -> SUBJECT_DEPT;
            case SUBJECT_ROLE -> SUBJECT_ROLE;
            case SUBJECT_ALL -> SUBJECT_ALL;
            default -> SUBJECT_USER;
        };
    }

    private String normalizeQuotaSubjectType(String subjectType) {
        String value = subjectType == null ? SUBJECT_USER : subjectType.toUpperCase();
        return switch (value) {
            case SUBJECT_DEPT -> SUBJECT_DEPT;
            case SUBJECT_ROLE -> SUBJECT_ROLE;
            case SUBJECT_ALL -> SUBJECT_ALL;
            default -> SUBJECT_USER;
        };
    }

    private String normalizeQuotaPeriodType(String periodType) {
        return switch (periodType == null ? "DAY" : periodType.trim().toUpperCase(Locale.ROOT)) {
            case "WEEK" -> "WEEK";
            case "MONTH" -> "MONTH";
            default -> "DAY";
        };
    }

    private String normalizeQuotaAction(String action) {
        return "APPROVAL".equalsIgnoreCase(action) ? "APPROVAL" : "BLOCK";
    }

    private String normalizeAclPermissionType(String permissionType) {
        String value = permissionType == null ? PERMISSION_BOOK : permissionType.toUpperCase();
        return switch (value) {
            case PERMISSION_VIEW -> PERMISSION_VIEW;
            case PERMISSION_APPROVE -> PERMISSION_APPROVE;
            default -> PERMISSION_BOOK;
        };
    }

    private void requireRoomManage() {
        if (!StpUtil.hasPermission("department:room:manage") && !canManageAll()) {
            throw new ServiceException("您没有房间管理权限");
        }
    }

    private void requireRoomApproval() {
        if (!StpUtil.hasPermission("department:room:approve") && !canManageAll()) {
            throw new ServiceException("您没有房间预约审批权限");
        }
    }

    private Long requireDepartment(String action) {
        Long deptId = departmentAccessService.currentDeptId();
        if (deptId == null) {
            throw new ServiceException("当前登录用户缺少业务科室，无法" + action);
        }
        return deptId;
    }

    private boolean canManageAll() {
        return LoginHelper.isSuperAdmin() || StpUtil.hasPermission("department:room:manageAll");
    }

    private RoomResourceVo toRoomVo(RoomResource entity) {
        RoomResourceVo vo = new RoomResourceVo();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setRoomCode(entity.getRoomCode());
        vo.setRoomName(entity.getRoomName());
        vo.setRoomType(entity.getRoomType());
        vo.setLocation(entity.getLocation());
        vo.setFloorPlanId(entity.getFloorPlanId());
        vo.setBuildingName(entity.getBuildingName());
        vo.setFloorName(entity.getFloorName());
        vo.setAreaName(entity.getAreaName());
        vo.setMergeGroup(entity.getMergeGroup());
        vo.setDisplayImage(entity.getDisplayImage());
        vo.setPhotoUrls(entity.getPhotoUrls());
        vo.setUsageGuide(entity.getUsageGuide());
        vo.setManagerUserId(entity.getManagerUserId());
        vo.setManagerName(entity.getManagerName());
        vo.setDisplayScreen(entity.getDisplayScreen());
        vo.setCapacity(entity.getCapacity());
        vo.setAmenities(entity.getAmenities());
        List<Long> amenityIds = entity.getAmenityIds() == null
            ? amenityRelationMapper.selectAmenityIds(entity.getId()) : entity.getAmenityIds();
        vo.setAmenityIds(amenityIds);
        vo.setAmenityNames(amenityRelationMapper.selectAmenityNames(entity.getId()));
        vo.setStatus(entity.getStatus());
        vo.setScopeType(entity.getScopeType());
        vo.setOpenTime(entity.getOpenTime());
        vo.setCloseTime(entity.getCloseTime());
        vo.setAllowWeekend(entity.getAllowWeekend());
        vo.setAllowCrossDay(entity.getAllowCrossDay());
        vo.setApprovalMode(entity.getApprovalMode());
        vo.setMaxAdvanceDays(entity.getMaxAdvanceDays());
        vo.setMaxDurationMinutes(entity.getMaxDurationMinutes());
        vo.setAllowRecurring(entity.getAllowRecurring());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private RoomFloorPlanVo toFloorPlanVo(RoomFloorPlan entity) {
        RoomFloorPlanVo vo = new RoomFloorPlanVo();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setPlanName(entity.getPlanName());
        vo.setBuildingName(entity.getBuildingName());
        vo.setFloorName(entity.getFloorName());
        vo.setFloorNo(entity.getFloorNo());
        vo.setMapImage(entity.getMapImage());
        vo.setMapData(entity.getMapData());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private void sanitizeFloorPlanMapData(Collection<RoomFloorPlanVo> plans) {
        if (plans == null || plans.isEmpty() || canManageAll()) {
            return;
        }
        boolean canManage = StpUtil.hasPermission("department:room:manage");
        Set<String> accessibleRoomIds = roomResourceMapper.selectViewOptions(
                departmentAccessService.currentDeptId(), LoginHelper.getUserId(), false, canManage
            ).stream()
            .map(RoomResourceVo::getId)
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .collect(Collectors.toSet());
        plans.forEach(plan -> {
            String originalMapData = plan.getMapData();
            if (StringUtils.isNotBlank(plan.getMapImage())
                && RoomFloorPlanMapDataSanitizer.hasInaccessibleRooms(originalMapData, accessibleRoomIds)) {
                plan.setMapImage(null);
            }
            plan.setMapData(RoomFloorPlanMapDataSanitizer.sanitize(originalMapData, accessibleRoomIds));
        });
    }

    private void addConflict(RoomConflictResultVo result,
                             Long roomId,
                             RoomResource room,
                             RoomBookingOccurrence occurrence,
                             String reason) {
        addConflict(result, roomId, room, occurrence.getStartAt(), occurrence.getEndAt(), reason);
    }

    private void addConflict(RoomConflictResultVo result,
                             Long roomId,
                             RoomResource room,
                             LocalDateTime startAt,
                             LocalDateTime endAt,
                             String reason) {
        RoomConflictVo conflict = new RoomConflictVo();
        conflict.setRoomId(roomId);
        conflict.setRoomName(room == null ? null : room.getRoomName());
        conflict.setStartAt(startAt);
        conflict.setEndAt(endAt);
        conflict.setReason(reason);
        result.getConflicts().add(conflict);
    }

    private List<Long> normalizeRoomIds(Collection<Long> roomIds) {
        if (roomIds == null) {
            return List.of();
        }
        return roomIds.stream().filter(Objects::nonNull).collect(Collectors.collectingAndThen(
            Collectors.toCollection(LinkedHashSet::new), ArrayList::new
        ));
    }

    private String normalizeRoomStatus(String status) {
        String value = status == null ? ROOM_ENABLED : status.toUpperCase();
        return switch (value) {
            case ROOM_MAINTENANCE -> ROOM_MAINTENANCE;
            case ROOM_DISABLED -> ROOM_DISABLED;
            default -> ROOM_ENABLED;
        };
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private void copyCalendarExceptionBo(RoomCalendarExceptionBo bo, RoomCalendarException entity) {
        if (bo == null || bo.getExceptionDate() == null) {
            throw new ServiceException("例外日期不能为空");
        }
        entity.setExceptionDate(bo.getExceptionDate());
        entity.setExceptionType(EXCEPTION_WORKDAY.equalsIgnoreCase(bo.getExceptionType()) ? EXCEPTION_WORKDAY : EXCEPTION_HOLIDAY);
        entity.setExceptionName(StringUtils.isBlank(bo.getExceptionName())
            ? (EXCEPTION_WORKDAY.equalsIgnoreCase(entity.getExceptionType()) ? "补班" : "节假日")
            : StringUtils.trim(bo.getExceptionName()));
        entity.setRemark(StringUtils.trim(bo.getRemark()));
    }

    private void checkCalendarExceptionDuplicate(RoomCalendarException entity) {
        RoomCalendarException duplicate = calendarExceptionMapper.selectOne(Wrappers.<RoomCalendarException>lambdaQuery()
            .eq(RoomCalendarException::getDeptId, entity.getDeptId())
            .eq(RoomCalendarException::getExceptionDate, entity.getExceptionDate())
            .ne(entity.getId() != null, RoomCalendarException::getId, entity.getId()));
        if (duplicate != null) {
            throw new ServiceException("该日期已经配置日历例外");
        }
    }

    private String normalizeReason(String reason, String fallback) {
        return StringUtils.isBlank(reason) ? fallback : StringUtils.trim(reason);
    }

    private BigDecimal rate(Long numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
            .divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
