package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.PersonProfileEvent;

/** 人员服务关系历史事件数据层。 */
@Mapper
public interface PersonProfileEventMapper extends BaseMapperPlus<PersonProfileEvent, PersonProfileEvent> {
}
