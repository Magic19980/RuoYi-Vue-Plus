package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 日报列表和导出视图。
 */
@Data
@ExcelIgnoreUnannotated
public class DailyReportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("日报日期")
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate reportDate;

    @ExcelProperty("填报人")
    private String userName;

    @ExcelProperty("姓名")
    private String nickName;

    @ExcelProperty("部门")
    private String deptName;

    @ExcelProperty("今日工作")
    private String todayWork;

    @ExcelProperty("明日计划")
    private String tomorrowPlan;

    @ExcelProperty("待协调事项/备注")
    private String coordinationNote;

    @ExcelProperty("状态")
    private String status;

    private String sourceType;

    private Long leaveId;

    private Long userId;

    private Long deptId;
}
