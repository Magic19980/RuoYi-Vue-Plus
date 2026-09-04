package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class OaBusinessWorkflowBindingVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String businessType;
    private Long formId;
    private Long defaultOptionId;
    private List<Long> optionIds;
}
