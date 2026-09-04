package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.PersonProfileBo;
import org.dromara.department.domain.bo.PersonProfileBatchBo;
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

    /**
     * 按当前业务科室分页查询人员档案。
     *
     * @param bo        人员档案查询条件
     * @param pageQuery 分页参数
     * @return 分页人员档案
     */
    PageResult<PersonProfileVo> queryPageList(PersonProfileQueryBo bo, PageQuery pageQuery);

    /**
     * 查询人员档案详情。
     *
     * @param id 档案主键
     * @return 人员档案详情
     */
    PersonProfileVo queryById(Long id);

    /**
     * 查询当前业务科室的人员档案列表。
     *
     * @param bo 人员档案查询条件
     * @return 人员档案列表
     */
    List<PersonProfileVo> queryList(PersonProfileQueryBo bo);

    /**
     * 查询当前用户可见的全部人员选择项。
     *
     * @return 人员选择项列表
     */
    List<PersonUserOptionVo> queryUserOptions();

    /**
     * 分页查询人员选择器数据。
     *
     * @param bo        人员选择查询条件
     * @param pageQuery 分页参数
     * @return 分页人员选择项
     */
    PageResult<PersonUserOptionVo> queryUserOptionsPage(PersonUserOptionQueryBo bo, PageQuery pageQuery);

    /**
     * 查询当前科室已纳入人员档案的有效成员，用于任务和审核人配置。
     *
     * @return 当前科室有效成员选择项
     */
    List<PersonUserOptionVo> queryMemberUserOptions();

    /**
     * 查询当前登录用户可切换的有效服务科室。
     *
     * @return 可切换的科室上下文列表
     */
    List<PersonDepartmentContextVo> queryMyDepartments();

    /**
     * 切换当前登录用户的业务科室上下文。
     *
     * @param deptId 目标业务科室主键
     * @return 切换后的科室上下文
     */
    PersonDepartmentContextVo switchMyDepartment(Long deptId);

    /**
     * 新增人员档案。
     *
     * @param bo 人员档案新增参数
     * @return 是否新增成功
     */
    Boolean insertByBo(PersonProfileBo bo);

    /**
     * 批量新增人员档案。
     *
     * @param bo 人员档案批量新增参数
     * @return 是否新增成功
     */
    Boolean insertBatch(PersonProfileBatchBo bo);

    /**
     * 修改人员档案。
     *
     * @param bo 人员档案修改参数
     * @return 是否修改成功
     */
    Boolean updateByBo(PersonProfileBo bo);

    /**
     * 删除人员档案。
     *
     * @param ids 档案主键集合
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /**
     * 结束人员服务关系。
     *
     * @param id 人员档案主键
     * @param bo 结束服务参数
     * @return 是否结束成功
     */
    Boolean endMembership(Long id, PersonProfileEndBo bo);

    /**
     * 导入人员档案数据。
     *
     * @param rows 导入行数据
     * @return 导入结果说明
     */
    String importData(List<PersonProfileImportVo> rows);
}
