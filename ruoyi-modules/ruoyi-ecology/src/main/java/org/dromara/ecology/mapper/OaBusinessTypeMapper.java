package org.dromara.ecology.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaBusinessType;
import org.dromara.ecology.domain.vo.OaBusinessTypeVo;

/** 泛微审批业务类型配置数据层。 */
@Mapper
public interface OaBusinessTypeMapper extends BaseMapperPlus<OaBusinessType, OaBusinessTypeVo> {
}
