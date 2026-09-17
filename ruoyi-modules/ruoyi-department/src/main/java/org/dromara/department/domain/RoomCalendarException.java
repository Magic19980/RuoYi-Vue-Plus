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

/** 房间日历例外：节假日或补班日。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_calendar_exception")
public class RoomCalendarException extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    private LocalDate exceptionDate;

    /** HOLIDAY 禁止预约，WORKDAY 覆盖周末限制允许预约。 */
    private String exceptionType;

    private String exceptionName;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
