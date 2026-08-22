package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;

/**
 * 人员档案新增、修改参数。
 */
@Data
public class PersonProfileBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "系统用户不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long userId;

    private String employeeNo;

    private String jobTitle;

    private String dailyReportEnabled;

    private LocalTime reminderTime;

    private String remark;
}
