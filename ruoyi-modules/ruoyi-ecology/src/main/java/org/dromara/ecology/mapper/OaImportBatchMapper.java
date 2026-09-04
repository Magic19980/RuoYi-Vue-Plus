package org.dromara.ecology.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaImportBatch;
import org.dromara.ecology.domain.vo.OaImportBatchVo;

/** 通用导入批次数据层。 */
@Mapper
public interface OaImportBatchMapper extends BaseMapperPlus<OaImportBatch, OaImportBatchVo> {
}
