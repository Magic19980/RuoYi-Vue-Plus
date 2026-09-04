package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 通用“导入数据到泛微”业务模板配置。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_import_business_config")
public class OaImportBusinessConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String businessType;

    private String businessName;

    private String sheetName;

    private Integer headerRow;

    /** Excel 列定义：[{"code":"dept","header":"部门","type":"TEXT","required":true}] */
    private String fieldDefinitionsJson;

    /** 提交时补充的参数定义：[{"code":"period","label":"期间","type":"TEXT","required":true}] */
    private String parameterDefinitionsJson;

    /** 分组字段编码数组；为空表示整个批次只生成一条 OA。 */
    private String groupByJson;

    /** 用于业务归属组织匹配的字段编码，可为空。 */
    private String deptField;

    /** 用于公司匹配的字段编码，可为空。 */
    private String companyField;

    /** 聚合规则：{"totalAmount":{"source":"amount","operation":"SUM"}} */
    private String aggregationJson;

    /** 业务字段到泛微字段的映射：{"amount":"field_100"} */
    private String formMappingJson;

    /** 附件生成配置，包括表头、模板 OSS、标题和文件名规则。 */
    private String attachmentConfigJson;

    private String requestNameTemplate;

    private String contentTemplate;

    private Long defaultWorkflowConfigId;

    /** 默认审批方案；为空时提交页面可选择或自动匹配。 */
    private Long defaultApprovalPlanId;

    /** AUTO_RULE 自动匹配 / PLAN 选择方案 / MANUAL 本次临时指定。 */
    private String defaultApprovalMode;

    private String status;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
