package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 工单台账查询参数。
 */
@Data
public class WorkOrderQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate beginDate;

    private LocalDate endDate;

    private String ticketNo;

    private String systemName;

    private String faultType;

    private String sourceType;

    private String keyword;
}
