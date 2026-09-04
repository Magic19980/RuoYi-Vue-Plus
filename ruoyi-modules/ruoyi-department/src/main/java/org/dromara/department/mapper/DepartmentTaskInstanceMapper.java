package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentTaskInstance;
import org.dromara.department.domain.vo.DepartmentTaskInstanceVo;

import java.time.LocalDate;

/** 任务周期实例数据层。 */
@Mapper
public interface DepartmentTaskInstanceMapper extends BaseMapperPlus<DepartmentTaskInstance, DepartmentTaskInstanceVo> {

    /** 查询指定规则、用户和周期的有效任务实例。 */
    default DepartmentTaskInstance selectActive(Long ruleId, Long userId, LocalDate periodStart) {
        return selectOne(Wrappers.<DepartmentTaskInstance>lambdaQuery()
            .eq(DepartmentTaskInstance::getRuleId, ruleId)
            .eq(DepartmentTaskInstance::getUserId, userId)
            .eq(DepartmentTaskInstance::getPeriodStart, periodStart)
            .eq(DepartmentTaskInstance::getDelFlag, "0"));
    }

    /** 统计指定规则、用户和周期的任务实例数量。 */
    @Select("select count(1) from dm_department_task_instance where rule_id = #{ruleId} and user_id = #{userId} and period_start = #{periodStart} and del_flag = '0'")
    long countActive(@Param("ruleId") Long ruleId, @Param("userId") Long userId, @Param("periodStart") LocalDate periodStart);
}
