package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 审批实例状态与外部调用事件日志。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_process_event_log")
public class OaProcessEventLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long processId;

    private String eventType;

    private String fromStatus;

    private String toStatus;

    private String requestSummary;

    private String responseSummary;

    private String errorCode;

    private String idempotencyKey;
}
