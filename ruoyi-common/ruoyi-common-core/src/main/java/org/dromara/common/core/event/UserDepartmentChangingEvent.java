package org.dromara.common.core.event;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统用户主部门变更前事件。
 *
 * <p>业务模块可以在用户主部门真正更新前校验自己的服务关系，避免组织关系
 * 被直接改掉后留下仍然有效的旧科室成员关系。</p>
 */
@Getter
public class UserDepartmentChangingEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final Long oldDeptId;
    private final Long newDeptId;

    public UserDepartmentChangingEvent(Long userId, Long oldDeptId, Long newDeptId) {
        this.userId = userId;
        this.oldDeptId = oldDeptId;
        this.newDeptId = newDeptId;
    }
}
