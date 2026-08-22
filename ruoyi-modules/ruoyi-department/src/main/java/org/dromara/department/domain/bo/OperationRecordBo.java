package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运维工作记录新增、修改参数。
 */
@Data
public class OperationRecordBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;

    private Long projectId;

    @Size(max = 150, message = "请求人不能超过150个字符")
    private String requestPerson;

    @Size(max = 150, message = "客户单位不能超过150个字符")
    private String customerUnit;

    @Size(max = 100, message = "请求岗位类型不能超过100个字符")
    private String requestRoleType;

    private LocalDateTime requestTime;

    @Size(max = 100, message = "处理人不能超过100个字符")
    private String handler;

    private LocalDateTime processTime;

    private LocalDateTime completionTime;

    private Integer responseMinutes;

    private Integer processingMinutes;

    private String lunchBreak;

    private String processStatus;

    @Size(max = 100, message = "处理方式不能超过100个字符")
    private String processMethod;

    @Size(max = 100, message = "提交人不能超过100个字符")
    private String submitter;

    @Size(max = 150, message = "系统/项目不能超过150个字符")
    private String systemName;

    @Size(max = 100, message = "故障类型不能超过100个字符")
    private String faultType;

    private String businessDescription;

    private String solution;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;
}
