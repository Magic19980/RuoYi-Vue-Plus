package org.dromara.department.api;

import org.dromara.department.api.domain.DepartmentMembershipDTO;

import java.util.Collection;
import java.util.List;

/**
 * 科室人员服务关系查询接口。
 *
 * <p>系统用户模块通过该接口校验用户删除，不直接依赖科室业务模块。</p>
 */
public interface DepartmentMembershipService {

    /**
     * 查询用户当前仍在服务的科室关系。
     *
     * @param userIds 用户 ID 集合
     * @return 当前有效的科室服务关系
     */
    List<DepartmentMembershipDTO> selectActiveMemberships(Collection<Long> userIds);
}
