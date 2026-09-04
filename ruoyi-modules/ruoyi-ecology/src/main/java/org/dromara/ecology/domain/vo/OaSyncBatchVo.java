package org.dromara.ecology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ecology.domain.OaSyncBatch;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 泛微 HRM 同步批次视图。 */
@Data
@AutoMapper(target = OaSyncBatch.class)
public class OaSyncBatchVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
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
