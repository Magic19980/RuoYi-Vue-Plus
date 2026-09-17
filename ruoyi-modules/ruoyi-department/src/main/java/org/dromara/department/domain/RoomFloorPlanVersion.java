package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 平面图发布前后的不可变快照。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_floor_plan_version")
public class RoomFloorPlanVersion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
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
}
