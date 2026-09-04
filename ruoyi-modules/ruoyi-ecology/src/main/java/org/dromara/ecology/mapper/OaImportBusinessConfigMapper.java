package org.dromara.ecology.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaImportBusinessConfig;
import org.dromara.ecology.domain.vo.OaImportBusinessConfigVo;

/** 通用导入业务模板数据层。 */
@Mapper
public interface OaImportBusinessConfigMapper extends BaseMapperPlus<OaImportBusinessConfig, OaImportBusinessConfigVo> {
}
