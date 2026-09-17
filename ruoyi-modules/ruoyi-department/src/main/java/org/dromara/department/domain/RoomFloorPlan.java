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

/** 楼宇楼层平面图配置，mapData 保存语义化 2D 布局，3D 场景由前端视觉规则自动生成。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_floor_plan")
public class RoomFloorPlan extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    private String planName;

    private String buildingName;

    private String floorName;

    private Integer floorNo;

    private String mapImage;

    private String mapData;

    private String status;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
