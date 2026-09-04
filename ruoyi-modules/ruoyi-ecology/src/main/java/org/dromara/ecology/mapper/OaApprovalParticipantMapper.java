package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaApprovalParticipant;
import org.dromara.ecology.domain.vo.OaApprovalParticipantVo;

import java.util.List;

/** 泛微动态审批人数据层。 */
@Mapper
public interface OaApprovalParticipantMapper extends BaseMapperPlus<OaApprovalParticipant, OaApprovalParticipantVo> {

    default List<OaApprovalParticipant> selectByApplicationId(Long applicationId) {
        return selectList(Wrappers.<OaApprovalParticipant>lambdaQuery()
            .eq(OaApprovalParticipant::getApplicationId, applicationId)
            .orderByAsc(OaApprovalParticipant::getStageCode)
            .orderByAsc(OaApprovalParticipant::getSortNo)
            .orderByAsc(OaApprovalParticipant::getId));
    }

    default int deleteByApplicationId(Long applicationId) {
        return delete(Wrappers.<OaApprovalParticipant>lambdaUpdate()
            .eq(OaApprovalParticipant::getApplicationId, applicationId));
    }
}
