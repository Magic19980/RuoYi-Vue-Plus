package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 楼层平面图查询参数。 */
@Data
public class RoomFloorPlanQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String planName;

    private String buildingName;

    private String floorName;

    private String status;
}
