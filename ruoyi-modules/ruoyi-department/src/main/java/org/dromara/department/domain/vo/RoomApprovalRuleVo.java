package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 房间预约审批规则视图。 */
@Data
public class RoomApprovalRuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roomId;
    private String roomName;
    private Integer stepNo;
    private String approverType;
    private Long approverId;
    private String approverName;
    private Integer timeoutMinutes;
    private Boolean enabled;
    private String remark;
    private LocalDateTime createTime;
}
