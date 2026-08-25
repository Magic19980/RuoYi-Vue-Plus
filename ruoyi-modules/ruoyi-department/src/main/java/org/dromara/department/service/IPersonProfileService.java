package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.PersonProfileBo;
import org.dromara.department.domain.bo.PersonProfileEndBo;
import org.dromara.department.domain.bo.PersonProfileQueryBo;
import org.dromara.department.domain.bo.PersonUserOptionQueryBo;
import org.dromara.department.domain.vo.PersonProfileVo;
import org.dromara.department.domain.vo.PersonProfileImportVo;
import org.dromara.department.domain.vo.PersonDepartmentContextVo;
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

    PageResult<PersonUserOptionVo> queryUserOptionsPage(PersonUserOptionQueryBo bo, PageQuery pageQuery);

    /** 当前科室已纳入人员档案的有效成员，用于任务和审核人配置。 */
    List<PersonUserOptionVo> queryMemberUserOptions();

    /** 当前登录用户可切换的有效服务科室。 */
    List<PersonDepartmentContextVo> queryMyDepartments();

    /** 切换当前登录用户的业务科室上下文。 */
    PersonDepartmentContextVo switchMyDepartment(Long deptId);

    Boolean insertByBo(PersonProfileBo bo);

    Boolean updateByBo(PersonProfileBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    Boolean endMembership(Long id, PersonProfileEndBo bo);

    String importData(List<PersonProfileImportVo> rows);
}
