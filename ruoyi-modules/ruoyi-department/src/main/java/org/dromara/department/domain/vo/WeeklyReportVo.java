package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 周报列表视图。
 */
@Data
public class WeeklyReportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private LocalDate weekStart;

    private LocalDate weekEnd;

    private String title;

    private Integer reportCount;

    private Integer requiredUserCount;

    private Integer filledUserCount;

    private Integer missingUserCount;

    private String status;

    private LocalDateTime createTime;
}
