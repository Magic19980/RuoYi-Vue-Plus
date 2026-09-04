package org.dromara.ecology.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/** 通用导入批次部门映射。 */
@Data
public class OaImportDeptMappingBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, Long> mappings;

    /** 仅对当前导入批次生效的来源组织跳过原因。 */
    private Map<String, String> skippedDeptReasons;
}
