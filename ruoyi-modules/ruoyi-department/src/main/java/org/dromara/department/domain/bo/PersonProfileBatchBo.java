package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 人员档案批量新增参数。
 */
@Data
public class PersonProfileBatchBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "系统用户不能为空", groups = AddGroup.class)
    private List<Long> userIds;

    private LocalDate joinDate;

    private LocalDate leaveDate;

    private String memberType;

    private String remark;
}
