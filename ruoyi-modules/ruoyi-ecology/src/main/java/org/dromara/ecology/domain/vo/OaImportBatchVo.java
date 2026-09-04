package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 通用导入批次视图。 */
@Data
public class OaImportBatchVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long configId;
    private String businessType;
    private String businessName;
    private String batchNo;
    private String sourceFileName;
    private String status;
    private Integer totalCount;
    private Integer matchedCount;
    private Integer groupCount;
    private Integer applicationCount;
    private Integer failedCount;
    private Integer skippedCount;
    private String message;
    private List<String> unmatchedDeptNames;
    private List<String> skippedDeptNames;
    private List<OaImportDeptMappingItemVo> mappingItems;
    private List<OaImportGroupVo> groups;
    private List<OaImportRecordVo> records;
}
