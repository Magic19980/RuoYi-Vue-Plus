package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 周报查询参数。
 */
@Data
public class WeeklyReportQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate beginDate;

    private LocalDate endDate;

    private String status;
}
