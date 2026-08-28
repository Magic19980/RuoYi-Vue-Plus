package org.dromara.department.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.mapper.PersonProfileMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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

    private static final String REQUEST_ACCESS_CACHE_KEY = DepartmentAccessService.class.getName() + ".access";

    private final PersonProfileMapper personProfileMapper;
    private final DepartmentContextResolver departmentContextResolver;

    /**
     * 获取当前请求绑定的业务科室。
     *
     * <p>该值来自当前用户可用的服务科室上下文，而不是直接读取系统主部门。</p>
     */
    public Long currentDeptId() {
        return departmentContextResolver.resolveCurrentDeptId();
    }

    /**
     * 校验当前用户是否可以访问指定科室的数据。
     *
     * @param deptId 目标科室
     * @param permission 允许直接放行的权限字符，可为空
     * @return 是否允许访问
     */
    public boolean canViewDepartment(Long deptId, String permission) {
        if (LoginHelper.isSuperAdmin()) {
            return true;
        }
        if (deptId == null || LoginHelper.getUserId() == null) {
            return false;
        }

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String cacheKey = deptId + "|" + (permission == null ? "" : permission);
        Map<String, Boolean> accessCache = getRequestAccessCache(requestAttributes);
        if (accessCache != null && accessCache.containsKey(cacheKey)) {
            return accessCache.get(cacheKey);
        }

        boolean allowed = permission != null && StpUtil.hasPermission(permission)
            || personProfileMapper.countFullMemberInDeptAt(
                LoginHelper.getUserId(), deptId, LocalDate.now()) > 0;
        if (requestAttributes != null) {
            if (accessCache == null) {
                accessCache = new HashMap<>();
                requestAttributes.setAttribute(REQUEST_ACCESS_CACHE_KEY, accessCache, RequestAttributes.SCOPE_REQUEST);
            }
            accessCache.put(cacheKey, allowed);
        }
        return allowed;
    }

    /**
     * 清理当前请求的科室和权限缓存。科室上下文切换后必须调用。
     */
    public void clearRequestCache() {
        departmentContextResolver.clearRequestCache();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.removeAttribute(REQUEST_ACCESS_CACHE_KEY, RequestAttributes.SCOPE_REQUEST);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> getRequestAccessCache(RequestAttributes requestAttributes) {
        if (requestAttributes == null) {
            return null;
        }
        Object value = requestAttributes.getAttribute(REQUEST_ACCESS_CACHE_KEY, RequestAttributes.SCOPE_REQUEST);
        return value instanceof Map ? (Map<String, Boolean>) value : null;
    }

    /**
     * 校验当前用户是否可以访问当前业务科室。
     *
     * @param permission 允许直接放行的权限字符，可为空
     * @return 是否允许访问
     */
    public boolean canViewCurrentDepartment(String permission) {
        Long deptId = departmentContextResolver.resolveCurrentDeptId();
        return canViewDepartment(deptId, permission);
    }

    /**
     * 获取当前业务科室，不存在有效上下文时统一抛出业务异常。
     *
     * @param message 无上下文时返回给前端的提示
     * @return 当前业务科室编号
     */
    public Long requireCurrentDept(String message) {
        Long deptId = currentDeptId();
        if (deptId == null) {
            throw new ServiceException(message == null || message.isBlank()
                ? "当前未选择有效业务科室"
                : message);
        }
        return deptId;
    }

    /**
     * 解析当前登录用户的业务数据范围。超级管理员同样遵循当前科室上下文，
     * 不在业务列表中隐式展开全部科室。
     */
    public DepartmentScope scope(String permission) {
        Long deptId = departmentContextResolver.resolveCurrentDeptId();
        return canViewDepartment(deptId, permission)
            ? DepartmentScope.current(deptId)
            : DepartmentScope.current(null);
    }

    /**
     * 判断当前登录用户是否为超级管理员。
     *
     * @return 是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return LoginHelper.isSuperAdmin();
    }

    /**
     * 校验实体所属科室是否属于当前业务上下文。
     *
     * @param entityDeptId 实体所属科室
     * @param permission 允许直接放行的权限字符，可为空
     * @return 是否允许访问
     */
    public boolean canViewEntityDept(Long entityDeptId, String permission) {
        if (entityDeptId == null) {
            return false;
        }
        if (LoginHelper.isSuperAdmin()) {
            return entityDeptId.equals(departmentContextResolver.resolveCurrentDeptId());
        }
        Long currentDeptId = departmentContextResolver.resolveCurrentDeptId();
        return entityDeptId.equals(currentDeptId) && canViewDepartment(entityDeptId, permission);
    }

    /**
     * 校验实体所属科室访问权限，失败时抛出统一业务异常。
     *
     * @param deptId 目标科室
     * @param permission 允许直接放行的权限字符，可为空
     */
    public void requireDepartmentView(Long deptId, String permission) {
        if (!canViewEntityDept(deptId, permission)) {
            throw new ServiceException("您没有访问该科室数据的权限");
        }
    }
}
