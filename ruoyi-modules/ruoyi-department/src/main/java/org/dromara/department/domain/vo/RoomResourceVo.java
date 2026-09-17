package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** 房间主数据视图。 */
@Data
@ExcelIgnoreUnannotated
public class RoomResourceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("主键")
    private Long id;

    private Long deptId;

    @ExcelProperty("房间编号")
    private String roomCode;

    @ExcelProperty("房间名称")
    private String roomName;

    @ExcelProperty("房间类型")
    private String roomType;

    @ExcelProperty("位置")
    private String location;

    private Long floorPlanId;

    private String buildingName;

    private String floorName;

    private String areaName;

    private String mergeGroup;

    private String displayImage;

    private String photoUrls;

    private String usageGuide;

    private Long managerUserId;

    private String managerName;

    private Boolean displayScreen;

    @ExcelProperty("容量")
    private Integer capacity;

    @ExcelProperty("设施")
    private String amenities;

    private List<Long> amenityIds;

    private List<String> amenityNames;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("使用范围")
    private String scopeType;

    private LocalTime openTime;

    private LocalTime closeTime;

    private Boolean allowWeekend;

    private Boolean allowCrossDay;

    private String approvalMode;

    private Integer maxAdvanceDays;

    private Integer maxDurationMinutes;

    private Boolean allowRecurring;

    @ExcelProperty("备注")
    private String remark;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
