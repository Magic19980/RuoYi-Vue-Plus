package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;

/**
 * 人员档案导入模板。
 */
@Data
@ExcelIgnoreUnannotated
public class PersonProfileImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("登录账号")
    private String userName;

    @ExcelProperty("工号")
    private String employeeNo;

    @ExcelProperty("岗位")
    private String jobTitle;

    @ExcelProperty("纳入日报")
    private String dailyReportEnabled;

    @ExcelProperty("提醒时间")
    @DateTimeFormat("HH:mm:ss")
    private LocalTime reminderTime;

    @ExcelProperty("备注")
    private String remark;
}
