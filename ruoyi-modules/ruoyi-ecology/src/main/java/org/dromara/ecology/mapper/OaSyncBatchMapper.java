package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaSyncBatch;
import org.dromara.ecology.domain.vo.OaSyncBatchVo;

import java.util.List;

/** 泛微 HRM 同步批次数据层。 */
@Mapper
public interface OaSyncBatchMapper extends BaseMapperPlus<OaSyncBatch, OaSyncBatchVo> {

    default OaSyncBatch selectLastSuccess(String syncType) {
        return selectOne(Wrappers.<OaSyncBatch>lambdaQuery()
            .eq(OaSyncBatch::getSyncType, syncType)
            .in(OaSyncBatch::getStatus, List.of("SUCCESS", "PARTIAL"))
            .orderByDesc(OaSyncBatch::getFinishedAt)
            .last("limit 1"));
    }
}
