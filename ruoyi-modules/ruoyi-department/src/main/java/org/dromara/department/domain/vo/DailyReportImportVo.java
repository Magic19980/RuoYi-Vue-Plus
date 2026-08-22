package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 日报导入模板。填报人为空时默认使用当前登录用户。
 */
@Data
@ExcelIgnoreUnannotated
public class DailyReportImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("日期")
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate reportDate;

    @ExcelProperty("填报人")
    private String userName;

    @ExcelProperty("今日工作")
    private String todayWork;

    @ExcelProperty("明日计划")
    private String tomorrowPlan;

    @ExcelProperty("待协调项/备注")
    private String coordinationNote;
}
