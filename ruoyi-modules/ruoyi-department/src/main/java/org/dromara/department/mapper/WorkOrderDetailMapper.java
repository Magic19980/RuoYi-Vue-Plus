package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.WorkOrderDetail;
import org.dromara.department.domain.vo.WorkOrderDetailVo;

import java.util.Collection;
import java.util.List;

/**
 * 人工单 PDF 明细数据层。
 */
@Mapper
public interface WorkOrderDetailMapper extends BaseMapperPlus<WorkOrderDetail, WorkOrderDetailVo> {

    /** 查询指定工单的明细并按序号排序。 */
    default List<WorkOrderDetail> selectByWorkOrderId(Long workOrderId) {
        return selectList(Wrappers.<WorkOrderDetail>lambdaQuery()
            .eq(WorkOrderDetail::getWorkOrderId, workOrderId)
            .orderByAsc(WorkOrderDetail::getSequenceNo)
            .orderByAsc(WorkOrderDetail::getId));
    }

    /** 批量查询多个工单的明细并按工单、序号排序。 */
    default List<WorkOrderDetail> selectByWorkOrderIds(Collection<Long> workOrderIds) {
        if (workOrderIds == null || workOrderIds.isEmpty()) {
            return List.of();
        }
        return selectList(Wrappers.<WorkOrderDetail>lambdaQuery()
            .in(WorkOrderDetail::getWorkOrderId, workOrderIds)
            .orderByAsc(WorkOrderDetail::getWorkOrderId)
            .orderByAsc(WorkOrderDetail::getSequenceNo)
            .orderByAsc(WorkOrderDetail::getId));
    }

    /** 删除指定工单的全部明细。 */
    default int deleteByWorkOrderId(Long workOrderId) {
        return delete(Wrappers.<WorkOrderDetail>lambdaUpdate().eq(WorkOrderDetail::getWorkOrderId, workOrderId));
    }
}
