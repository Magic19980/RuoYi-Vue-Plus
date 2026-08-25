package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentTaskRule;
import org.dromara.department.domain.vo.DepartmentTaskRuleVo;

/** 周期任务规则数据层。 */
@Mapper
public interface DepartmentTaskRuleMapper extends BaseMapperPlus<DepartmentTaskRule, DepartmentTaskRuleVo> {

    default java.util.List<DepartmentTaskRule> selectEnabledRules() {
        return selectList(Wrappers.<DepartmentTaskRule>lambdaQuery()
            .eq(DepartmentTaskRule::getStatus, "ENABLED")
            .eq(DepartmentTaskRule::getDelFlag, "0"));
    }

    @Select("select count(1) from dm_department_task_assignment where rule_id = #{ruleId} and status = 'ENABLED' and del_flag = '0'")
    long countAssignments(@Param("ruleId") Long ruleId);
}
