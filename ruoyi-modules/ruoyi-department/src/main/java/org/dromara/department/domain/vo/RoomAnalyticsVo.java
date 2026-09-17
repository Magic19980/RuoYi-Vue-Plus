package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** 房间使用分析结果。 */
@Data
public class RoomAnalyticsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDateTime beginAt;

    private LocalDateTime endAt;

    private RoomAnalyticsSummaryVo summary;

    private List<RoomAnalyticsRoomVo> rooms;

    private List<RoomAnalyticsDimensionVo> departments;

    private List<RoomAnalyticsDimensionVo> users;

    private List<RoomAnalyticsHotSlotVo> hotSlots;

    private RoomAnalyticsApprovalVo approvalStats;
}
