package org.dromara.ecology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ecology.domain.OaSyncDetail;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 泛微 HRM 同步异常/处理明细视图。 */
@Data
@AutoMapper(target = OaSyncDetail.class)
public class OaSyncDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long batchId;
    private String entityType;
    private String sourceId;
    private String sourceKey;
    private Long localId;
    private String action;
    private String detailStatus;
    private String message;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
