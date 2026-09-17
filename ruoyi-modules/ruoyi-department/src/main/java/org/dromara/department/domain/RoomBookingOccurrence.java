package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/** 预约系列展开后的房间占用实例。多房间预约会有多条同序号实例。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_booking_occurrence")
public class RoomBookingOccurrence extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long bookingId;

    private Long roomId;

    private Integer occurrenceNo;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private String status;

    /** 当前待审批级别，从1开始；无需审批的实例为null。 */
    private Integer approvalStep;

    /** 当前审批级别的截止时间，null表示不自动超时。 */
    private LocalDateTime approvalDueAt;

    private LocalDateTime checkInAt;

    private LocalDateTime checkOutAt;

    private String cancellationReason;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
