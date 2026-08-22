package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.WorkOrderImportBatch;
import org.dromara.department.domain.vo.WorkOrderImportBatchVo;

/**
 * 工单PDF导入批次数据层。
 */
@Mapper
public interface WorkOrderImportBatchMapper extends BaseMapperPlus<WorkOrderImportBatch, WorkOrderImportBatchVo> {
}
