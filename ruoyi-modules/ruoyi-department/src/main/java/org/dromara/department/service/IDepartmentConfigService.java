package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.DepartmentConfigBo;
import org.dromara.department.domain.bo.DepartmentConfigMigrationBo;
import org.dromara.department.domain.bo.DepartmentConfigQueryBo;
import org.dromara.department.domain.vo.DepartmentConfigVo;

import java.util.Collection;
import java.util.List;

/** 业务科室配置服务。 */
public interface IDepartmentConfigService {

    /**
     * 分页查询业务科室配置。
     *
     * @param bo        科室配置查询条件
     * @param pageQuery 分页参数
     * @return 分页科室配置数据
     */
    PageResult<DepartmentConfigVo> queryPageList(DepartmentConfigQueryBo bo, PageQuery pageQuery);

    /**
     * 查询业务科室配置详情。
     *
     * @param deptId 系统部门主键
     * @return 科室配置详情，未配置时返回 {@code null}
     */
    DepartmentConfigVo queryById(Long deptId);

    /**
     * 查询尚未配置为业务科室的有效系统部门。
     *
     * @param deptName 部门名称或印尼语名称关键字
     * @return 可配置的部门选项
     */
    List<DepartmentConfigVo> queryAvailableDepartments(String deptName);

    /**
     * 查询泛微组织树的直属下级部门。
     *
     * @param parentId 父部门主键
     * @return 直属下级部门列表
     */
    List<DepartmentConfigVo> queryOrganizationChildren(Long parentId);

    /**
     * 新增业务科室配置。
     *
     * @param bo 科室配置新增参数
     * @return 是否新增成功
     */
    Boolean insertByBo(DepartmentConfigBo bo);

    /**
     * 修改业务科室配置。
     *
     * @param bo 科室配置修改参数
     * @return 是否修改成功
     */
    Boolean updateByBo(DepartmentConfigBo bo);

    /**
     * 将科室及其关联业务数据迁移至新的系统部门。
     *
     * @param bo 科室迁移参数
     */
    void migrate(DepartmentConfigMigrationBo bo);

    /**
     * 停用业务科室配置，并结束其自动纳入的人员关系。
     *
     * @param deptIds 系统部门主键集合
     * @return 是否停用成功
     */
    Boolean disableByIds(Collection<Long> deptIds);
}
