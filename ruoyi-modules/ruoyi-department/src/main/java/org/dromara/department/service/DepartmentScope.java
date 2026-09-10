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

    /** 创建限定到指定业务科室的数据范围。 */
    public static DepartmentScope current(Long deptId) {
        return new DepartmentScope(deptId, false);
    }

    /** 创建不限定科室的数据范围。 */
    public static DepartmentScope all() {
        return new DepartmentScope(null, true);
    }

    /** 返回限定的业务科室编号。 */
    public Long getDeptId() {
        return deptId;
    }

    /** 判断当前范围是否包含全部业务科室。 */
    public boolean isAll() {
        return all;
    }
}
