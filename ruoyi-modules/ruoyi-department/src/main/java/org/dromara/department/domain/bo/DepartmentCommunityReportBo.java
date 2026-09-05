package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 协作社区举报及处理参数。
 */
@Data
public class DepartmentCommunityReportBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "举报主键不能为空", groups = HandleGroup.class)
    private Long id;

    @NotBlank(message = "举报原因不能为空", groups = ReportGroup.class)
    @Size(max = 500, message = "举报原因不能超过500个字符", groups = ReportGroup.class)
    private String reason;

    @NotBlank(message = "处理状态不能为空", groups = HandleGroup.class)
    private String status;

    @Size(max = 500, message = "处理备注不能超过500个字符", groups = HandleGroup.class)
    private String handleNote;

    public interface ReportGroup {
    }

    public interface HandleGroup {
    }
}
