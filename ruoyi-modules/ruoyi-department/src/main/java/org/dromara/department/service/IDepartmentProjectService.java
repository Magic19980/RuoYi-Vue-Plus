package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.DepartmentProjectBo;
import org.dromara.department.domain.bo.DepartmentProjectQueryBo;
import org.dromara.department.domain.vo.DepartmentProjectVo;

import java.util.Collection;
import java.util.List;

/**
 * 科室项目主数据业务接口。
 */
public interface IDepartmentProjectService {

    PageResult<DepartmentProjectVo> queryPageList(DepartmentProjectQueryBo bo, PageQuery pageQuery);

    DepartmentProjectVo queryById(Long id);

    List<DepartmentProjectVo> queryOptions();

    Boolean insertByBo(DepartmentProjectBo bo);

    Boolean updateByBo(DepartmentProjectBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
