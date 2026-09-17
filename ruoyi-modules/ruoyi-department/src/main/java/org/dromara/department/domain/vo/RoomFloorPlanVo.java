package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 楼层平面图视图。 */
@Data
public class RoomFloorPlanVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long deptId;

    private String deptName;

    private String planName;

    private String buildingName;

    private String floorName;

    private Integer floorNo;

    private String mapImage;

    private String mapData;

    private String status;

    private String remark;

    private LocalDateTime createTime;
}
