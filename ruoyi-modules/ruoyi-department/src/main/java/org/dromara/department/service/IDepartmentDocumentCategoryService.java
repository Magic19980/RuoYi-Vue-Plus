package org.dromara.department.service;

import org.dromara.department.domain.bo.DepartmentDocumentCategoryBo;
import org.dromara.department.domain.bo.DepartmentDocumentCategoryQueryBo;
import org.dromara.department.domain.vo.DepartmentDocumentCategoryVo;

import java.util.Collection;
import java.util.List;

/** 科室资料分类配置业务接口。 */
public interface IDepartmentDocumentCategoryService {

    /**
     * 查询当前业务科室可用的资料分类选项。
     *
     * @return 分类选项列表
     */
    List<DepartmentDocumentCategoryVo> queryOptions();

    /**
     * 查询当前业务科室的资料分类树。
     *
     * @param bo 分类查询条件
     * @return 分类树数据
     */
    List<DepartmentDocumentCategoryVo> queryTreeList(DepartmentDocumentCategoryQueryBo bo);

    /**
     * 新增资料分类。
     *
     * @param bo 分类新增参数
     * @return 是否新增成功
     */
    Boolean insertByBo(DepartmentDocumentCategoryBo bo);

    /**
     * 修改资料分类。
     *
     * @param bo 分类修改参数
     * @return 是否修改成功
     */
    Boolean updateByBo(DepartmentDocumentCategoryBo bo);

    /**
     * 删除资料分类及其允许删除的记录。
     *
     * @param ids 分类主键集合
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);
}
