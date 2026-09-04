package org.dromara.ecology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ecology.domain.OaFormWorkflowOption;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = OaFormWorkflowOption.class)
public class OaWorkflowOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String optionCode;
    private String optionName;
    private String processType;
    private String participantMappingJson;
    private Integer sortNo;
    private String status;
    private String remark;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
