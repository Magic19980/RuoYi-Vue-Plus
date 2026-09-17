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
import java.time.LocalDateTime;

/** 预约系列主记录。实际占用时间段保存在 {@link RoomBookingOccurrence}。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_booking")
public class RoomBooking extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 发起预约时的业务科室。 */
    private Long deptId;

    private Long organizerId;

    /** 新建预约请求幂等号，重试同一请求不会重复创建预约。 */
    private String requestKey;

    private String title;

    private String description;

    /** PUBLIC 或 PRIVATE。 */
    private String visibility;

    /** NONE、DAILY、WEEKLY、MONTHLY。 */
    private String recurrenceType;

    private Integer recurrenceInterval;

    private LocalDate recurrenceUntil;

    private Integer recurrenceCount;

    /** 系列的第一条预约时间，方便详情和编辑。 */
    private LocalDateTime baseStartAt;

    private LocalDateTime baseEndAt;

    /** PENDING、CONFIRMED、IN_USE、REJECTED、CANCELLED、RELEASED、COMPLETED。 */
    private String status;

    private Boolean approvalRequired;

    private Long approvedBy;

    private LocalDateTime approvedAt;

    private String rejectedReason;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
