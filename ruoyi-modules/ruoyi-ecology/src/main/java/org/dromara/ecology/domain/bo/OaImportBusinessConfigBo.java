package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/** 通用导入业务模板配置参数。复杂规则以 JSON 存储，但仅供管理员配置。 */
@Data
public class OaImportBusinessConfigBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "业务模板主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "业务类型不能为空")
    @Size(max = 64, message = "业务类型不能超过64个字符")
    private String businessType;

    @NotBlank(message = "业务名称不能为空")
    @Size(max = 100, message = "业务名称不能超过100个字符")
    private String businessName;

    @Size(max = 100, message = "工作表名称不能超过100个字符")
    private String sheetName;

    @NotNull(message = "表头行不能为空")
    @Min(value = 0, message = "表头行不能小于0")
    @Max(value = 100, message = "表头行不能大于100")
    private Integer headerRow = 0;

    @Size(max = 20000, message = "Excel字段定义不能超过20000个字符")
    private String fieldDefinitionsJson;

    @Size(max = 10000, message = "参数定义不能超过10000个字符")
    private String parameterDefinitionsJson;

    @Size(max = 5000, message = "分组规则不能超过5000个字符")
    private String groupByJson;

    @Size(max = 64, message = "业务归属组织字段不能超过64个字符")
    private String deptField;

    @Size(max = 64, message = "公司字段不能超过64个字符")
    private String companyField;

    @Size(max = 10000, message = "聚合规则不能超过10000个字符")
    private String aggregationJson;

    @Size(max = 10000, message = "OA表单映射不能超过10000个字符")
    private String formMappingJson;

    @Size(max = 20000, message = "附件配置不能超过20000个字符")
    private String attachmentConfigJson;

    @Size(max = 200, message = "申请名称模板不能超过200个字符")
    private String requestNameTemplate;

    @Size(max = 5000, message = "申请内容模板不能超过5000个字符")
    private String contentTemplate;

    private Long defaultWorkflowConfigId;

    private Long defaultApprovalPlanId;

    @Size(max = 20, message = "默认审批方式不能超过20个字符")
    private String defaultApprovalMode = "AUTO_RULE";

    private String status;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;
}
