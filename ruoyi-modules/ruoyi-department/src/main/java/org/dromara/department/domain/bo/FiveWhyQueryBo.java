package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 5WHY查询参数。 */
@Data
public class FiveWhyQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String analystName;
    private String problemName;
    private String reviewStatus;
    private LocalDate beginDate;
    private LocalDate endDate;
}
