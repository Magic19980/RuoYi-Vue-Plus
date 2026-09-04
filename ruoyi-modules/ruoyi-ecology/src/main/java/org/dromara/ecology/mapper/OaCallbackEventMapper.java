package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaCallbackEvent;

/** 泛微回调事件数据层。 */
@Mapper
public interface OaCallbackEventMapper extends BaseMapperPlus<OaCallbackEvent, OaCallbackEvent> {

    default OaCallbackEvent selectByEventKey(String eventKey) {
        return selectOne(Wrappers.<OaCallbackEvent>lambdaQuery()
            .eq(OaCallbackEvent::getEventKey, eventKey)
            .last("limit 1"));
    }
}
