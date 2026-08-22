package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工单PDF导入批次视图。
 */
@Data
public class WorkOrderImportBatchVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String sourceFileName;

    private Long ossId;

    private LocalDate sourcePeriodStart;

    private LocalDate sourcePeriodEnd;

    private Integer pageCount;

    private Integer recordCount;

    private Integer parsedRecordCount;

    private Integer pendingRecordCount;

    private String status;

    private String errorMessage;

    private LocalDateTime createTime;
}
