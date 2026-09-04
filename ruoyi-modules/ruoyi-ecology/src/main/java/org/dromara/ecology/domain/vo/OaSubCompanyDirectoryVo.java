package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微 HRM 分部目录项。 */
@Data
public class OaSubCompanyDirectoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String oaId;
    private String oaCode;
    private String oaName;
    private String oaFullName;
    private String oaParentId;
    private String canceled;
    private String created;
    private String modified;
    private String showOrder;
}
