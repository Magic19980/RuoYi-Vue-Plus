package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 预约冲突预检结果。 */
@Data
public class RoomConflictResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean available;
    private List<RoomConflictVo> conflicts = new ArrayList<>();
}
