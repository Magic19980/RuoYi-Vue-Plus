package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 平面图历史版本视图。 */
@Data
public class RoomFloorPlanVersionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long floorPlanId;

    private Integer versionNo;

    private String planName;

    private String buildingName;

    private String floorName;

    private Integer floorNo;

    private String mapImage;

    private String mapData;

    private String status;

    private String remark;

    private String versionNote;

    private Long createBy;

    private String createByName;

    private LocalDateTime createTime;
}
