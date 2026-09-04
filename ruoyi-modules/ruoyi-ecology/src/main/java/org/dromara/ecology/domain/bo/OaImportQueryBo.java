package org.dromara.ecology.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 通用导入批次查询参数。 */
@Data
public class OaImportQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long configId;

    private String businessType;

    private String batchNo;

    private String status;

    private String keyword;
}
