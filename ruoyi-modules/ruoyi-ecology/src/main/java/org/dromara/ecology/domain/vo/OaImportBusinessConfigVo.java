package org.dromara.ecology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ecology.domain.OaImportBusinessConfig;

import java.io.Serial;
import java.io.Serializable;

/** 通用导入业务模板配置视图。 */
@Data
@AutoMapper(target = OaImportBusinessConfig.class)
public class OaImportBusinessConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String businessType;
    private String businessName;
    private String sheetName;
    private Integer headerRow;
    private String fieldDefinitionsJson;
    private String parameterDefinitionsJson;
    private String groupByJson;
    private String deptField;
    private String companyField;
    private String aggregationJson;
    private String formMappingJson;
    private String attachmentConfigJson;
    private String requestNameTemplate;
    private String contentTemplate;
    private Long defaultWorkflowConfigId;
    private Long defaultApprovalPlanId;
    private String defaultApprovalMode;
    private String status;
    private String remark;
}
