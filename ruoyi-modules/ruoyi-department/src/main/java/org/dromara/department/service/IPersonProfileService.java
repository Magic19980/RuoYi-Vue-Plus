package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.PersonProfileBo;
import org.dromara.department.domain.bo.PersonProfileQueryBo;
import org.dromara.department.domain.vo.PersonProfileVo;
import org.dromara.department.domain.vo.PersonProfileImportVo;
import org.dromara.department.domain.vo.PersonUserOptionVo;

import java.util.Collection;
import java.util.List;

/**
 * 人员档案业务接口。
 */
public interface IPersonProfileService {

    PageResult<PersonProfileVo> queryPageList(PersonProfileQueryBo bo, PageQuery pageQuery);

    PersonProfileVo queryById(Long id);

    List<PersonProfileVo> queryList(PersonProfileQueryBo bo);

    List<PersonUserOptionVo> queryUserOptions();

    Boolean insertByBo(PersonProfileBo bo);

    Boolean updateByBo(PersonProfileBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    String importData(List<PersonProfileImportVo> rows);
}
