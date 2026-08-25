package org.dromara.department.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.vo.DepartmentConfigVo;
import org.dromara.department.domain.vo.PersonDepartmentContextVo;
import org.dromara.department.mapper.DepartmentConfigMapper;
import org.dromara.department.mapper.PersonProfileMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 解析当前登录用户的业务科室上下文。
 *
 * <p>系统用户的主部门和科室业务归属不是同一个概念。用户可以同时服务多个科室，
 * 因此业务模块不能直接把 sys_user.dept_id 当作当前科室；优先使用当前会话已经
 * 选择的科室，其次才在只有一个有效服务关系时自动切换。</p>
 */
@Component
@RequiredArgsConstructor
public class DepartmentContextResolver {

    private final PersonProfileMapper personProfileMapper;
    private final DepartmentConfigMapper departmentConfigMapper;
    private final DepartmentMembershipSyncService membershipSyncService;

    /**
     * 返回当前业务科室。管理员仍使用会话中的科室；普通成员在只有一个服务科室时
     * 自动建立上下文，多个科室时要求用户显式切换，避免把数据查到错误科室。
     */
    public Long resolveCurrentDeptId() {
        Long currentDeptId = LoginHelper.getDeptId();
        if (LoginHelper.isSuperAdmin()) {
            List<DepartmentConfigVo> departments = departmentConfigMapper.selectEnabledDepartments();
            if (departments.isEmpty()) {
                throw new ServiceException("当前尚未配置启用的业务科室，请先完成科室配置");
            }
            boolean currentAvailable = departments.stream()
                .anyMatch(item -> item.getDeptId().equals(currentDeptId));
            if (currentAvailable) {
                return currentDeptId;
            }
            DepartmentConfigVo first = departments.get(0);
            LoginHelper.setActiveDept(first.getDeptId(), first.getDeptName(), null);
            return first.getDeptId();
        }

        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            throw new ServiceException("当前登录用户信息不存在");
        }

        // 以用户管理中的主部门为准补偿正式人员档案。同步服务对已经正确的关系是幂等的，
        // 因此既支持登录后首次纳入，也支持用户部门变更后的下一次请求补偿。
        membershipSyncService.syncMainDepartment(userId, LoginHelper.getMainDeptId());

        LocalDate today = LocalDate.now();
        if (currentDeptId != null && personProfileMapper.countMemberInDept(userId, currentDeptId) > 0) {
            return currentDeptId;
        }

        List<PersonDepartmentContextVo> contexts = personProfileMapper.selectCurrentDepartmentContexts(userId, today);
        if (contexts.size() == 1) {
            PersonDepartmentContextVo context = contexts.get(0);
            LoginHelper.setActiveDept(context.getDeptId(), context.getDeptName(), context.getMemberType());
            return context.getDeptId();
        }
        if (contexts.size() > 1) {
            throw new ServiceException("您同时服务多个科室，请先在右上角选择当前科室");
        }

        // 保留系统主部门作为最后兜底，让页面可以返回“未纳入人员档案”的明确空状态，
        // 而不是因为上下文为空直接报错。
        if (currentDeptId != null) {
            return currentDeptId;
        }
        throw new ServiceException("当前登录用户尚未纳入任何科室");
    }
}
