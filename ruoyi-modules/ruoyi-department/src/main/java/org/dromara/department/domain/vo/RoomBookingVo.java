package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 日历事件、预约列表和导出视图。 */
@Data
@ExcelIgnoreUnannotated
public class RoomBookingVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("预约主键")
    private Long id;

    private Long occurrenceId;

    private Long roomId;

    @ExcelProperty("房间编号")
    private String roomCode;

    @ExcelProperty("房间名称")
    private String roomName;

    @ExcelProperty("房间类型")
    private String roomType;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("会议主题")
    private String title;

    @ExcelProperty("组织人")
    private String organizerName;

    private Long organizerId;

    @ExcelProperty("组织部门")
    private String deptName;

    private Long deptId;

    @ExcelProperty("开始时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;

    @ExcelProperty("结束时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;

    @ExcelProperty("状态")
    private String status;

    private String visibility;

    private String recurrenceType;

    private Integer occurrenceNo;

    /** 实例状态；循环预约可能出现同一系列不同实例状态。 */
    private String occurrenceStatus;

    private Integer approvalStep;

    private LocalDateTime approvalDueAt;

    private LocalDateTime checkInAt;

    private LocalDateTime checkOutAt;

    private String description;

    private String attendeeNames;

    private Boolean mine;

    private Boolean canEdit;

    private Boolean canCancel;

    private Boolean canCheckIn;

    private Boolean canCheckOut;

    private Boolean canRelease;

    private Boolean canExtend;

    private Boolean canApprove;
}
