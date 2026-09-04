package org.dromara.department.service;

import org.dromara.department.domain.bo.DepartmentDocumentCategoryBo;
import org.dromara.department.domain.bo.DepartmentDocumentCategoryQueryBo;
import org.dromara.department.domain.vo.DepartmentDocumentCategoryVo;

import java.util.Collection;
import java.util.List;

/** 科室资料分类配置业务接口。 */
public interface IDepartmentDocumentCategoryService {

    List<DepartmentDocumentCategoryVo> queryOptions();

    List<DepartmentDocumentCategoryVo> queryTreeList(DepartmentDocumentCategoryQueryBo bo);

    Boolean insertByBo(DepartmentDocumentCategoryBo bo);

    Boolean updateByBo(DepartmentDocumentCategoryBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
