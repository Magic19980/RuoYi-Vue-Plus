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
import java.time.LocalDate;

/** 循环预约中的例外实例。例外记录独立于实例，系列编辑时不会被重新生成覆盖。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_booking_recurrence_exception")
public class RoomBookingRecurrenceException extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long bookingId;

    private Integer occurrenceNo;

    private LocalDate occurrenceDate;

    private String reason;

    private String status;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
