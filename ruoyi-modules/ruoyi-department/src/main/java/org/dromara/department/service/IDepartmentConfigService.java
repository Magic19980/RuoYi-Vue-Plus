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

    PageResult<DepartmentConfigVo> queryPageList(DepartmentConfigQueryBo bo, PageQuery pageQuery);

    DepartmentConfigVo queryById(Long deptId);

    List<DepartmentConfigVo> queryAvailableDepartments(String deptName);

    List<DepartmentConfigVo> queryOrganizationChildren(Long parentId);

    Boolean insertByBo(DepartmentConfigBo bo);

    Boolean updateByBo(DepartmentConfigBo bo);

    void migrate(DepartmentConfigMigrationBo bo);

    Boolean disableByIds(Collection<Long> deptIds);
}
