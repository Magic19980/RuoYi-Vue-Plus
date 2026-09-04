package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.department.domain.DepartmentConfig;
import org.dromara.department.domain.vo.DepartmentConfigMigrationConflictVo;

import java.time.LocalDateTime;
import java.util.List;

/** 业务科室配置迁移数据层。所有迁移更新在服务事务中执行。 */
@Mapper
public interface DepartmentConfigMigrationMapper {

    @Select("select * from dm_department where dept_id = #{deptId} and del_flag = '0' for update")
    DepartmentConfig selectConfigForUpdate(@Param("deptId") Long deptId);

    @Select("select count(1) from dm_department where dept_id = #{deptId}")
    long countAnyConfig(@Param("deptId") Long deptId);

    /**
     * 检查迁移到目标科室后可能触发的业务唯一键冲突。
     * 迁移不自动合并重复数据，出现冲突时由管理员先处理目标科室数据。
     */
    @Results({
        @Result(property = "dataName", column = "data_name"),
        @Result(property = "conflictCount", column = "conflict_count")
    })
    @Select({
        "select data_name, conflict_count from (",
        "select '日报' as data_name, count(1) as conflict_count",
        "from dm_daily_report source join dm_daily_report target on target.dept_id = #{targetDeptId}",
        "and target.report_date = source.report_date and target.user_id = source.user_id",
        "and target.del_flag = source.del_flag where source.dept_id = #{sourceDeptId}",
        "union all",
        "select '日报日期例外' as data_name, count(1) as conflict_count",
        "from dm_daily_calendar_override source join dm_daily_calendar_override target on target.dept_id = #{targetDeptId}",
        "and target.calendar_date = source.calendar_date and target.del_flag = source.del_flag",
        "and (target.user_id = source.user_id or (target.user_id is null and source.user_id is null))",
        "where source.dept_id = #{sourceDeptId}",
        "union all",
        "select '资料分类' as data_name, count(1) as conflict_count",
        "from dm_department_document_category source join dm_department_document_category target on target.dept_id = #{targetDeptId}",
        "and target.parent_id = source.parent_id and target.category_name = source.category_name",
        "and target.del_flag = source.del_flag where source.dept_id = #{sourceDeptId}",
        "union all",
        "select '科室项目' as data_name, count(1) as conflict_count",
        "from dm_department_project source join dm_department_project target on target.dept_id = #{targetDeptId}",
        "and target.project_name = source.project_name and target.del_flag = source.del_flag",
        "where source.dept_id = #{sourceDeptId}",
        "union all",
        "select '审核规则' as data_name, count(1) as conflict_count",
        "from dm_review_rule source join dm_review_rule target on target.dept_id = #{targetDeptId}",
        "and target.task_type = source.task_type and target.del_flag = source.del_flag",
        "where source.dept_id = #{sourceDeptId}",
        ") conflicts where conflict_count > 0"
    })
    List<DepartmentConfigMigrationConflictVo> selectConflicts(@Param("sourceDeptId") Long sourceDeptId,
                                                               @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_department set dept_id = #{targetDeptId}, update_by = #{operatorId}, update_time = #{updateTime}, version = coalesce(version, 0) + 1 "
        + "where dept_id = #{sourceDeptId} and del_flag = '0'")
    int moveConfig(@Param("sourceDeptId") Long sourceDeptId,
                   @Param("targetDeptId") Long targetDeptId,
                   @Param("operatorId") Long operatorId,
                   @Param("updateTime") LocalDateTime updateTime);

    @Update("update dm_person_profile set create_dept = #{targetDeptId} where create_dept = #{sourceDeptId}")
    int movePersonProfiles(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_person_profile_event set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int movePersonProfileEvents(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_daily_report set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveDailyReports(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_daily_calendar_override set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveDailyCalendarOverrides(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_daily_leave set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveDailyLeaves(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_department_document_category set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveDocumentCategories(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_department_document set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveDocuments(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_work_order set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveWorkOrders(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_department_project set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveProjects(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_operation_record set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveOperationRecords(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_operation_system set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveOperationSystems(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_five_why set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveFiveWhy(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_score_proposal set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveScoreProposals(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_score_proposal_review_task set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveScoreProposalReviewTasks(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_review_rule set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveReviewRules(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_department_task_rule set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveTaskRules(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_department_task_assignment set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveTaskAssignments(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    @Update("update dm_department_task_instance set dept_id = #{targetDeptId} where dept_id = #{sourceDeptId}")
    int moveTaskInstances(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);
}
