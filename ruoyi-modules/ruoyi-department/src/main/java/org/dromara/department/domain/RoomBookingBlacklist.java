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

/** 房间预约黑名单。黑名单优先于房间的允许授权，仅禁止预约，不影响查看。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_booking_blacklist")
public class RoomBookingBlacklist extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long roomId;

    private String subjectType;

    private Long subjectId;

    private String reason;

    private Boolean enabled;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
