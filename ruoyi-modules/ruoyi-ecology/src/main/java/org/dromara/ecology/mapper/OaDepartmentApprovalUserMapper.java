package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaDepartmentApprovalUser;
import org.dromara.ecology.domain.vo.OaDepartmentApprovalUserVo;

import java.util.List;

/** 泛微审批部门配置用户数据层。 */
@Mapper
public interface OaDepartmentApprovalUserMapper extends BaseMapperPlus<OaDepartmentApprovalUser, OaDepartmentApprovalUserVo> {

    default List<OaDepartmentApprovalUser> selectByApprovalId(Long approvalId) {
        return selectList(Wrappers.<OaDepartmentApprovalUser>lambdaQuery()
            .eq(OaDepartmentApprovalUser::getApprovalId, approvalId)
            .orderByAsc(OaDepartmentApprovalUser::getStageCode)
            .orderByAsc(OaDepartmentApprovalUser::getParticipantRole)
            .orderByAsc(OaDepartmentApprovalUser::getSortNo)
            .orderByAsc(OaDepartmentApprovalUser::getId));
    }

    default void deleteByApprovalId(Long approvalId) {
        delete(Wrappers.<OaDepartmentApprovalUser>lambdaUpdate()
            .eq(OaDepartmentApprovalUser::getApprovalId, approvalId));
    }
}
