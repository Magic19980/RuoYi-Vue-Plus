package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentReviewRule;
import org.dromara.department.domain.vo.DepartmentReviewRuleVo;

/** 审核人配置数据层。 */
@Mapper
public interface DepartmentReviewRuleMapper extends BaseMapperPlus<DepartmentReviewRule, DepartmentReviewRuleVo> {

    /**
     * 查询指定科室和业务类型当前生效的审核规则。
     *
     * @param deptId   业务科室主键
     * @param taskType 业务任务类型
     * @return 当前生效的审核规则
     */
    default DepartmentReviewRule selectEnabledRule(Long deptId, String taskType) {
        return selectOne(Wrappers.<DepartmentReviewRule>lambdaQuery()
            .eq(DepartmentReviewRule::getDeptId, deptId)
            .eq(DepartmentReviewRule::getTaskType, taskType)
            .eq(DepartmentReviewRule::getStatus, "ENABLED")
            .eq(DepartmentReviewRule::getDelFlag, "0")
            .and(wrapper -> wrapper.isNull(DepartmentReviewRule::getEffectiveStart).or().le(DepartmentReviewRule::getEffectiveStart, java.time.LocalDate.now()))
            .and(wrapper -> wrapper.isNull(DepartmentReviewRule::getEffectiveEnd).or().ge(DepartmentReviewRule::getEffectiveEnd, java.time.LocalDate.now()))
            .last("limit 1"));
    }
}
