package org.dromara.ecology.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微审批申请查询参数。 */
@Data
public class OaApplicationQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String businessType;

    private String title;

    private String status;

    /** 是否按审批监控权限查询全量申请。 */
    private Boolean monitor;
}
