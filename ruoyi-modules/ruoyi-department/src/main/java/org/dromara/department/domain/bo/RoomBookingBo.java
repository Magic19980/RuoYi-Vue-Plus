package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 房间预约新增、修改和冲突预检参数。 */
@Data
public class RoomBookingBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "预约主键不能为空", groups = EditGroup.class)
    private Long id;

    /** 新建预约请求幂等号；由前端在一次提交生命周期内保持不变。 */
    @Size(max = 64, message = "预约请求号不能超过64个字符")
    private String requestKey;

    /**
     * 修改/取消的作用范围：SERIES 整个预约系列，OCCURRENCE 单次实例。
     * 新建预约时忽略该字段。
     */
    private String operationScope;

    /** 循环预约中要操作的实例序号，从 1 开始。 */
    private Integer occurrenceNo;

    /** 取消或变更时的业务原因。 */
    @Size(max = 500, message = "操作原因不能超过500个字符")
    private String operationReason;

    @NotEmpty(message = "至少选择一个房间")
    private List<Long> roomIds;

    @NotBlank(message = "会议主题不能为空")
    @Size(max = 200, message = "会议主题不能超过200个字符")
    private String title;

    @Size(max = 2000, message = "会议说明不能超过2000个字符")
    private String description;

    private String visibility;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startAt;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endAt;

    private String recurrenceType;

    private Integer recurrenceInterval;

    private LocalDate recurrenceUntil;

    private Integer recurrenceCount;

    private List<Long> attendeeIds;

    /** 条款确认；预约提交和修改都必须显式确认。 */
    private Boolean termsAccepted;
}
