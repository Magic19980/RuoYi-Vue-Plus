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

    /**
     * 分页查询科室项目。
     *
     * @param bo        项目查询条件
     * @param pageQuery 分页参数
     * @return 分页项目数据
     */
    PageResult<DepartmentProjectVo> queryPageList(DepartmentProjectQueryBo bo, PageQuery pageQuery);

    /**
     * 查询项目详情。
     *
     * @param id 项目主键
     * @return 项目详情
     */
    DepartmentProjectVo queryById(Long id);

    /**
     * 查询当前业务科室的项目选项。
     *
     * @return 项目选项列表
     */
    List<DepartmentProjectVo> queryOptions();

    /**
     * 新增科室项目。
     *
     * @param bo 项目新增参数
     * @return 是否新增成功
     */
    Boolean insertByBo(DepartmentProjectBo bo);

    /**
     * 修改科室项目。
     *
     * @param bo 项目修改参数
     * @return 是否修改成功
     */
    Boolean updateByBo(DepartmentProjectBo bo);

    /**
     * 删除科室项目。
     *
     * @param ids 项目主键集合
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);
}
