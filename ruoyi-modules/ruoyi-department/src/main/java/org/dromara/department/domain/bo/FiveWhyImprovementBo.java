package org.dromara.department.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 5WHY改善措施。 */
@Data
public class FiveWhyImprovementBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String kind;

    @Size(max = 2000, message = "改善措施不能超过2000个字符")
    private String measure;

    @Size(max = 200, message = "责任人不能超过200个字符")
    private String responsible;

    private LocalDate expectedDate;
}
