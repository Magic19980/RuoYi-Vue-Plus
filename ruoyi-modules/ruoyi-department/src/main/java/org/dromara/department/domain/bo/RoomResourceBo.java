package org.dromara.department.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/** 房间主数据新增、修改参数。 */
@Data
public class RoomResourceBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "房间主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "房间编号不能为空")
    @Size(max = 64, message = "房间编号不能超过64个字符")
    private String roomCode;

    @NotBlank(message = "房间名称不能为空")
    @Size(max = 100, message = "房间名称不能超过100个字符")
    private String roomName;

    @Size(max = 50, message = "房间类型不能超过50个字符")
    private String roomType;

    @Size(max = 200, message = "房间位置不能超过200个字符")
    private String location;

    private Long floorPlanId;

    @Size(max = 100, message = "建筑物名称不能超过100个字符")
    private String buildingName;

    @Size(max = 100, message = "楼层名称不能超过100个字符")
    private String floorName;

    @Size(max = 100, message = "区域名称不能超过100个字符")
    private String areaName;

    @Size(max = 64, message = "合并房间组不能超过64个字符")
    private String mergeGroup;

    @Size(max = 500, message = "展示图地址不能超过500个字符")
    private String displayImage;

    @Size(max = 5000, message = "房间照片地址不能超过5000个字符")
    private String photoUrls;

    @Size(max = 2000, message = "使用说明不能超过2000个字符")
    private String usageGuide;

    private Long managerUserId;

    private Boolean displayScreen;

    @NotNull(message = "房间容量不能为空")
    @Min(value = 1, message = "房间容量必须大于0")
    private Integer capacity;

    @Size(max = 500, message = "房间设施不能超过500个字符")
    private String amenities;

    private List<Long> amenityIds;

    private String status;

    private String scopeType;

    private LocalTime openTime;

    private LocalTime closeTime;

    private Boolean allowWeekend;

    private Boolean allowCrossDay;

    private String approvalMode;

    @Min(value = 0, message = "提前预约天数不能小于0")
    private Integer maxAdvanceDays;

    @Min(value = 1, message = "最长预约时长必须大于0")
    private Integer maxDurationMinutes;

    private Boolean allowRecurring;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;
}
