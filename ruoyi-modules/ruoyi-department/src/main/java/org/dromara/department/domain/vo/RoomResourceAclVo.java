package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 房间级授权展示视图。 */
@Data
public class RoomResourceAclVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roomId;
    private String subjectType;
    private Long subjectId;
    private String subjectName;
    private String permissionType;
    private LocalDateTime createTime;
}
