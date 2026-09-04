package org.dromara.ecology.service;

import org.dromara.ecology.domain.OaApplication;
import org.dromara.ecology.domain.bo.OaApprovalParticipantBo;
import org.dromara.ecology.domain.bo.OaDepartmentApprovalBo;
import org.dromara.ecology.domain.vo.OaDepartmentApprovalVo;

import java.util.List;

/** 泛微审批方案服务。 */
public interface IOaDepartmentApprovalService {

    List<OaDepartmentApprovalVo> queryList(Long workflowConfigId, String businessType, String sourceModule,
                                            Long businessDeptId, boolean enabledOnly);

    OaDepartmentApprovalVo queryById(Long id);

    Boolean insertByBo(OaDepartmentApprovalBo bo);

    Boolean updateByBo(OaDepartmentApprovalBo bo);

    Boolean deleteById(Long id);

    /** 是否存在与申请上下文匹配的审批方案。 */
    boolean hasMatchingConfig(OaApplication application);

    /** 将审批方案按维护顺序转换为申请审批人快照。 */
    List<OaApprovalParticipantBo> resolve(OaApplication application);

    /** 按通用导入批次的结算部门解析部门审批方案；不会使用全组织通用方案兜底。 */
    OaDepartmentApprovalVo resolveForImport(String businessType, String sourceModule,
                                             Long businessDeptId, String formDataJson);
}
