package org.dromara.department.controller;

import com.baomidou.lock.annotation.Lock4j;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.RoomBookingBo;
import org.dromara.department.domain.bo.RoomBookingQueryBo;
import org.dromara.department.domain.bo.RoomCalendarExceptionBo;
import org.dromara.department.domain.bo.RoomCalendarExceptionQueryBo;
import org.dromara.department.domain.bo.RoomResourceBo;
import org.dromara.department.domain.bo.RoomResourceQueryBo;
import org.dromara.department.domain.bo.RoomResourceAclBo;
import org.dromara.department.domain.bo.RoomBookingExtendBo;
import org.dromara.department.domain.bo.RoomBookingBlockBo;
import org.dromara.department.domain.bo.RoomFloorPlanBo;
import org.dromara.department.domain.bo.RoomFloorPlanQueryBo;
import org.dromara.department.domain.bo.RoomAnalyticsQueryBo;
import org.dromara.department.domain.bo.RoomApprovalRuleBo;
import org.dromara.department.domain.bo.RoomQuotaPolicyBo;
import org.dromara.department.domain.bo.RoomAmenityBo;
import org.dromara.department.domain.bo.RoomBookingBlacklistBo;
import org.dromara.department.domain.bo.RoomResourceMergeBo;
import org.dromara.department.domain.vo.RoomBookingDetailVo;
import org.dromara.department.domain.vo.RoomBookingVo;
import org.dromara.department.domain.vo.RoomCalendarExceptionVo;
import org.dromara.department.domain.vo.RoomConflictResultVo;
import org.dromara.department.domain.vo.RoomResourceVo;
import org.dromara.department.domain.vo.RoomUserOptionVo;
import org.dromara.department.domain.vo.RoomResourceAclVo;
import org.dromara.department.domain.vo.RoomBookingBlockVo;
import org.dromara.department.domain.vo.RoomFloorPlanVo;
import org.dromara.department.domain.vo.RoomFloorPlanVersionVo;
import org.dromara.department.domain.vo.RoomAnalyticsVo;
import org.dromara.department.domain.vo.RoomApprovalRuleVo;
import org.dromara.department.domain.vo.RoomQuotaPolicyVo;
import org.dromara.department.domain.vo.RoomAmenityVo;
import org.dromara.department.domain.vo.RoomBookingBlacklistVo;
import org.dromara.department.domain.vo.RoomBookingRecurrenceExceptionVo;
import org.dromara.department.domain.vo.RoomQuotaUsageRecordVo;
import org.dromara.department.service.IRoomBookingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** 房间资源、日历和预约接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/room")
public class RoomBookingController extends BaseController {

    private final IRoomBookingService roomBookingService;

    /** 查询房间主数据。 */
    @SaCheckPermission("department:room:list")
    @GetMapping("/resource/list")
    public R<PageResult<RoomResourceVo>> resourceList(RoomResourceQueryBo bo, PageQuery pageQuery) {
        return R.ok(roomBookingService.queryRoomPage(bo, pageQuery));
    }

    /** 查询当前用户可预约的房间。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/resource/options")
    public R<List<RoomResourceVo>> resourceOptions() {
        return R.ok(roomBookingService.queryRoomOptions());
    }

    /** 查询当前用户可查看的房间，用于平面图展示。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/resource/view-options")
    public R<List<RoomResourceVo>> resourceViewOptions() {
        return R.ok(roomBookingService.queryRoomViewOptions());
    }

    /** 查询房间详情。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/resource/{id}")
    public R<RoomResourceVo> resourceInfo(@NotNull(message = "房间主键不能为空") @PathVariable Long id) {
        return R.ok(roomBookingService.queryRoomById(id));
    }

    /** 新增房间。 */
    @SaCheckPermission(value = {"department:room:add", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间资源", businessType = BusinessType.INSERT)
    @PostMapping("/resource")
    public R<Void> addResource(@Validated(AddGroup.class) @RequestBody RoomResourceBo bo) {
        return toAjax(roomBookingService.insertRoom(bo));
    }

    /** 修改房间。 */
    @SaCheckPermission(value = {"department:room:edit", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间资源", businessType = BusinessType.UPDATE)
    @PutMapping("/resource")
    public R<Void> editResource(@Validated(EditGroup.class) @RequestBody RoomResourceBo bo) {
        return toAjax(roomBookingService.updateRoom(bo));
    }

    /** 删除没有历史预约的房间。已有记录的房间应改为停用。 */
    @SaCheckPermission(value = {"department:room:remove", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/resource/{ids}")
    public R<Void> removeResource(@NotEmpty(message = "房间主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomBookingService.deleteRooms(Arrays.asList(ids)));
    }

    /** 查询参与者选项。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/users/options")
    public R<List<RoomUserOptionVo>> userOptions() {
        return R.ok(roomBookingService.queryUserOptions());
    }

    /** 查询房间级授权。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/resource/{roomId}/acl")
    public R<List<RoomResourceAclVo>> resourceAclList(@NotNull(message = "房间主键不能为空") @PathVariable Long roomId) {
        return R.ok(roomBookingService.queryRoomAcls(roomId));
    }

    /** 新增或修改房间级授权。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间访问授权", businessType = BusinessType.INSERT)
    @PostMapping("/resource/acl")
    public R<Void> saveResourceAcl(@Validated @RequestBody RoomResourceAclBo bo) {
        return toAjax(roomBookingService.saveRoomAcl(bo));
    }

    /** 删除房间级授权。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间访问授权", businessType = BusinessType.DELETE)
    @DeleteMapping("/resource/acl/{ids}")
    public R<Void> removeResourceAcl(@NotEmpty(message = "授权主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomBookingService.deleteRoomAcls(Arrays.asList(ids)));
    }

    /** 查询房间预约黑名单。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/resource/{roomId}/blacklist")
    public R<List<RoomBookingBlacklistVo>> resourceBlacklistList(@NotNull(message = "房间主键不能为空") @PathVariable Long roomId) {
        return R.ok(roomBookingService.queryRoomBlacklists(roomId));
    }

    /** 新增或修改房间预约黑名单。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约黑名单", businessType = BusinessType.INSERT)
    @PostMapping("/resource/blacklist")
    public R<Void> saveResourceBlacklist(@Validated @RequestBody RoomBookingBlacklistBo bo) {
        return toAjax(roomBookingService.saveRoomBlacklist(bo));
    }

    /** 删除房间预约黑名单。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约黑名单", businessType = BusinessType.DELETE)
    @DeleteMapping("/resource/blacklist/{ids}")
    public R<Void> removeResourceBlacklist(@NotEmpty(message = "黑名单主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomBookingService.deleteRoomBlacklists(Arrays.asList(ids)));
    }

    /** 查询房间封锁时段。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/resource/{roomId}/blocks")
    public R<List<RoomBookingBlockVo>> resourceBlockList(@NotNull(message = "房间主键不能为空") @PathVariable Long roomId) {
        return R.ok(roomBookingService.queryRoomBlocks(roomId));
    }

    /** 新增或修改房间封锁时段。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间封锁时段", businessType = BusinessType.INSERT)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/resource/block")
    public R<Void> saveResourceBlock(@Validated @RequestBody RoomBookingBlockBo bo) {
        return toAjax(roomBookingService.saveRoomBlock(bo));
    }

    /** 删除房间封锁时段。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间封锁时段", businessType = BusinessType.DELETE)
    @DeleteMapping("/resource/block/{ids}")
    public R<Void> removeResourceBlock(@NotEmpty(message = "封锁记录主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomBookingService.deleteRoomBlocks(Arrays.asList(ids)));
    }

    /** 查询房间的多级审批规则。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/resource/{roomId}/approval-rules")
    public R<List<RoomApprovalRuleVo>> approvalRuleList(@NotNull(message = "房间主键不能为空") @PathVariable Long roomId) {
        return R.ok(roomBookingService.queryApprovalRules(roomId));
    }

    /** 新增或修改房间的多级审批规则。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约审批规则", businessType = BusinessType.INSERT)
    @PostMapping("/resource/approval-rule")
    public R<Void> saveApprovalRule(@Validated @RequestBody RoomApprovalRuleBo bo) {
        return toAjax(roomBookingService.saveApprovalRule(bo));
    }

    /** 删除房间的多级审批规则。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约审批规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/resource/approval-rule/{ids}")
    public R<Void> removeApprovalRule(@NotEmpty(message = "审批规则主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomBookingService.deleteApprovalRules(Arrays.asList(ids)));
    }

    /** 查询房间配额策略。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/resource/{roomId}/quota-policies")
    public R<List<RoomQuotaPolicyVo>> quotaPolicyList(@NotNull(message = "房间主键不能为空") @PathVariable Long roomId) {
        return R.ok(roomBookingService.queryQuotaPolicies(roomId));
    }

    /** 新增或修改房间配额策略。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约配额", businessType = BusinessType.INSERT)
    @PostMapping("/resource/quota-policy")
    public R<Void> saveQuotaPolicy(@Validated @RequestBody RoomQuotaPolicyBo bo) {
        return toAjax(roomBookingService.saveQuotaPolicy(bo));
    }

    /** 删除房间配额策略。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约配额", businessType = BusinessType.DELETE)
    @DeleteMapping("/resource/quota-policy/{ids}")
    public R<Void> removeQuotaPolicy(@NotEmpty(message = "配额策略主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomBookingService.deleteQuotaPolicies(Arrays.asList(ids)));
    }

    /** 查询房间设施标准目录。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/amenity/list")
    public R<PageResult<RoomAmenityVo>> amenityList(PageQuery pageQuery) {
        return R.ok(roomBookingService.queryAmenityPage(pageQuery));
    }

    /** 查询当前用户可使用的房间设施选项。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/amenity/options")
    public R<List<RoomAmenityVo>> amenityOptions() {
        return R.ok(roomBookingService.queryAmenityOptions());
    }

    /** 新增房间设施。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间设施", businessType = BusinessType.INSERT)
    @PostMapping("/amenity")
    public R<Void> addAmenity(@Validated(AddGroup.class) @RequestBody RoomAmenityBo bo) {
        return toAjax(roomBookingService.insertAmenity(bo));
    }

    /** 修改房间设施。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间设施", businessType = BusinessType.UPDATE)
    @PutMapping("/amenity")
    public R<Void> editAmenity(@Validated(EditGroup.class) @RequestBody RoomAmenityBo bo) {
        return toAjax(roomBookingService.updateAmenity(bo));
    }

    /** 删除未绑定房间的设施。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间设施", businessType = BusinessType.DELETE)
    @DeleteMapping("/amenity/{ids}")
    public R<Void> removeAmenity(@NotEmpty(message = "设施主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomBookingService.deleteAmenities(Arrays.asList(ids)));
    }

    /** 查询楼层平面图。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/floor-plan/list")
    public R<PageResult<RoomFloorPlanVo>> floorPlanList(RoomFloorPlanQueryBo bo, PageQuery pageQuery) {
        return R.ok(roomBookingService.queryFloorPlanPage(bo, pageQuery));
    }

    /** 查询当前用户可查看的平面图选项。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/floor-plan/options")
    public R<List<RoomFloorPlanVo>> floorPlanOptions() {
        return R.ok(roomBookingService.queryFloorPlanOptions());
    }

    /** 查询平面图详情。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/floor-plan/{id}")
    public R<RoomFloorPlanVo> floorPlanInfo(@NotNull(message = "平面图主键不能为空") @PathVariable Long id) {
        return R.ok(roomBookingService.queryFloorPlanById(id));
    }

    /** 新增平面图。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间平面图", businessType = BusinessType.INSERT)
    @PostMapping("/floor-plan")
    public R<Void> addFloorPlan(@Validated(AddGroup.class) @RequestBody RoomFloorPlanBo bo) {
        return toAjax(roomBookingService.insertFloorPlan(bo));
    }

    /** 修改平面图。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间平面图", businessType = BusinessType.UPDATE)
    @PutMapping("/floor-plan")
    public R<Void> editFloorPlan(@Validated(EditGroup.class) @RequestBody RoomFloorPlanBo bo) {
        return toAjax(roomBookingService.updateFloorPlan(bo));
    }

    /** 删除未绑定房间的平面图。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间平面图", businessType = BusinessType.DELETE)
    @DeleteMapping("/floor-plan/{ids}")
    public R<Void> removeFloorPlan(@NotEmpty(message = "平面图主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomBookingService.deleteFloorPlans(Arrays.asList(ids)));
    }

    /** 查询平面图历史版本。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/floor-plan/{id}/versions")
    public R<List<RoomFloorPlanVersionVo>> floorPlanVersions(@NotNull(message = "平面图主键不能为空") @PathVariable Long id) {
        return R.ok(roomBookingService.queryFloorPlanVersions(id));
    }

    /** 发布平面图，使其对普通用户可见。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间平面图", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:floor-plan", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/floor-plan/{id}/publish")
    public R<Void> publishFloorPlan(@NotNull(message = "平面图主键不能为空") @PathVariable Long id) {
        return toAjax(roomBookingService.publishFloorPlan(id));
    }

    /** 撤回平面图发布，恢复为草稿。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间平面图", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:floor-plan", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/floor-plan/{id}/unpublish")
    public R<Void> unpublishFloorPlan(@NotNull(message = "平面图主键不能为空") @PathVariable Long id) {
        return toAjax(roomBookingService.unpublishFloorPlan(id));
    }

    /** 从历史版本恢复为草稿，发布前可继续编辑确认。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间平面图", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:floor-plan", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/floor-plan/version/{versionId}/restore")
    public R<Void> restoreFloorPlanVersion(@NotNull(message = "历史版本主键不能为空") @PathVariable Long versionId) {
        return toAjax(roomBookingService.restoreFloorPlanVersion(versionId));
    }

    /** 将房间从合并组中拆出。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间组合", businessType = BusinessType.UPDATE)
    @PostMapping("/resource/{roomId}/split-group")
    public R<Void> splitRoomGroup(@NotNull(message = "房间主键不能为空") @PathVariable Long roomId) {
        return toAjax(roomBookingService.splitRoomGroup(roomId));
    }

    /** 原子设置多个房间的组合编码，用于组合预约。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间组合", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/resource/merge-group")
    public R<Void> mergeRoomGroup(@Validated @RequestBody RoomResourceMergeBo bo) {
        return toAjax(roomBookingService.mergeRoomGroup(bo));
    }

    /** 查询房间使用率、取消率和爽约统计。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/analytics")
    public R<RoomAnalyticsVo> analytics(RoomAnalyticsQueryBo bo) {
        return R.ok(roomBookingService.queryRoomAnalytics(bo));
    }

    /** 查询营业日历例外。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/calendar-exception/list")
    public R<PageResult<RoomCalendarExceptionVo>> calendarExceptionList(RoomCalendarExceptionQueryBo bo, PageQuery pageQuery) {
        return R.ok(roomBookingService.queryCalendarExceptionPage(bo, pageQuery));
    }

    /** 新增营业日历例外。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间日历例外", businessType = BusinessType.INSERT)
    @PostMapping("/calendar-exception")
    public R<Void> addCalendarException(@Validated(AddGroup.class) @RequestBody RoomCalendarExceptionBo bo) {
        return toAjax(roomBookingService.insertCalendarException(bo));
    }

    /** 修改营业日历例外。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间日历例外", businessType = BusinessType.UPDATE)
    @PutMapping("/calendar-exception")
    public R<Void> editCalendarException(@Validated(EditGroup.class) @RequestBody RoomCalendarExceptionBo bo) {
        return toAjax(roomBookingService.updateCalendarException(bo));
    }

    /** 删除营业日历例外。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间日历例外", businessType = BusinessType.DELETE)
    @DeleteMapping("/calendar-exception/{ids}")
    public R<Void> removeCalendarException(@NotEmpty(message = "例外主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomBookingService.deleteCalendarExceptions(Arrays.asList(ids)));
    }

    /** 查询循环预约中被跳过的实例。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:approve", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/booking/{id}/recurrence-exceptions")
    public R<List<RoomBookingRecurrenceExceptionVo>> recurrenceExceptions(@NotNull(message = "预约主键不能为空") @PathVariable Long id) {
        return R.ok(roomBookingService.queryRecurrenceExceptions(id));
    }

    /** 查询房间配额消费、释放和退款流水。 */
    @SaCheckPermission(value = {"department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/resource/{roomId}/quota-usages")
    public R<List<RoomQuotaUsageRecordVo>> quotaUsages(@NotNull(message = "房间主键不能为空") @PathVariable Long roomId) {
        return R.ok(roomBookingService.queryQuotaUsage(roomId));
    }

    /** 查询指定时间范围内的日历事件。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:approve", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/calendar")
    public R<List<RoomBookingVo>> calendar(RoomBookingQueryBo bo) {
        return R.ok(roomBookingService.queryCalendar(bo));
    }

    /** 查询预约列表，供“我的预约”和管理员列表使用。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:approve", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/booking/list")
    public R<PageResult<RoomBookingVo>> bookingList(RoomBookingQueryBo bo, PageQuery pageQuery) {
        return R.ok(roomBookingService.queryBookingPage(bo, pageQuery));
    }

    /** 查询按预约实例去重后的待审批数量。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:approve", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/booking/pending-count")
    public R<Long> pendingBookingCount() {
        return R.ok(roomBookingService.queryPendingBookingCount());
    }

    /** 查询预约编辑详情。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:approve", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/booking/{id}")
    public R<RoomBookingDetailVo> bookingInfo(@NotNull(message = "预约主键不能为空") @PathVariable Long id,
                                               @RequestParam(required = false) Integer occurrenceNo) {
        return R.ok(roomBookingService.queryBookingById(id, occurrenceNo));
    }

    /** 导出单条预约为标准 iCalendar 文件，可导入 Outlook、Apple Calendar 和 Google Calendar。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:approve", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @GetMapping("/booking/{id}/ical")
    public void bookingIcal(@NotNull(message = "预约主键不能为空") @PathVariable Long id,
                            @RequestParam(required = false) Integer occurrenceNo,
                            HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/calendar;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=room-booking-" + id + ".ics");
        response.getWriter().write(roomBookingService.exportBookingIcal(id, occurrenceNo));
    }

    /** 预约提交前的冲突预检。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:book", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @PostMapping("/booking/check-conflict")
    public R<RoomConflictResultVo> checkConflict(@Validated @RequestBody RoomBookingBo bo) {
        return R.ok(roomBookingService.checkConflict(bo));
    }

    /** 创建或修改预约；短锁保证同一时刻不会出现并发重复占用。 */
    @SaCheckPermission(value = {"department:room:book", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约", businessType = BusinessType.INSERT)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/booking")
    public R<Void> saveBooking(@Validated(AddGroup.class) @RequestBody RoomBookingBo bo) {
        return toAjax(roomBookingService.saveBooking(bo));
    }

    /** 修改预约，单独保留 PUT 语义便于前端区分操作。 */
    @SaCheckPermission(value = {"department:room:book", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @PutMapping("/booking")
    public R<Void> editBooking(@Validated(EditGroup.class) @RequestBody RoomBookingBo bo) {
        return toAjax(roomBookingService.saveBooking(bo));
    }

    /** 取消预约。 */
    @SaCheckPermission(value = {"department:room:cancel", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @DeleteMapping("/booking/{ids}")
    public R<Void> cancelBooking(@NotEmpty(message = "预约主键不能为空") @PathVariable Long[] ids,
                                 @RequestParam(required = false, defaultValue = "SERIES") String operationScope,
                                 @RequestParam(required = false) Integer occurrenceNo,
                                 @RequestParam(required = false) String reason) {
        return toAjax(roomBookingService.cancelBookings(Arrays.asList(ids), operationScope, occurrenceNo, reason));
    }

    /** 审批通过预约。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:approve", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/booking/{id}/approve")
    public R<Void> approve(@NotNull(message = "预约主键不能为空") @PathVariable Long id,
                           @RequestParam(required = false) Integer occurrenceNo) {
        return toAjax(roomBookingService.approveBooking(id, occurrenceNo));
    }

    /** 驳回预约。 */
    @SaCheckPermission(value = {"department:room:query", "department:room:approve", "department:room:manage", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/booking/{id}/reject")
    public R<Void> reject(@NotNull(message = "预约主键不能为空") @PathVariable Long id,
                          @RequestParam(required = false) Integer occurrenceNo,
                          @RequestParam(required = false) String reason) {
        return toAjax(roomBookingService.rejectBooking(id, occurrenceNo, reason));
    }

    /** 预约签到，默认操作当前系列的第一条可签到实例。 */
    @SaCheckPermission(value = {"department:room:book", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/booking/{id}/check-in")
    public R<Void> checkIn(@NotNull(message = "预约主键不能为空") @PathVariable Long id,
                           @RequestParam(required = false) Integer occurrenceNo) {
        return toAjax(roomBookingService.checkInBooking(id, occurrenceNo));
    }

    /** 预约签退。 */
    @SaCheckPermission(value = {"department:room:book", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/booking/{id}/check-out")
    public R<Void> checkOut(@NotNull(message = "预约主键不能为空") @PathVariable Long id,
                            @RequestParam(required = false) Integer occurrenceNo) {
        return toAjax(roomBookingService.checkOutBooking(id, occurrenceNo));
    }

    /** 主动释放已确认或使用中的预约实例。 */
    @SaCheckPermission(value = {"department:room:book", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/booking/{id}/release")
    public R<Void> release(@NotNull(message = "预约主键不能为空") @PathVariable Long id,
                           @RequestParam(required = false) Integer occurrenceNo,
                           @RequestParam(required = false) String reason) {
        return toAjax(roomBookingService.releaseBooking(id, occurrenceNo, reason));
    }

    /** 延长使用中的预约，并重新执行冲突和房间策略校验。 */
    @SaCheckPermission(value = {"department:room:book", "department:room:manageAll"}, mode = SaMode.OR)
    @Log(title = "房间预约", businessType = BusinessType.UPDATE)
    @Lock4j(name = "department:room:booking", expire = 10000, acquireTimeout = 2000)
    @PostMapping("/booking/extend")
    public R<Void> extend(@Validated @RequestBody RoomBookingExtendBo bo) {
        return toAjax(roomBookingService.extendBooking(bo));
    }

    /** 导出指定范围内的预约记录。 */
    @SaCheckPermission("department:room:export")
    @Log(title = "房间预约", businessType = BusinessType.EXPORT)
    @PostMapping("/booking/export")
    public void export(RoomBookingQueryBo bo, HttpServletResponse response) {
        List<RoomBookingVo> list = roomBookingService.queryBookingList(bo);
        ExcelBuilder.of(list, RoomBookingVo.class).sheetName("房间预约").toResponse(response);
    }

    /** 导出统计范围内的预约明细，供分析页继续做透视或审计。 */
    @SaCheckPermission("department:room:export")
    @Log(title = "房间使用分析", businessType = BusinessType.EXPORT)
    @PostMapping("/analytics/export")
    public void exportAnalytics(RoomAnalyticsQueryBo bo, HttpServletResponse response) {
        RoomBookingQueryBo query = new RoomBookingQueryBo();
        query.setBeginAt(bo == null ? null : bo.getBeginAt());
        query.setEndAt(bo == null ? null : bo.getEndAt());
        List<RoomBookingVo> list = roomBookingService.queryBookingList(query);
        ExcelBuilder.of(list, RoomBookingVo.class).sheetName("房间分析明细").toResponse(response);
    }
}
