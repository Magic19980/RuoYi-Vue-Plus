package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/** 配额消费、释放和退款流水。配额实时校验仍以预约实例为准，流水用于审计和结算。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_quota_usage")
public class RoomQuotaUsage extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long roomId;
    private Long bookingId;
    private Integer occurrenceNo;
    private Long userId;
    private String action;
    private Integer minutes;
    private Integer bookingCount;
    private BigDecimal amount;
    private String reason;

    @TableLogic
    private String delFlag;
}
