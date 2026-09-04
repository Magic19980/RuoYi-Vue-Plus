package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/** 本地申请与泛微 requestId 的关联实例。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_process_instance")
public class OaProcessInstance extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long applicationId;

    private String businessType;

    private String businessId;

    private String businessNo;

    private String sourceModule;

    private String businessTitle;

    private Long workflowConfigId;

    private String workflowId;

    private String oaRequestId;

    private Long applicantUserId;

    private String applicantOaUserId;

    private String localStatus;

    private String oaStatus;

    private String oaStatusRaw;

    private String requestName;

    private LocalDateTime submittedAt;

    private LocalDateTime completedAt;

    private LocalDateTime lastSyncAt;

    private String failReason;

    private Integer retryCount;

    private String idempotencyKey;

    /** 提交时的流程配置快照，避免后续修改配置影响历史追溯。 */
    private String configSnapshotJson;

    private String oaLink;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
