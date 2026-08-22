package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 5WHY改善措施视图。 */
@Data
public class FiveWhyImprovementVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String kind;
    private String measure;
    private String responsible;
    private LocalDate expectedDate;
}
