package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统在线率台账列表和导出视图。
 */
@Data
@ExcelIgnoreUnannotated
public class OperationSystemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("统计日期")
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate statDate;

    private Long projectId;

    @ExcelProperty("项目")
    private String projectName;

    @ExcelProperty("系统名称")
    private String systemName;

    @ExcelProperty("负责人")
    private String responsiblePerson;

    @ExcelProperty("在线时长(天)")
    private BigDecimal onlineDays;

    @ExcelProperty("停机时间(分钟)")
    private Integer downtimeMinutes;

    @ExcelProperty("系统在线率(%)")
    private BigDecimal onlineRate;

    @ExcelProperty("备注")
    private String remark;

    private Long deptId;

    private String sourceType;

    private String sourceFileName;

    private LocalDateTime createTime;
}
