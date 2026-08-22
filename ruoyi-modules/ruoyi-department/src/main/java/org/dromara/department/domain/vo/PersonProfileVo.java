package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;

/**
 * 人员档案视图对象。
 */
@Data
@ExcelIgnoreUnannotated
public class PersonProfileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("主键")
    private Long id;

    private Long userId;

    @ExcelProperty("登录账号")
    private String userName;

    @ExcelProperty("姓名")
    private String nickName;

    @ExcelProperty("部门")
    private String deptName;

    @ExcelProperty("工号")
    private String employeeNo;

    @ExcelProperty("岗位")
    private String jobTitle;

    @ExcelProperty("纳入日报")
    private String dailyReportEnabled;

    @ExcelProperty("提醒时间")
    private LocalTime reminderTime;

    @ExcelProperty("备注")
    private String remark;
}
