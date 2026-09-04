package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** HRM 同步执行结果。 */
@Data
public class OaSyncResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long batchId;
    private String syncType;
    private String syncMode;
    private String status;
    private String watermark;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer totalCount;
    private Integer successCount;
    private Integer createdCount;
    private Integer updatedCount;
    private Integer disabledCount;
    private Integer pendingCount;
    private Integer failedCount;
    private String message;
}
