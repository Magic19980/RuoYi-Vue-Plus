package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaImportRecord;

import java.util.List;

/** 通用导入明细数据层。 */
@Mapper
public interface OaImportRecordMapper extends BaseMapperPlus<OaImportRecord, OaImportRecord> {

    default List<OaImportRecord> selectByBatchId(Long batchId) {
        return selectList(Wrappers.<OaImportRecord>lambdaQuery()
            .eq(OaImportRecord::getBatchId, batchId)
            .orderByAsc(OaImportRecord::getRowNo)
            .orderByAsc(OaImportRecord::getId));
    }
}
