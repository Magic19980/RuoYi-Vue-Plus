package org.dromara.ecology.service;

import org.dromara.ecology.domain.bo.OaBusinessWorkflowBindingBo;
import org.dromara.ecology.domain.vo.OaBusinessWorkflowBindingVo;

import java.util.List;

public interface IOaBusinessWorkflowService {

    List<OaBusinessWorkflowBindingVo> queryList();

    OaBusinessWorkflowBindingVo queryByBusinessType(String businessType);

    Boolean save(String businessType, OaBusinessWorkflowBindingBo bo);

    Boolean delete(String businessType);
}
