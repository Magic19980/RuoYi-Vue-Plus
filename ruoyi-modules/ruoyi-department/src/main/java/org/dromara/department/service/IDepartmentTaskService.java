package org.dromara.department.service;

import org.dromara.department.domain.bo.DepartmentReviewRuleBo;
import org.dromara.department.domain.bo.DepartmentTaskAssignmentBo;
import org.dromara.department.domain.bo.DepartmentTaskRuleBo;
import org.dromara.department.domain.vo.DepartmentReviewRuleVo;
import org.dromara.department.domain.vo.DepartmentTaskAssignmentVo;
import org.dromara.department.domain.vo.DepartmentTaskProgressVo;
import org.dromara.department.domain.vo.DepartmentTaskRuleVo;

import java.util.List;
import java.time.LocalDate;

/** 科室审核配置和周期任务服务。 */
public interface IDepartmentTaskService {

    List<DepartmentTaskRuleVo> queryRuleList();

    DepartmentTaskRuleVo queryRuleById(Long id);

    Boolean saveRule(DepartmentTaskRuleBo bo);

    Boolean deleteRule(Long id);

    List<DepartmentTaskAssignmentVo> queryAssignments(Long ruleId);

    Boolean saveAssignment(DepartmentTaskAssignmentBo bo);

    Boolean deleteAssignment(Long id);

    List<DepartmentTaskProgressVo> queryMyTasks();

    /** 查询时间范围内被分配日报任务的成员，周报缺报统计只使用这些成员。 */
    List<Long> queryDailyReportUserIds(LocalDate beginDate, LocalDate endDate);

    List<DepartmentReviewRuleVo> queryReviewRuleList();

    Boolean saveReviewRule(DepartmentReviewRuleBo bo);

    Boolean deleteReviewRule(Long id);

    /** 校验当前登录人是否为指定业务的配置审核人。 */
    void checkReviewer(String taskType, Long deptId);
}
