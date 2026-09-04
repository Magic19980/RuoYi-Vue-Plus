package org.dromara.ecology.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.ecology.callback.EcologyCallbackVerifier;
import org.dromara.ecology.domain.OaCallbackCommand;
import org.dromara.ecology.service.IOaApplicationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/** 泛微审批回调入口。 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/ecology/callback")
public class OaCallbackController {

    private final EcologyCallbackVerifier verifier;
    private final IOaApplicationService applicationService;
    @PostMapping
    public R<String> receive(@RequestHeader Map<String, String> headers, @RequestBody String body) {
        verifier.verify(body, headers);
        Map<String, Object> root = parseBody(body);
        Map<String, Object> data = asMap(root.get("data"));
        String requestId = firstNonBlank(value(data, "requestId"), value(data, "requestid"),
            value(root, "requestId"), value(root, "requestid"));
        String status = firstNonBlank(value(data, "status"), value(data, "requestStatus"),
            value(data, "requeststatus"), value(root, "status"));
        String eventKey = firstNonBlank(value(data, "eventId"), value(data, "eventid"), value(root, "eventId"),
            digest(body));
        if (StringUtils.isBlank(requestId)) {
            throw new ServiceException("泛微回调缺少 requestId");
        }
        applicationService.handleCallback(new OaCallbackCommand(eventKey, requestId, status, body));
        return R.ok("accepted");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }

    private Map<String, Object> parseBody(String body) {
        try {
            Map<String, Object> result = JsonUtils.parseMap(body);
            if (result == null || result.isEmpty()) {
                throw new ServiceException("泛微回调内容为空");
            }
            return result;
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("泛微回调内容不是合法 JSON：" + ex.getMessage());
        }
    }

    private String value(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String digest(String body) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest((body == null ? "" : body).getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                result.append(String.format("%02x", item));
            }
            return result.toString();
        } catch (Exception ex) {
            throw new ServiceException("泛微回调事件号生成失败：" + ex.getMessage());
        }
    }
}
