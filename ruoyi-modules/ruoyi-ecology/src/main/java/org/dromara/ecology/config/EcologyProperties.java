package org.dromara.ecology.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 泛微 Ecology 连接配置。
 *
 * <p>密钥只允许通过配置中心、环境变量或密钥管理系统注入，代码中不保留真实凭据。</p>
 */
@Data
@ConfigurationProperties(prefix = "ecology")
public class EcologyProperties {

    /** 是否启用真实泛微调用。 */
    private boolean enabled;

    /** 泛微服务地址，例如 https://oa.example.com。 */
    private String address;

    /** Ecology 应用 appid。 */
    private String appId;

    /** Ecology 服务端 RSA 公钥。 */
    private String serverPublicKey;

    /** Ecology 应用 secret。 */
    private String serverSecret;

    /**
     * Ecology HRM REST 接口的授权应用 ID。
     *
     * <p>System-boxplusn 的 getOaUserInfo/getOaUserList 使用的是 HRM REST
     * 的 key + ts 签名，不是 Ecology token 鉴权，因此单独配置。</p>
     */
    private String hrmApplyId;

    /** 获取泛微人员的 HRM REST 路径。 */
    private String hrmUserListPath = "/api/hrm/resful/getHrmUserInfoWithPage";

    /** 获取泛微分部的 HRM REST 路径。 */
    private String hrmSubcompanyListPath = "/api/hrm/resful/getHrmsubcompanyWithPage";

    /** 获取泛微部门的 HRM REST 路径。 */
    private String hrmDepartmentListPath = "/api/hrm/resful/getHrmdepartmentWithPage";

    /** 获取泛微岗位的 HRM REST 路径。 */
    private String hrmJobtitleListPath = "/api/hrm/resful/getJobtitleInfoWithPage";

    /** 人员搜索字段，默认兼容 System-boxplusn 的 workcode 参数。 */
    private String hrmUserSearchField = "workcode";

    /** 泛微新人员写入 sys_user 时使用的初始密码；必须通过密钥或环境变量配置。 */
    private String hrmDefaultPassword;

    /** 页面初始密码的加密密钥，建议通过环境变量注入；未配置时兼容使用 serverSecret 派生。 */
    private String hrmPasswordEncryptionKey;

    /** 是否跳过泛微次账号；默认跳过，避免一个人生成多个本地账号。 */
    private boolean hrmSkipSecondaryAccounts = true;

    /** 是否启用定时组织与人员同步。默认关闭，首次应先人工执行并核对同步结果。 */
    private boolean hrmSyncEnabled;

    /** HRM 同步任务间隔，单位毫秒。 */
    private long hrmSyncFixedDelayMillis = 3600000L;

    /** HRM 同步任务首次启动延迟，单位毫秒。 */
    private long hrmSyncInitialDelayMillis = 120000L;

    /** 增量同步向前重叠时间，单位秒，防止边界时间和时钟偏差导致漏数据。 */
    private int hrmSyncOverlapSeconds = 300;

    /** HRM 每页最大条数。 */
    private int hrmSyncPageSize = 200;

    /** HRM 分页之间的最小间隔，单位毫秒，避免连续请求形成突发流量。 */
    private long hrmSyncPageIntervalMillis = 300L;

    /** 申请 token 时声明的有效期，单位秒。 */
    private int tokenTtlSeconds = 3600;

    /** HTTP 连接超时时间，单位毫秒。 */
    private int connectTimeout = 5000;

    /** HTTP 读取超时时间，单位毫秒。 */
    private int readTimeout = 15000;

    /** 可选的泛微流程链接模板，使用 {requestId} 占位。 */
    private String requestLinkTemplate;

    /** 泛微流程状态查询路径，默认兼容 System-boxplusn 使用的 getWorkflowRequest。 */
    private String requestStatusPath = "/api/workflow/paService/getWorkflowRequest";

    /** 是否始终向泛微提交 tp/fj 字段；旧系统即使无附件也会提交空数组。 */
    private boolean attachmentWriteEmptyFields = true;

    /** HRM 单页请求失败后的重试次数。 */
    private int hrmRequestRetryCount = 2;

    /** HRM 请求首次重试前的等待时间，单位毫秒，后续按指数退避。 */
    private long hrmRequestRetryInitialDelayMillis = 1000L;

    /** HRM 请求重试等待时间上限，单位毫秒。 */
    private long hrmRequestRetryMaxDelayMillis = 30000L;

    /** 是否接收泛微回调。 */
    private boolean callbackEnabled;

    /** 回调 HMAC 密钥；实际泛微签名规范如不同，应替换 verifier 实现。 */
    private String callbackSecret;

    private String callbackSignatureHeader = "X-Ecology-Signature";

    private String callbackTimestampHeader = "X-Ecology-Timestamp";

    private String callbackNonceHeader = "X-Ecology-Nonce";

    /** 回调时间戳允许偏差，单位秒。 */
    private int callbackTimestampToleranceSeconds = 300;

    /** 是否启用后台自动对账。 */
    private boolean reconcileEnabled;

    /** 自动对账批次大小。 */
    private int reconcileBatchSize = 50;

    /** 对账间隔，单位毫秒。 */
    private long reconcileFixedDelayMillis = 300000L;

    /** 首次启动后延迟对账，单位毫秒。 */
    private long reconcileInitialDelayMillis = 60000L;

    /** 状态超过该时间仍未同步时进入下一轮对账，单位秒。 */
    private int reconcileStaleSeconds = 300;
}
