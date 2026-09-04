package org.dromara.department.service;

import org.dromara.department.domain.bo.ScoreCategoryBo;
import org.dromara.department.domain.vo.ScoreCategoryVo;

import java.util.Collection;
import java.util.List;

/** SCORE 提案分类配置业务接口。 */
public interface IScoreCategoryService {

    /**
     * 查询SCORE分类树。
     *
     * @param enabledOnly 是否仅返回启用分类
     * @return 分类树数据
     */
    List<ScoreCategoryVo> queryTree(boolean enabledOnly);

    /**
     * 查询SCORE分类详情。
     *
     * @param id 分类主键
     * @return 分类详情
     */
    ScoreCategoryVo queryById(Long id);

    /**
     * 新增SCORE分类。
     *
     * @param bo 分类新增参数
     * @return 是否新增成功
     */
    Boolean insertByBo(ScoreCategoryBo bo);

    /**
     * 修改SCORE分类。
     *
     * @param bo 分类修改参数
     * @return 是否修改成功
     */
    Boolean updateByBo(ScoreCategoryBo bo);

    /**
     * 删除没有子分类和提案引用的SCORE分类。
     *
     * @param ids 分类主键集合
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);
}
