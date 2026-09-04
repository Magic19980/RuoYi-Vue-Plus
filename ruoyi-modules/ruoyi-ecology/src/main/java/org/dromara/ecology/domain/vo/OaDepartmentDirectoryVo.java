package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微 HRM 部门目录项。 */
@Data
public class OaDepartmentDirectoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String oaId;
    private String oaCode;
    private String oaName;
    private String oaMark;
    private String oaSubCompanyId;
    private String oaParentId;
    private String canceled;
    private String created;
    private String modified;
    private String showOrder;
}
