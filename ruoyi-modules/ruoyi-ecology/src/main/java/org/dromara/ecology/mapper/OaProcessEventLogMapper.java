package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaProcessEventLog;

import java.util.List;

/** 泛微审批事件日志数据层。 */
@Mapper
public interface OaProcessEventLogMapper extends BaseMapperPlus<OaProcessEventLog, OaProcessEventLog> {

    default List<OaProcessEventLog> selectByProcessId(Long processId) {
        return selectList(Wrappers.<OaProcessEventLog>lambdaQuery()
            .eq(OaProcessEventLog::getProcessId, processId)
            .orderByAsc(OaProcessEventLog::getCreateTime)
            .orderByAsc(OaProcessEventLog::getId));
    }
}
