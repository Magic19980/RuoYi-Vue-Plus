package org.dromara.ecology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ecology.domain.OaWorkflowConfig;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 泛微流程配置视图。 */
@Data
@AutoMapper(target = OaWorkflowConfig.class)
public class OaWorkflowConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long formId;
    private String workflowId;
    private String workflowName;
    private String formName;
    private String approvalCode;
    private String approvalName;
    private Boolean isDefault;
    private String processType;
    private String participantMappingJson;
    private String sourceWorkflowName;
    private String requestNameTemplate;
    private String fieldMappingJson;
    private String specificFieldMappingJson;
    private String fieldSchemaJson;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
