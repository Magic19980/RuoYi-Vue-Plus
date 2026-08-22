package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 日报查询参数。
 */
@Data
public class DailyReportQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private LocalDate reportDate;

    private LocalDate beginDate;

    private LocalDate endDate;

    private Long userId;

    private String userName;

    private String status;
}
