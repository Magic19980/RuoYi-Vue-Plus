package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 房间设施目录视图。 */
@Data
public class RoomAmenityVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long deptId;
    private String deptName;
    private String amenityName;
    private String category;
    private String icon;
    private Integer sortNo;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}
