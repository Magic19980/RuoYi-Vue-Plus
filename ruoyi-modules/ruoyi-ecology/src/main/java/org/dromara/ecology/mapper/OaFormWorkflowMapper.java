package org.dromara.ecology.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaFormWorkflow;
import org.dromara.ecology.domain.vo.OaFormWorkflowVo;

@Mapper
public interface OaFormWorkflowMapper extends BaseMapperPlus<OaFormWorkflow, OaFormWorkflowVo> {
}
