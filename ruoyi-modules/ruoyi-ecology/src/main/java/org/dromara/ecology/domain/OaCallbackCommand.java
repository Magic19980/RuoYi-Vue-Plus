package org.dromara.ecology.domain;

import java.io.Serial;
import java.io.Serializable;

/** 已完成验签的泛微回调命令。 */
public record OaCallbackCommand(String eventKey, String requestId, String status, String rawBody)
    implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
