package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 运维工作记录查询参数。
 */
@Data
public class OperationRecordQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate beginDate;

    private LocalDate endDate;

    private String customerUnit;

    private Long projectId;

    private String systemName;

    private String processStatus;

    private String processMethod;

    private String keyword;
}
