package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/** 通用导入明细视图。 */
@Data
public class OaImportRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer rowNo;
    private Map<String, Object> data;
    private String groupKey;
    private String groupName;
    private Long deptId;
    private String deptName;
    private Long companyId;
    private Long applicationId;
    private Long attachmentOssId;
    private String status;
    private String errorMessage;
    private String skipReason;
}
