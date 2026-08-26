package org.dromara.department.service;

/**
 * 当前请求的科室数据范围。
 *
 * <p>业务模块统一通过该对象表达查询范围，避免各模块自行维护
 * {@code all}、{@code canViewAll} 和当前科室判断。</p>
 */
public final class DepartmentScope {

    private final Long deptId;
    private final boolean all;

    private DepartmentScope(Long deptId, boolean all) {
        this.deptId = deptId;
        this.all = all;
    }

    public static DepartmentScope current(Long deptId) {
        return new DepartmentScope(deptId, false);
    }

    public static DepartmentScope all() {
        return new DepartmentScope(null, true);
    }

    public Long getDeptId() {
        return deptId;
    }

    public boolean isAll() {
        return all;
    }
}
