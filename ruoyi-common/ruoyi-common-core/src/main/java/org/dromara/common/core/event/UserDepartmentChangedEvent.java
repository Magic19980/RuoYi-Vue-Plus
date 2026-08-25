package org.dromara.common.core.event;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统用户主部门变更事件。
 *
 * <p>系统模块只发布用户主部门变化，具体业务模块自行决定是否存在对应的业务配置，
 * 从而避免 system 模块反向依赖 department 模块。</p>
 */
@Getter
public class UserDepartmentChangedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final Long oldDeptId;
    private final Long newDeptId;

    public UserDepartmentChangedEvent(Long userId, Long oldDeptId, Long newDeptId) {
        this.userId = userId;
        this.oldDeptId = oldDeptId;
        this.newDeptId = newDeptId;
    }
}
