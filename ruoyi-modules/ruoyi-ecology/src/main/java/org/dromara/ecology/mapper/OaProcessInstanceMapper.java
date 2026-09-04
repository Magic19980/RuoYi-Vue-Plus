package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaProcessInstance;

import java.time.LocalDateTime;
import java.util.List;

/** 泛微审批实例数据层。 */
@Mapper
public interface OaProcessInstanceMapper extends BaseMapperPlus<OaProcessInstance, OaProcessInstance> {

    default OaProcessInstance selectByApplicationId(Long applicationId) {
        return selectOne(Wrappers.<OaProcessInstance>lambdaQuery()
            .eq(OaProcessInstance::getApplicationId, applicationId)
            .orderByDesc(OaProcessInstance::getId)
            .last("limit 1"));
    }

    default OaProcessInstance selectByOaRequestId(String oaRequestId) {
        return selectOne(Wrappers.<OaProcessInstance>lambdaQuery()
            .eq(OaProcessInstance::getOaRequestId, oaRequestId)
            .orderByDesc(OaProcessInstance::getId)
            .last("limit 1"));
    }

    default long countByWorkflowConfigId(Long workflowConfigId) {
        return selectCount(Wrappers.<OaProcessInstance>lambdaQuery()
            .eq(OaProcessInstance::getWorkflowConfigId, workflowConfigId));
    }

    default List<OaProcessInstance> selectDueForReconcile(LocalDateTime before, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return selectList(Wrappers.<OaProcessInstance>lambdaQuery()
            .in(OaProcessInstance::getLocalStatus, "SUBMITTING", "IN_PROGRESS", "UNKNOWN")
            .and(wrapper -> wrapper.isNull(OaProcessInstance::getLastSyncAt)
                .or().le(OaProcessInstance::getLastSyncAt, before))
            .orderByAsc(OaProcessInstance::getLastSyncAt)
            .last("limit " + safeLimit));
    }
}
