package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微 HRM 岗位目录项。 */
@Data
public class OaJobTitleDirectoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String oaId;
    private String oaName;
    private String oaMark;
    private String oaRemark;
    private String created;
    private String modified;
}
