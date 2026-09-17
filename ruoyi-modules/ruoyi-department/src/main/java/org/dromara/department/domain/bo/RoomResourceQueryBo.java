package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 房间主数据查询参数。 */
@Data
public class RoomResourceQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String roomCode;

    private String roomName;

    private String roomType;

    private String location;

    private Integer minCapacity;

    private String amenities;

    private String status;

    private String scopeType;
}
