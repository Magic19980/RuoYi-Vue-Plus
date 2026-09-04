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

/** 可复用的泛微表单配置。通用字段由公共配置承载，表单差异字段由本表维护。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_form_workflow")
public class OaFormWorkflow extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String workflowId;

    private String formName;

    private String requestNameTemplate;

    private String fieldMappingJson;

    /** 当前表单专属字段映射 JSON，键为业务字段，值为泛微字段编码。 */
    private String specificFieldMappingJson;

    /** 表单字段定义及泛微字段映射，供管理员维护和申请页动态渲染。 */
    private String fieldSchemaJson;

    private String status;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
