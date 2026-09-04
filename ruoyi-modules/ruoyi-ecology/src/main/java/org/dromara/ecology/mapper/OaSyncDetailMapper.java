package org.dromara.ecology.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaSyncDetail;
import org.dromara.ecology.domain.vo.OaSyncDetailVo;

/** 泛微 HRM 同步明细数据层。 */
@Mapper
public interface OaSyncDetailMapper extends BaseMapperPlus<OaSyncDetail, OaSyncDetailVo> {
}
