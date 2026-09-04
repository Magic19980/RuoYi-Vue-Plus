package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 当前导入批次的来源组织映射项。 */
@Data
public class OaImportDeptMappingItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sourceDeptName;

    private Long targetDeptId;

    private String targetDeptName;

    private String status;

    private Integer recordCount;

    private String skipReason;
}
