package org.dromara.department.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.mapper.PersonProfileMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 科室业务数据范围的统一判定入口。
 *
 * <p>sys_user.dept_id 只表示系统组织主部门，不能直接作为业务数据权限。
 * 业务权限以 dm_person_profile 的有效服务关系为准：正式成员可以查看当前
 * 服务科室的全部数据，临时成员默认只能查看自己的数据；管理员权限仍可作为
 * 临时协作人员的显式放开条件。</p>
 */
@Component
@RequiredArgsConstructor
public class DepartmentAccessService {

    private final PersonProfileMapper personProfileMapper;
    private final DepartmentContextResolver departmentContextResolver;

    public Long currentDeptId() {
        return departmentContextResolver.resolveCurrentDeptId();
    }

    public boolean canViewDepartment(Long deptId, String permission) {
        if (LoginHelper.isSuperAdmin()) {
            return true;
        }
        if (deptId == null || LoginHelper.getUserId() == null) {
            return false;
        }
        if (permission != null && StpUtil.hasPermission(permission)) {
            return true;
        }
        return personProfileMapper.countFullMemberInDeptAt(
            LoginHelper.getUserId(), deptId, LocalDate.now()) > 0;
    }

    public boolean canViewCurrentDepartment(String permission) {
        Long deptId = currentDeptId();
        return canViewDepartment(deptId, permission);
    }

    /**
     * 返回列表查询使用的部门范围。返回 null 时由各模块继续按 userId 限制，
     * 从而保证临时成员和未纳入人员档案的用户不会意外看到全科室数据。
     */
    public Long scopeDeptId(String permission) {
        if (!canViewCurrentDepartment(permission)) {
            return null;
        }
        return currentDeptId();
    }

    public boolean isSuperAdmin() {
        return LoginHelper.isSuperAdmin();
    }

    public boolean canViewEntityDept(Long entityDeptId, String permission) {
        if (entityDeptId == null) {
            return false;
        }
        if (LoginHelper.isSuperAdmin()) {
            return entityDeptId.equals(currentDeptId());
        }
        Long currentDeptId = currentDeptId();
        return entityDeptId.equals(currentDeptId) && canViewDepartment(entityDeptId, permission);
    }

    public void requireDepartmentView(Long deptId, String permission) {
        if (!canViewEntityDept(deptId, permission)) {
            throw new ServiceException("您没有访问该科室数据的权限");
        }
    }
}
