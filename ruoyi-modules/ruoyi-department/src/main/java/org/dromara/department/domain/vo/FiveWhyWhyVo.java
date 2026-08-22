package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 5WHY问题与原因视图。 */
@Data
public class FiveWhyWhyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer level;
    private String question;
    private String cause;
}
