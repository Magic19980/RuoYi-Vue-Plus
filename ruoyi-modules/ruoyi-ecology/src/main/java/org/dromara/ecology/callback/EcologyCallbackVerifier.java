package org.dromara.ecology.callback;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.ecology.config.EcologyProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;

/**
 * 泛微回调验签边界。
 *
 * <p>当前实现采用 HMAC-SHA256(timestamp + "\\n" + nonce + "\\n" + body)。
 * System-boxplusn 未提供回调验签实现，若现场 Ecology 使用厂商规定的 RSA/MD5 规范，
 * 只需替换本类，不要把验签逻辑下沉到业务服务。</p>
 */
@Component
@RequiredArgsConstructor
public class EcologyCallbackVerifier {

    private final EcologyProperties properties;

    public void verify(String body, Map<String, String> headers) {
        if (!properties.isCallbackEnabled()) {
            throw new ServiceException("泛微回调未启用");
        }
        if (StringUtils.isBlank(properties.getCallbackSecret())) {
            throw new ServiceException("泛微回调密钥未配置");
        }
        String signature = header(headers, properties.getCallbackSignatureHeader());
        String timestamp = header(headers, properties.getCallbackTimestampHeader());
        String nonce = header(headers, properties.getCallbackNonceHeader());
        if (StringUtils.isBlank(signature) || StringUtils.isBlank(timestamp) || StringUtils.isBlank(nonce)) {
            throw new ServiceException("泛微回调签名请求头不完整");
        }
        long timestampValue;
        try {
            timestampValue = Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            throw new ServiceException("泛微回调时间戳无效");
        }
        long tolerance = Math.max(30, properties.getCallbackTimestampToleranceSeconds());
        if (Math.abs(Instant.now().getEpochSecond() - timestampValue) > tolerance) {
            throw new ServiceException("泛微回调已超过允许时间窗口");
        }
        String expected = hmac(timestamp + "\n" + nonce + "\n" + (body == null ? "" : body));
        String supplied = signature.startsWith("sha256=") ? signature.substring(7) : signature;
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            throw new ServiceException("泛微回调签名校验失败");
        }
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getCallbackSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                result.append(String.format("%02x", item));
            }
            return result.toString();
        } catch (Exception ex) {
            throw new ServiceException("泛微回调签名计算失败：" + ex.getMessage());
        }
    }

    private String header(Map<String, String> headers, String name) {
        if (headers == null || StringUtils.isBlank(name)) {
            return null;
        }
        String direct = headers.get(name);
        if (StringUtils.isNotBlank(direct)) {
            return direct;
        }
        return headers.entrySet().stream()
            .filter(entry -> name.equalsIgnoreCase(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }
}
