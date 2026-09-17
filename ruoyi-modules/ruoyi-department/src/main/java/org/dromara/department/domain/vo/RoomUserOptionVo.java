package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 预约参与者选择项。 */
@Data
public class RoomUserOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userName;
    private String nickName;
    private String deptName;
}
