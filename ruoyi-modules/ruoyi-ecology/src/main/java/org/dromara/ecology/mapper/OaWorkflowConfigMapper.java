package org.dromara.ecology.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaWorkflowConfig;
import org.dromara.ecology.domain.vo.OaWorkflowConfigVo;

/** 泛微流程配置数据层。 */
@Mapper
public interface OaWorkflowConfigMapper extends BaseMapperPlus<OaWorkflowConfig, OaWorkflowConfigVo> {
}
