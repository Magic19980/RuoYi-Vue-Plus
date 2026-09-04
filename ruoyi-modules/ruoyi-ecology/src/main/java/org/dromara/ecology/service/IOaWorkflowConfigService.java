package org.dromara.ecology.service;

import org.dromara.ecology.domain.OaWorkflowConfig;
import org.dromara.ecology.domain.bo.OaFormWorkflowBo;
import org.dromara.ecology.domain.bo.OaWorkflowOptionBo;
import org.dromara.ecology.domain.vo.OaFormWorkflowVo;
import org.dromara.ecology.domain.vo.OaWorkflowConfigVo;
import org.dromara.ecology.domain.vo.OaWorkflowOptionVo;

import java.util.List;

/** 泛微流程配置服务。 */
public interface IOaWorkflowConfigService {

    List<OaWorkflowConfigVo> queryList(String businessType, boolean enabledOnly);

    List<OaFormWorkflowVo> queryFormList(boolean enabledOnly);

    OaFormWorkflowVo queryFormById(Long id);

    List<OaWorkflowOptionVo> queryOptions(boolean enabledOnly);

    Boolean insertOptionByBo(OaWorkflowOptionBo bo);

    Boolean updateOptionByBo(OaWorkflowOptionBo bo);

    Boolean deleteOptionById(Long id);

    Boolean insertFormByBo(OaFormWorkflowBo bo);

    Boolean updateFormByBo(OaFormWorkflowBo bo);

    Boolean deleteFormById(Long id);

    OaWorkflowConfigVo queryById(Long id);

    /** 按业务类型解析表单与通用审批方式，供历史申请详情展示。 */
    OaWorkflowConfigVo queryById(Long id, String businessType);

    OaWorkflowConfig requireEnabled(Long id, String businessType);
}
