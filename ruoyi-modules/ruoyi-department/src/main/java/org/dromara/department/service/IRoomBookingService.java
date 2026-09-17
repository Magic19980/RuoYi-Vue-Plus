package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.RoomBookingBo;
import org.dromara.department.domain.bo.RoomBookingQueryBo;
import org.dromara.department.domain.bo.RoomCalendarExceptionBo;
import org.dromara.department.domain.bo.RoomCalendarExceptionQueryBo;
import org.dromara.department.domain.bo.RoomResourceBo;
import org.dromara.department.domain.bo.RoomResourceQueryBo;
import org.dromara.department.domain.bo.RoomFloorPlanBo;
import org.dromara.department.domain.bo.RoomFloorPlanQueryBo;
import org.dromara.department.domain.bo.RoomAnalyticsQueryBo;
import org.dromara.department.domain.bo.RoomResourceAclBo;
import org.dromara.department.domain.bo.RoomBookingExtendBo;
import org.dromara.department.domain.bo.RoomBookingBlockBo;
import org.dromara.department.domain.bo.RoomApprovalRuleBo;
import org.dromara.department.domain.bo.RoomQuotaPolicyBo;
import org.dromara.department.domain.bo.RoomAmenityBo;
import org.dromara.department.domain.bo.RoomBookingBlacklistBo;
import org.dromara.department.domain.bo.RoomResourceMergeBo;
import org.dromara.department.domain.vo.RoomCalendarExceptionVo;
import org.dromara.department.domain.vo.RoomBookingDetailVo;
import org.dromara.department.domain.vo.RoomBookingVo;
import org.dromara.department.domain.vo.RoomConflictResultVo;
import org.dromara.department.domain.vo.RoomResourceVo;
import org.dromara.department.domain.vo.RoomUserOptionVo;
import org.dromara.department.domain.vo.RoomResourceAclVo;
import org.dromara.department.domain.vo.RoomBookingBlockVo;
import org.dromara.department.domain.vo.RoomFloorPlanVo;
import org.dromara.department.domain.vo.RoomAnalyticsVo;
import org.dromara.department.domain.vo.RoomApprovalRuleVo;
import org.dromara.department.domain.vo.RoomQuotaPolicyVo;
import org.dromara.department.domain.vo.RoomAmenityVo;
import org.dromara.department.domain.vo.RoomBookingBlacklistVo;
import org.dromara.department.domain.vo.RoomBookingRecurrenceExceptionVo;
import org.dromara.department.domain.vo.RoomQuotaUsageRecordVo;
import org.dromara.department.domain.vo.RoomFloorPlanVersionVo;

import java.util.Collection;
import java.util.List;

/** 房间资源与预约业务接口。 */
public interface IRoomBookingService {

    PageResult<RoomResourceVo> queryRoomPage(RoomResourceQueryBo bo, PageQuery pageQuery);

    List<RoomResourceVo> queryRoomOptions();

    List<RoomResourceVo> queryRoomViewOptions();

    RoomResourceVo queryRoomById(Long id);

    Boolean insertRoom(RoomResourceBo bo);

    Boolean updateRoom(RoomResourceBo bo);

    Boolean deleteRooms(Collection<Long> ids);

    List<RoomUserOptionVo> queryUserOptions();

    List<RoomResourceAclVo> queryRoomAcls(Long roomId);

    Boolean saveRoomAcl(RoomResourceAclBo bo);

    Boolean deleteRoomAcls(Collection<Long> ids);

    List<RoomBookingBlacklistVo> queryRoomBlacklists(Long roomId);

    Boolean saveRoomBlacklist(RoomBookingBlacklistBo bo);

    Boolean deleteRoomBlacklists(Collection<Long> ids);

    List<RoomBookingBlockVo> queryRoomBlocks(Long roomId);

    Boolean saveRoomBlock(RoomBookingBlockBo bo);

    Boolean deleteRoomBlocks(Collection<Long> ids);

    List<RoomApprovalRuleVo> queryApprovalRules(Long roomId);

    Boolean saveApprovalRule(RoomApprovalRuleBo bo);

    Boolean deleteApprovalRules(Collection<Long> ids);

    List<RoomQuotaPolicyVo> queryQuotaPolicies(Long roomId);

    Boolean saveQuotaPolicy(RoomQuotaPolicyBo bo);

    Boolean deleteQuotaPolicies(Collection<Long> ids);

    PageResult<RoomAmenityVo> queryAmenityPage(PageQuery pageQuery);

    List<RoomAmenityVo> queryAmenityOptions();

    Boolean insertAmenity(RoomAmenityBo bo);

    Boolean updateAmenity(RoomAmenityBo bo);

    Boolean deleteAmenities(Collection<Long> ids);

    PageResult<RoomFloorPlanVo> queryFloorPlanPage(RoomFloorPlanQueryBo bo, PageQuery pageQuery);

    List<RoomFloorPlanVo> queryFloorPlanOptions();

    RoomFloorPlanVo queryFloorPlanById(Long id);

    Boolean insertFloorPlan(RoomFloorPlanBo bo);

    Boolean updateFloorPlan(RoomFloorPlanBo bo);

    Boolean deleteFloorPlans(Collection<Long> ids);

    List<RoomFloorPlanVersionVo> queryFloorPlanVersions(Long floorPlanId);

    Boolean publishFloorPlan(Long floorPlanId);

    Boolean unpublishFloorPlan(Long floorPlanId);

    Boolean restoreFloorPlanVersion(Long versionId);

    Boolean splitRoomGroup(Long roomId);

    Boolean mergeRoomGroup(RoomResourceMergeBo bo);

    RoomAnalyticsVo queryRoomAnalytics(RoomAnalyticsQueryBo bo);

    PageResult<RoomCalendarExceptionVo> queryCalendarExceptionPage(RoomCalendarExceptionQueryBo bo, PageQuery pageQuery);

    Boolean insertCalendarException(RoomCalendarExceptionBo bo);

    Boolean updateCalendarException(RoomCalendarExceptionBo bo);

    Boolean deleteCalendarExceptions(Collection<Long> ids);

    List<RoomBookingRecurrenceExceptionVo> queryRecurrenceExceptions(Long bookingId);

    List<RoomQuotaUsageRecordVo> queryQuotaUsage(Long roomId);

    List<RoomBookingVo> queryCalendar(RoomBookingQueryBo bo);

    PageResult<RoomBookingVo> queryBookingPage(RoomBookingQueryBo bo, PageQuery pageQuery);

    long queryPendingBookingCount();

    RoomBookingDetailVo queryBookingById(Long id);

    RoomBookingDetailVo queryBookingById(Long id, Integer occurrenceNo);

    String exportBookingIcal(Long id, Integer occurrenceNo);

    RoomConflictResultVo checkConflict(RoomBookingBo bo);

    Boolean saveBooking(RoomBookingBo bo);

    Boolean cancelBookings(Collection<Long> ids);

    Boolean cancelBookings(Collection<Long> ids, String operationScope, Integer occurrenceNo, String reason);

    Boolean approveBooking(Long id);

    Boolean approveBooking(Long id, Integer occurrenceNo);

    Boolean rejectBooking(Long id, String reason);

    Boolean rejectBooking(Long id, Integer occurrenceNo, String reason);

    List<RoomBookingVo> queryBookingList(RoomBookingQueryBo bo);

    Boolean checkInBooking(Long id, Integer occurrenceNo);

    Boolean checkOutBooking(Long id, Integer occurrenceNo);

    Boolean releaseBooking(Long id, Integer occurrenceNo, String reason);

    Boolean extendBooking(RoomBookingExtendBo bo);

    int processBookingLifecycle();
}
