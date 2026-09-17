package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 某用户在某房间、某配额周期内的已用量。 */
@Data
public class RoomQuotaUsageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long usedMinutes;
    private Long usedCount;
}
