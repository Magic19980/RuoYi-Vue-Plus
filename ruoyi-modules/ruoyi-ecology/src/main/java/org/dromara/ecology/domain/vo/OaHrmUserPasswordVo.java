package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微 HRM 新人员初始密码配置状态。 */
@Data
public class OaHrmUserPasswordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否已经配置初始密码。 */
    private boolean configured;

    /** 配置来源：PAGE、ENV 或 NONE。 */
    private String source;

    /** 当前初始密码，仅向拥有泛微人员同步权限的管理员返回。 */
    private String password;
}
