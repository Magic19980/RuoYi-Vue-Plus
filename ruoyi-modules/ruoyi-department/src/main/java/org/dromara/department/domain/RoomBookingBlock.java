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

/** 房间临时封锁时段，例如维护、培训或临时占用。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_booking_block")
public class RoomBookingBlock extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long roomId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String reason;
    private String status;

    /** NONE、DAILY、WEEKLY、MONTHLY；维护计划展开为多个封锁实例。 */
    private String recurrenceType;

    private Integer recurrenceInterval;

    private Integer recurrenceCount;

    private java.time.LocalDate recurrenceUntil;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
