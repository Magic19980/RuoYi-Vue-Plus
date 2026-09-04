package org.dromara.ecology.service;

import org.dromara.ecology.domain.bo.OaBusinessTypeBo;
import org.dromara.ecology.domain.vo.OaBusinessTypeVo;

import java.util.List;

/** 泛微审批业务类型配置服务。 */
public interface IOaBusinessTypeService {

    List<OaBusinessTypeVo> queryList(String keyword, boolean enabledOnly);

    OaBusinessTypeVo queryById(Long id);

    Boolean insertByBo(OaBusinessTypeBo bo);

    Boolean updateByBo(OaBusinessTypeBo bo);

    Boolean disableById(Long id);

    /** 校验业务类型可用于新审批配置，并返回规范化后的标识。 */
    String requireEnabled(String businessType);

    /** 校验业务类型可用，并返回主数据，供其他配置保存时带出规范名称。 */
    OaBusinessTypeVo requireEnabledConfig(String businessType);
}
