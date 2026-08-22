package org.dromara.department.service;

import org.dromara.department.domain.bo.ScoreCategoryBo;
import org.dromara.department.domain.vo.ScoreCategoryVo;

import java.util.Collection;
import java.util.List;

/** SCORE 提案分类配置业务接口。 */
public interface IScoreCategoryService {

    List<ScoreCategoryVo> queryTree(boolean enabledOnly);

    ScoreCategoryVo queryById(Long id);

    Boolean insertByBo(ScoreCategoryBo bo);

    Boolean updateByBo(ScoreCategoryBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
