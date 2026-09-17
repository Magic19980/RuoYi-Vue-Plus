package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 房间预约配额策略视图。 */
@Data
public class RoomQuotaPolicyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roomId;
    private String roomName;
    private String subjectType;
    private Long subjectId;
    private String subjectName;
    private String periodType;
    private Integer quotaMinutes;
    private Integer quotaCount;
    private String overQuotaAction;

    private BigDecimal unitPrice;

    private BigDecimal refundRate;
    private Boolean enabled;
    private String remark;
    private LocalDateTime createTime;
}
