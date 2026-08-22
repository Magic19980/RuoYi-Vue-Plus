package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 工单PDF导入结果。
 */
@Data
public class WorkOrderImportResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private WorkOrderImportBatchVo batch;

    private String message;
}
