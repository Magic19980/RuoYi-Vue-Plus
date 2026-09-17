package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 房间预约审批及状态变更历史。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_booking_approval")
public class RoomBookingApproval extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long bookingId;

    /** null 表示系列级操作，否则表示具体实例序号。 */
    private Integer occurrenceNo;

    /** SUBMIT、UPDATE、APPROVE、REJECT、CANCEL。 */
    private String action;

    private String fromStatus;

    private String toStatus;

    private Long operatorId;

    private String reason;
}
