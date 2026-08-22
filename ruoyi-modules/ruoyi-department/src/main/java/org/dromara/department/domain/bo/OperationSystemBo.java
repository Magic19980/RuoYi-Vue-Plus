package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 系统在线率台账新增、修改参数。
 */
@Data
public class OperationSystemBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;

    private Long projectId;

    private LocalDate statDate;

    @Size(max = 150, message = "系统名称不能超过150个字符")
    private String systemName;

    @Size(max = 100, message = "负责人不能超过100个字符")
    private String responsiblePerson;

    @Size(max = 150, message = "服务器名称不能超过150个字符")
    private String serverName;

    @Size(max = 1000, message = "服务器IP不能超过1000个字符")
    private String serverIp;

    private BigDecimal onlineDays;

    private Integer downtimeMinutes;

    private BigDecimal onlineRate;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;
}
