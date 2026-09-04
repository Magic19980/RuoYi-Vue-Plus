package org.dromara.ecology.client;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.ecology.config.EcologyProperties;
import org.dromara.ecology.domain.vo.OaDepartmentDirectoryVo;
import org.dromara.ecology.domain.vo.OaJobTitleDirectoryVo;
import org.dromara.ecology.domain.vo.OaSubCompanyDirectoryVo;
import org.dromara.ecology.domain.vo.OaUserDirectoryVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 泛微 Ecology REST 客户端。
 *
 * <p>这里只封装鉴权、发起和查询三个稳定边界，业务字段由审批中心按流程配置组装，
 * 避免把旧系统的固定 workflowId 和字段名散落到业务代码中。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EcologyClient {

    private final EcologyProperties properties;

    private volatile TokenCache tokenCache;

    /** 发起泛微流程。 */
    public EcologyClientResponse createRequest(String oaUserId, Map<String, Object> form) {
        return postForm("/api/workflow/paService/doCreateRequest", form, oaUserId);
    }

    /** 查询泛微流程。 */
    public EcologyClientResponse queryRequest(String requestId, String oaUserId) {
        if (StringUtils.isBlank(requestId)) {
            throw new ServiceException("泛微请求编号不能为空");
        }
        String path = StringUtils.isBlank(properties.getRequestStatusPath())
            ? "/api/workflow/paService/getWorkflowRequest" : properties.getRequestStatusPath().trim();
        String separator = path.contains("?") ? "&" : "?";
        return get(path + separator + "requestId=" + requestId, oaUserId);
    }

    /**
     * 获取泛微 HRM 人员列表。
     *
     * <p>这里兼容 System-boxplusn 的 getOaUserInfo/getOaUserList：人员接口使用
     * HRM REST 的 {@code key + ts} MD5 鉴权，不能复用流程接口的 Ecology token。
     * 返回结果已经统一转换为审批中心使用的人员目录结构。</p>
     */
    public PageResult<OaUserDirectoryVo> queryOaUserList(String keyword, PageQuery pageQuery) {
        int pageNum = pageQuery == null || pageQuery.getPageNum() == null || pageQuery.getPageNum() <= 0
            ? PageQuery.DEFAULT_PAGE_NUM : pageQuery.getPageNum();
        int pageSize = pageQuery == null || pageQuery.getPageSize() == null || pageQuery.getPageSize() <= 0
            ? 20 : Math.min(pageQuery.getPageSize(), 1000);
        return queryOaUserList(keyword, pageNum, pageSize, null);
    }

    /** 获取泛微 HRM 人员，支持按修改时间增量查询。 */
    public PageResult<OaUserDirectoryVo> queryOaUserList(String keyword, int pageNum, int pageSize, String modified) {
        ensureHrmReady(properties.getHrmUserListPath(), "人员");
        Map<String, Object> params = new LinkedHashMap<>();
        String search = StringUtils.trim(keyword);
        if (StringUtils.isNotBlank(search)) {
            params.put(normalizeSearchField(properties.getHrmUserSearchField()), search);
        }
        putModified(params, modified);
        HrmPage page = queryHrmPage(properties.getHrmUserListPath(), params, pageNum, pageSize);
        List<OaUserDirectoryVo> users = new ArrayList<>();
        for (Map<?, ?> item : page.items()) {
            users.add(toUserDirectory(item));
        }
        return PageResult.build(users, page.total());
    }

    /** 获取泛微 HRM 分部列表。 */
    public PageResult<OaSubCompanyDirectoryVo> queryOaSubCompanyList(int pageNum, int pageSize, String modified) {
        ensureHrmReady(properties.getHrmSubcompanyListPath(), "分部");
        Map<String, Object> params = new LinkedHashMap<>();
        putModified(params, modified);
        HrmPage page = queryHrmPage(properties.getHrmSubcompanyListPath(), params, pageNum, pageSize);
        List<OaSubCompanyDirectoryVo> rows = new ArrayList<>();
        for (Map<?, ?> item : page.items()) {
            rows.add(toSubCompanyDirectory(item));
        }
        return PageResult.build(rows, page.total());
    }

    /** 获取泛微 HRM 部门列表。 */
    public PageResult<OaDepartmentDirectoryVo> queryOaDepartmentList(int pageNum, int pageSize, String modified) {
        ensureHrmReady(properties.getHrmDepartmentListPath(), "部门");
        Map<String, Object> params = new LinkedHashMap<>();
        putModified(params, modified);
        HrmPage page = queryHrmPage(properties.getHrmDepartmentListPath(), params, pageNum, pageSize);
        List<OaDepartmentDirectoryVo> rows = new ArrayList<>();
        for (Map<?, ?> item : page.items()) {
            rows.add(toDepartmentDirectory(item));
        }
        return PageResult.build(rows, page.total());
    }

    /** 获取泛微 HRM 岗位列表。 */
    public PageResult<OaJobTitleDirectoryVo> queryOaJobTitleList(int pageNum, int pageSize, String modified) {
        ensureHrmReady(properties.getHrmJobtitleListPath(), "岗位");
        Map<String, Object> params = new LinkedHashMap<>();
        putModified(params, modified);
        HrmPage page = queryHrmPage(properties.getHrmJobtitleListPath(), params, pageNum, pageSize);
        List<OaJobTitleDirectoryVo> rows = new ArrayList<>();
        for (Map<?, ?> item : page.items()) {
            rows.add(toJobTitleDirectory(item));
        }
        return PageResult.build(rows, page.total());
    }

    /** 判断客户端是否具备真实调用条件。 */
    public boolean isReady() {
        return properties.isEnabled()
            && StringUtils.isNotBlank(properties.getAddress())
            && StringUtils.isNotBlank(properties.getAppId())
            && StringUtils.isNotBlank(properties.getServerPublicKey())
            && StringUtils.isNotBlank(properties.getServerSecret());
    }

    private EcologyClientResponse postForm(String path, Map<String, Object> form, String oaUserId) {
        return execute(HttpRequest.post(url(path)).form(form), oaUserId);
    }

    private EcologyClientResponse get(String path, String oaUserId) {
        return execute(HttpRequest.get(url(path)), oaUserId);
    }

    private EcologyClientResponse execute(HttpRequest request, String oaUserId) {
        ensureReady();
        if (StringUtils.isBlank(oaUserId)) {
            throw new ServiceException("泛微用户 ID 不能为空");
        }
        String token = getToken();
        String encryptedUserId = encrypt(oaUserId);
        try (HttpResponse response = request
            .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            .header("appid", properties.getAppId())
            .header("token", token)
            .header("userid", encryptedUserId)
            .setConnectionTimeout(properties.getConnectTimeout())
            .setReadTimeout(properties.getReadTimeout())
            .header("X-Request-ID", UUID.randomUUID().toString())
            .execute()) {
            return parseResponse(response.body(), response.getStatus());
        } catch (Exception ex) {
            log.warn("调用泛微接口失败，path={}", request.getUrl(), ex);
            throw new ServiceException("调用泛微接口失败：" + ex.getMessage());
        }
    }

    private String getToken() {
        TokenCache cached = tokenCache;
        if (cached != null && cached.expiredAt() > System.currentTimeMillis()) {
            return cached.value();
        }
        synchronized (this) {
            cached = tokenCache;
            if (cached != null && cached.expiredAt() > System.currentTimeMillis()) {
                return cached.value();
            }
            try (HttpResponse response = HttpRequest.post(url("/api/ec/dev/auth/applytoken"))
                .header("appid", properties.getAppId())
                .header("secret", encrypt(properties.getServerSecret()))
                .header("time", String.valueOf(properties.getTokenTtlSeconds()))
                .setConnectionTimeout(properties.getConnectTimeout())
                .setReadTimeout(properties.getReadTimeout())
                .execute()) {
                String body = response.body();
                if (!response.isOk()) {
                    throw new ServiceException("泛微 token 接口 HTTP " + response.getStatus() + "：" + limit(body));
                }
                Map<String, Object> result = JsonUtils.parseMap(body);
                String token = value(result, "token");
                if (StringUtils.isBlank(token)) {
                    throw new ServiceException("泛微 token 获取失败：" + limit(body));
                }
                long expiresAt = System.currentTimeMillis() + Math.max(60, properties.getTokenTtlSeconds() - 60) * 1000L;
                tokenCache = new TokenCache(token, expiresAt);
                return token;
            } catch (ServiceException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new ServiceException("获取泛微 token 失败：" + ex.getMessage());
            }
        }
    }

    private EcologyClientResponse parseResponse(String body, int httpStatus) {
        EcologyClientResponse response = new EcologyClientResponse();
        response.setRawBody(limit(body));
        try {
            Map<String, Object> root = JsonUtils.parseMap(body);
            Map<String, Object> data = asMap(root.get("data"));
            String code = firstNonBlank(value(root, "code"), value(data, "code"));
            response.setCode(code);
            response.setMessage(firstNonBlank(value(root, "message"), value(root, "msg"),
                value(data, "message"), value(data, "msg")));
            response.setRequestId(firstNonBlank(value(data, "requestid"), value(data, "requestId"), value(root, "requestid")));
            response.setStatus(firstNonBlank(value(data, "status"), value(data, "requeststatus"),
                value(data, "requestStatus"), value(root, "status"), value(root, "requeststatus")));
            boolean meaningfulPayload = StringUtils.isNotBlank(response.getRequestId())
                || StringUtils.isNotBlank(response.getStatus());
            response.setSuccess(httpStatus >= 200 && httpStatus < 300
                && (isSuccessCode(code) || (StringUtils.isBlank(code) && meaningfulPayload)));
            if (!response.isSuccess() && StringUtils.isBlank(response.getMessage())) {
                response.setMessage("泛微接口返回失败，HTTP " + httpStatus);
            }
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setMessage("泛微返回内容不是合法 JSON：" + ex.getMessage());
        }
        return response;
    }

    private String encrypt(String value) {
        try {
            RSA rsa = new RSA(null, properties.getServerPublicKey());
            return rsa.encryptBase64(value, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
        } catch (Exception ex) {
            throw new ServiceException("泛微 RSA 加密失败：" + ex.getMessage());
        }
    }

    private void ensureReady() {
        if (!properties.isEnabled()) {
            throw new ServiceException("泛微审批未启用，请先配置 ecology.enabled=true");
        }
        if (!isReady()) {
            throw new ServiceException("泛微审批配置不完整，请检查地址、appid、公钥和 secret");
        }
    }

    private void ensureHrmReady(String path, String resourceName) {
        if (!properties.isEnabled()) {
            throw new ServiceException("泛微审批未启用，请先配置 ecology.enabled=true");
        }
        if (StringUtils.isBlank(properties.getAddress()) || StringUtils.isBlank(properties.getHrmApplyId())
            || StringUtils.isBlank(path)) {
            throw new ServiceException("泛微 HRM " + resourceName + "接口配置不完整，请检查 address、hrmApplyId 和对应路径配置");
        }
    }

    private HrmPage queryHrmPage(String path, Map<String, Object> filters, int pageNum, int pageSize) {
        ensureHrmReady(path, "组织");
        int normalizedPageNum = pageNum <= 0 ? 1 : pageNum;
        int normalizedPageSize = pageSize <= 0 ? 20 : Math.min(pageSize, 1000);
        Map<String, Object> params = new LinkedHashMap<>();
        if (filters != null) {
            params.putAll(filters);
        }
        // 文档使用 curpage/pagesize，部分已部署版本使用 page/pageNo/pageSize；统一发送兼容参数。
        params.put("curpage", normalizedPageNum);
        params.put("pagesize", normalizedPageSize);
        params.put("page", normalizedPageNum);
        params.put("pageNo", normalizedPageNum);
        params.put("pageSize", normalizedPageSize);
        Map<String, Object> requestBody = Map.of("params", params);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String key = SecureUtil.md5(properties.getHrmApplyId() + timestamp).toUpperCase();
        int retryCount = Math.max(0, Math.min(properties.getHrmRequestRetryCount(), 5));
        Exception last = null;
        for (int attempt = 0; attempt <= retryCount; attempt++) {
            try (HttpResponse response = HttpRequest.post(url(path))
                .header("Content-Type", "application/json;charset=utf-8")
                .header("key", key)
                .header("ts", timestamp)
                .header("X-Request-ID", UUID.randomUUID().toString())
                .setConnectionTimeout(properties.getConnectTimeout())
                .setReadTimeout(properties.getReadTimeout())
                .body(JsonUtils.toJsonString(requestBody))
                .execute()) {
                String body = response.body();
                int status = response.getStatus();
                if (status < 200 || status >= 300) {
                    String errorMessage = "泛微 HRM 接口 HTTP " + status + "：" + limit(body);
                    if (isRetryableStatus(status)) {
                        throw new HrmRetryableException(status, errorMessage,
                            parseRetryAfterMillis(response.header("Retry-After")));
                    }
                    throw new ServiceException(errorMessage);
                }
                return parseHrmPage(body, normalizedPageNum, normalizedPageSize);
            } catch (HrmRetryableException ex) {
                last = ex;
                if (attempt >= retryCount) {
                    throw new ServiceException("泛微 HRM 请求失败，已停止重试：" + ex.getMessage());
                }
                long delay = ex.retryAfterMillis() > 0
                    ? Math.min(ex.retryAfterMillis(), retryMaxDelayMillis())
                    : retryDelayMillis(attempt);
                log.warn("泛微 HRM 请求暂时不可用，第 {} 次重试，path={}, httpStatus={}, delayMs={}",
                    attempt + 1, path, ex.status(), delay);
                sleepBeforeRetry(delay);
            } catch (ServiceException ex) {
                last = ex;
                // 业务错误、鉴权错误和返回格式错误不会因立即重试而恢复，直接终止本次同步。
                throw ex;
            } catch (Exception ex) {
                last = ex;
                if (attempt >= retryCount) {
                    log.warn("调用泛微 HRM 接口失败，path={}", path, ex);
                    throw new ServiceException("获取泛微 HRM 数据失败：" + ex.getMessage());
                }
                long delay = retryDelayMillis(attempt);
                log.warn("泛微 HRM 分页请求异常，第 {} 次重试，path={}, delayMs={}",
                    attempt + 1, path, delay, ex);
                sleepBeforeRetry(delay);
            }
        }
        throw new ServiceException("获取泛微 HRM 数据失败：" + (last == null ? "未知错误" : last.getMessage()));
    }

    private boolean isRetryableStatus(int status) {
        return status == 408 || status == 425 || status == 429 || status >= 500;
    }

    private long retryDelayMillis(int attempt) {
        long initial = Math.max(100L, properties.getHrmRequestRetryInitialDelayMillis());
        long maximum = retryMaxDelayMillis();
        int shift = Math.min(attempt, 10);
        long exponential = initial > maximum / (1L << shift)
            ? maximum : initial * (1L << shift);
        long jitter = java.util.concurrent.ThreadLocalRandom.current().nextLong(
            0, Math.min(250L, maximum - exponential) + 1);
        return Math.min(maximum, exponential + jitter);
    }

    private long retryMaxDelayMillis() {
        return Math.max(1000L, properties.getHrmRequestRetryMaxDelayMillis());
    }

    private long parseRetryAfterMillis(String value) {
        if (StringUtils.isBlank(value)) {
            return 0L;
        }
        try {
            return Math.max(0L, Long.parseLong(value.trim()) * 1000L);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private void sleepBeforeRetry(long delayMillis) {
        try {
            Thread.sleep(Math.max(0L, delayMillis));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new ServiceException("泛微 HRM 请求重试被中断");
        }
    }

    private HrmPage parseHrmPage(String body, int pageNum, int pageSize) {
        try {
            Map<String, Object> root = JsonUtils.parseMap(body);
            String code = value(root, "code");
            if (!isSuccessCode(code)) {
                throw new ServiceException("泛微 HRM 接口返回失败：" + firstNonBlank(value(root, "message"), value(root, "msg"), code));
            }
            Map<String, Object> data = asMap(root.get("data"));
            Object rawList = data.get("dataList");
            if (!(rawList instanceof List<?>)) {
                rawList = data.get("rows");
            }
            if (!(rawList instanceof List<?>)) {
                rawList = data.get("list");
            }
            List<Map<?, ?>> items = new ArrayList<>();
            if (rawList instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map) {
                        items.add(map);
                    }
                }
            }
            long total = number(data, "totalSize", "total", "totalCount");
            if (total <= 0 && !items.isEmpty() && items.size() < pageSize) {
                total = (long) (pageNum - 1) * pageSize + items.size();
            }
            return new HrmPage(items, total);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("泛微 HRM 返回内容不是合法 JSON：" + ex.getMessage());
        }
    }

    private OaUserDirectoryVo toUserDirectory(Map<?, ?> item) {
        OaUserDirectoryVo user = new OaUserDirectoryVo();
        user.setOaUserId(firstNonBlank(objectValue(item, "oaUserId"), objectValue(item, "userid"),
            objectValue(item, "userId"), objectValue(item, "id"), objectValue(item, "loginid")));
        user.setOaUserName(firstNonBlank(objectValue(item, "oaUserName"), objectValue(item, "lastname"),
            objectValue(item, "userName"), objectValue(item, "username")));
        user.setOaWorkcode(firstNonBlank(objectValue(item, "oaWorkcode"), objectValue(item, "workcode")));
        user.setOaLoginId(firstNonBlank(objectValue(item, "oaLoginId"), objectValue(item, "loginid")));
        user.setSex(objectValue(item, "sex"));
        user.setMobile(firstNonBlank(objectValue(item, "mobile"), objectValue(item, "mobilecall")));
        user.setTelephone(objectValue(item, "telephone"));
        user.setEmail(objectValue(item, "email"));
        user.setDepartmentId(firstNonBlank(objectValue(item, "departmentId"), objectValue(item, "departmentid")));
        user.setDepartmentName(firstNonBlank(objectValue(item, "departmentName"), objectValue(item, "departmentname")));
        user.setDepartmentCode(firstNonBlank(objectValue(item, "departmentCode"), objectValue(item, "departmentcode")));
        user.setSubCompanyId(firstNonBlank(objectValue(item, "subCompanyId"), objectValue(item, "subcompanyid1"),
            objectValue(item, "subcompanyId")));
        user.setSubCompanyName(firstNonBlank(objectValue(item, "subCompanyName"), objectValue(item, "subcompanyname")));
        user.setJobTitle(firstNonBlank(objectValue(item, "jobTitle"), objectValue(item, "jobtitle")));
        user.setManagerId(firstNonBlank(objectValue(item, "managerId"), objectValue(item, "managerid")));
        user.setAssistantId(firstNonBlank(objectValue(item, "assistantId"), objectValue(item, "assistantid")));
        user.setAccountType(firstNonBlank(objectValue(item, "accountType"), objectValue(item, "accounttype")));
        user.setBelongTo(firstNonBlank(objectValue(item, "belongTo"), objectValue(item, "belongto")));
        user.setModified(objectValue(item, "modified"));
        user.setStatus(objectValue(item, "status"));
        return user;
    }

    private OaSubCompanyDirectoryVo toSubCompanyDirectory(Map<?, ?> item) {
        OaSubCompanyDirectoryVo vo = new OaSubCompanyDirectoryVo();
        vo.setOaId(firstNonBlank(objectValue(item, "oaId"), objectValue(item, "id")));
        vo.setOaCode(firstNonBlank(objectValue(item, "oaCode"), objectValue(item, "subcompanycode"), objectValue(item, "subCompanyCode")));
        vo.setOaName(firstNonBlank(objectValue(item, "oaName"), objectValue(item, "subcompanyname"), objectValue(item, "subCompanyName")));
        vo.setOaFullName(firstNonBlank(objectValue(item, "oaFullName"), objectValue(item, "subcompanydesc"),
            objectValue(item, "subCompanyDesc"), objectValue(item, "subcompanyname"), objectValue(item, "subCompanyName")));
        vo.setOaParentId(firstNonBlank(objectValue(item, "oaParentId"), objectValue(item, "supsubcomid"), objectValue(item, "supSubComId")));
        vo.setCanceled(objectValue(item, "canceled"));
        vo.setCreated(objectValue(item, "created"));
        vo.setModified(objectValue(item, "modified"));
        vo.setShowOrder(objectValue(item, "showorder"));
        return vo;
    }

    private OaDepartmentDirectoryVo toDepartmentDirectory(Map<?, ?> item) {
        OaDepartmentDirectoryVo vo = new OaDepartmentDirectoryVo();
        vo.setOaId(firstNonBlank(objectValue(item, "oaId"), objectValue(item, "id")));
        vo.setOaCode(firstNonBlank(objectValue(item, "oaCode"), objectValue(item, "departmentcode"), objectValue(item, "departmentCode")));
        vo.setOaName(firstNonBlank(objectValue(item, "oaName"), objectValue(item, "departmentname"),
            objectValue(item, "departmentName"), objectValue(item, "departmentmark"), objectValue(item, "departmentMark")));
        vo.setOaMark(firstNonBlank(objectValue(item, "oaMark"), objectValue(item, "departmentmark"), objectValue(item, "departmentMark")));
        vo.setOaSubCompanyId(firstNonBlank(objectValue(item, "oaSubCompanyId"), objectValue(item, "subcompanyid1"),
            objectValue(item, "subCompanyId"), objectValue(item, "subcompanyId")));
        vo.setOaParentId(firstNonBlank(objectValue(item, "oaParentId"), objectValue(item, "supdepid"), objectValue(item, "supDeptId")));
        vo.setCanceled(objectValue(item, "canceled"));
        vo.setCreated(objectValue(item, "created"));
        vo.setModified(objectValue(item, "modified"));
        vo.setShowOrder(objectValue(item, "showorder"));
        return vo;
    }

    private OaJobTitleDirectoryVo toJobTitleDirectory(Map<?, ?> item) {
        OaJobTitleDirectoryVo vo = new OaJobTitleDirectoryVo();
        vo.setOaId(firstNonBlank(objectValue(item, "oaId"), objectValue(item, "id")));
        vo.setOaName(firstNonBlank(objectValue(item, "oaName"), objectValue(item, "jobtitlename"),
            objectValue(item, "jobTitleName"), objectValue(item, "jobtitlemark"), objectValue(item, "jobTitleMark")));
        vo.setOaMark(firstNonBlank(objectValue(item, "oaMark"), objectValue(item, "jobtitlemark"), objectValue(item, "jobTitleMark")));
        vo.setOaRemark(firstNonBlank(objectValue(item, "oaRemark"), objectValue(item, "jobtitleremark"), objectValue(item, "jobTitleRemark")));
        vo.setCreated(objectValue(item, "created"));
        vo.setModified(objectValue(item, "modified"));
        return vo;
    }

    private void putModified(Map<String, Object> params, String modified) {
        if (StringUtils.isNotBlank(modified)) {
            params.put("modified", modified);
        }
    }

    private String objectValue(Map<?, ?> map, String... keys) {
        if (map == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private long number(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value != null) {
                try {
                    return Long.parseLong(String.valueOf(value));
                } catch (NumberFormatException ignored) {
                    // 尝试下一个兼容字段。
                }
            }
        }
        return 0L;
    }

    private boolean isSuccessCode(String code) {
        return StringUtils.isBlank(code) || "SUCCESS".equalsIgnoreCase(code)
            || "0".equals(code) || "1".equals(code) || "200".equals(code)
            || "TRUE".equalsIgnoreCase(code);
    }

    private String normalizeSearchField(String field) {
        if ("lastname".equalsIgnoreCase(field) || "keyword".equalsIgnoreCase(field)) {
            return field.toLowerCase();
        }
        return "workcode";
    }

    private String url(String path) {
        return StrUtil.removeSuffix(properties.getAddress().trim(), "/") + path;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }

    private String value(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) {
            return null;
        }
        return String.valueOf(map.get(key));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String limit(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 2000 ? value.substring(0, 2000) : value;
    }

    private static final class HrmRetryableException extends Exception {

        private final int status;
        private final long retryAfterMillis;

        private HrmRetryableException(int status, String message, long retryAfterMillis) {
            super(message);
            this.status = status;
            this.retryAfterMillis = retryAfterMillis;
        }

        private int status() {
            return status;
        }

        private long retryAfterMillis() {
            return retryAfterMillis;
        }
    }

    private record TokenCache(String value, long expiredAt) {
    }

    private record HrmPage(List<Map<?, ?>> items, long total) {
    }
}
