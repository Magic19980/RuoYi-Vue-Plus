package org.dromara.ecology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ecology.domain.OaFormWorkflow;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AutoMapper(target = OaFormWorkflow.class)
public class OaFormWorkflowVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String workflowId;
    private String formName;
    private String requestNameTemplate;
    private String fieldMappingJson;
    private String specificFieldMappingJson;
    private String fieldSchemaJson;
    private String status;
    private String remark;
    private List<OaWorkflowOptionVo> options;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
