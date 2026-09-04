package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 通用导入批次的分组视图；一个分组对应一份泛微申请。 */
@Data
public class OaImportGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String groupKey;
    private String groupName;
    private Integer recordCount;
    private Integer skippedCount;
    private Integer applicationCount;
    private Long applicationId;
    private Long attachmentOssId;
    private String status;
    private String errorMessage;
    private String skipReason;
}
