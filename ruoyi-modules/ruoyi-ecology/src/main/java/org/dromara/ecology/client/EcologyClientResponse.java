package org.dromara.ecology.client;

import lombok.Data;

/** 泛微接口统一响应。 */
@Data
public class EcologyClientResponse {

    private boolean success;

    private String code;

    private String requestId;

    private String status;

    private String message;

    /** 脱敏后的接口响应摘要，供事件日志使用。 */
    private String rawBody;
}
