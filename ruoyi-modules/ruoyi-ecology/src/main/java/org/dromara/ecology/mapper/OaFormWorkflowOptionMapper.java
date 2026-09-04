package org.dromara.ecology.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaFormWorkflowOption;
import org.dromara.ecology.domain.vo.OaWorkflowOptionVo;

@Mapper
public interface OaFormWorkflowOptionMapper extends BaseMapperPlus<OaFormWorkflowOption, OaWorkflowOptionVo> {
}
