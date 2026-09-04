package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaDepartmentApproval;
import org.dromara.ecology.domain.vo.OaDepartmentApprovalVo;

import java.util.List;

/** 泛微审批部门配置数据层。 */
@Mapper
public interface OaDepartmentApprovalMapper extends BaseMapperPlus<OaDepartmentApproval, OaDepartmentApprovalVo> {

    default List<OaDepartmentApproval> selectEnabledByApplication(Long workflowConfigId, String businessType) {
        return selectList(Wrappers.<OaDepartmentApproval>lambdaQuery()
            .eq(OaDepartmentApproval::getWorkflowConfigId, workflowConfigId)
            .eq(OaDepartmentApproval::getBusinessType, businessType)
            .eq(OaDepartmentApproval::getStatus, "ENABLED")
            .orderByAsc(OaDepartmentApproval::getId));
    }
}
