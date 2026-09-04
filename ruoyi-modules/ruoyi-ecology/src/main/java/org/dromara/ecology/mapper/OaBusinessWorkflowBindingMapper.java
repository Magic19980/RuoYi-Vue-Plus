package org.dromara.ecology.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaBusinessWorkflowBinding;
import org.dromara.ecology.domain.vo.OaBusinessWorkflowBindingVo;

@Mapper
public interface OaBusinessWorkflowBindingMapper extends BaseMapperPlus<OaBusinessWorkflowBinding, OaBusinessWorkflowBindingVo> {
}
