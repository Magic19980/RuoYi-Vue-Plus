package org.dromara.department.service;

import org.dromara.department.domain.bo.DepartmentReviewRuleBo;
import org.dromara.department.domain.bo.DepartmentTaskAssignmentBo;
import org.dromara.department.domain.bo.DepartmentTaskRuleBo;
import org.dromara.department.domain.vo.DepartmentReviewRuleVo;
import org.dromara.department.domain.vo.DepartmentTaskAssignmentVo;
import org.dromara.department.domain.vo.DepartmentTaskProgressVo;
import org.dromara.department.domain.vo.DepartmentTaskRuleVo;
import org.dromara.department.domain.vo.ScoreProposalReviewTaskVo;

import java.util.List;
import java.time.LocalDate;

/** 科室审核配置和周期任务服务。 */
public interface IDepartmentTaskService {

    /**
     * 查询当前业务科室的启用任务规则。
     *
     * @return 启用的任务规则列表
     */
    List<DepartmentTaskRuleVo> queryRuleList();

    /**
     * 查询指定任务规则详情，并校验其属于当前业务科室。
     *
     * @param id 任务规则主键
     * @return 任务规则详情
     */
    DepartmentTaskRuleVo queryRuleById(Long id);

    /**
     * 新增或修改当前业务科室的任务规则。
     *
     * @param bo 任务规则参数
     * @return 是否保存成功
     */
    Boolean saveRule(DepartmentTaskRuleBo bo);

    /**
     * 删除当前业务科室的任务规则。
     *
     * @param id 任务规则主键
     * @return 是否删除成功
     */
    Boolean deleteRule(Long id);

    /**
     * 查询指定规则下的成员分配关系。
     *
     * @param ruleId 任务规则主键
     * @return 成员分配列表
     */
    List<DepartmentTaskAssignmentVo> queryAssignments(Long ruleId);

    /**
     * 新增或修改任务成员分配关系。
     *
     * @param bo 成员分配参数
     * @return 是否保存成功
     */
    Boolean saveAssignment(DepartmentTaskAssignmentBo bo);

    /**
     * 删除任务成员分配关系。
     *
     * @param id 成员分配主键
     * @return 是否删除成功
     */
    Boolean deleteAssignment(Long id);

    /**
     * 查询当前登录人的任务进度。
     *
     * <p>该查询只读取业务数据，不在请求过程中创建或更新任务实例。</p>
     */
    List<DepartmentTaskProgressVo> queryMyTasks();

    /**
     * 查询当前登录人的SCORE提案审核/现场确认任务。
     *
     * @return 待处理的提案任务列表
     */
    List<ScoreProposalReviewTaskVo> queryMyScoreProposalReviewTasks();

    /**
     * 查询时间范围内被分配日报任务的成员，周报缺报统计只使用这些成员。
     *
     * @param beginDate 统计开始日期，包含当天
     * @param endDate   统计结束日期，包含当天
     * @return 需要填报日报的用户主键列表
     */
    List<Long> queryDailyReportUserIds(LocalDate beginDate, LocalDate endDate);

    /**
     * 查询当前业务科室的提案审核规则。
     *
     * @return 提案审核规则列表
     */
    List<DepartmentReviewRuleVo> queryReviewRuleList();

    /**
     * 新增或修改提案审核规则。
     *
     * @param bo 提案审核规则参数
     * @return 是否保存成功
     */
    Boolean saveReviewRule(DepartmentReviewRuleBo bo);

    /**
     * 删除提案审核规则。
     *
     * @param id 审核规则主键
     * @return 是否删除成功
     */
    Boolean deleteReviewRule(Long id);

    /**
     * 校验当前登录人是否为指定业务的配置审核人。
     *
     * @param taskType 业务任务类型
     * @param deptId   业务科室主键
     */
    void checkReviewer(String taskType, Long deptId);

    /**
     * 获取当前生效的主/备用审核人，用于提交时创建事件型审核任务。
     *
     * @param taskType 业务任务类型
     * @param deptId   业务科室主键
     * @return 当前生效的审核人用户主键列表
     */
    List<Long> getReviewerUserIds(String taskType, Long deptId);
}
