package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 预约编辑详情。 */
@Data
public class RoomBookingDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String description;
    private String visibility;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String recurrenceType;
    private Integer recurrenceInterval;
    private LocalDate recurrenceUntil;
    private Integer recurrenceCount;
    private String status;
    private Boolean approvalRequired;
    private Long organizerId;
    private String organizerName;
    private Integer occurrenceNo;
    private Long occurrenceId;
    private Integer approvalStep;
    private LocalDateTime approvalDueAt;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private Boolean canCheckIn;
    private Boolean canCheckOut;
    private Boolean canRelease;
    private Boolean canExtend;
    private List<Long> roomIds = new ArrayList<>();
    private List<Long> attendeeIds = new ArrayList<>();
    private List<RoomBookingApprovalVo> approvalRecords = new ArrayList<>();

    private List<RoomBookingRecurrenceExceptionVo> recurrenceExceptions = new ArrayList<>();
}
