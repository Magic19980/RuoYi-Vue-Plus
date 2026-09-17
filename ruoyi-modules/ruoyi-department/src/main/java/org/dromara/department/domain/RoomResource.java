package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalTime;
import java.util.List;

/** 会议室/可预约资源主数据。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_resource")
public class RoomResource extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 资源所属业务科室；公共房间也保留维护部门。 */
    private Long deptId;

    private String roomCode;

    private String roomName;

    private String roomType;

    private String location;

    /** 所属平面图，房间在图上的位置由平面图图形数据维护。 */
    private Long floorPlanId;

    private String buildingName;

    private String floorName;

    private String areaName;

    /** 同组房间可在前端作为一个可合并资源展示。 */
    private String mergeGroup;

    /** 房间展示图、照片列表和使用说明，供找房和地图详情使用。 */
    private String displayImage;

    private String photoUrls;

    private String usageGuide;

    private Long managerUserId;

    private Boolean displayScreen;

    @TableField(exist = false)
    private String managerName;

    private Integer capacity;

    /** 设施编码，使用逗号分隔，支持快速筛选。 */
    private String amenities;

    @TableField(exist = false)
    private List<Long> amenityIds;

    /** ENABLED、MAINTENANCE、DISABLED。占用状态由预约实例动态计算。 */
    private String status;

    /** PUBLIC 或 DEPT。 */
    private String scopeType;

    private LocalTime openTime;

    private LocalTime closeTime;

    private Boolean allowWeekend;

    /** 是否允许预约跨越自然日。 */
    private Boolean allowCrossDay;

    /** NONE 或 APPROVAL。 */
    private String approvalMode;

    private Integer maxAdvanceDays;

    private Integer maxDurationMinutes;

    private Boolean allowRecurring;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
