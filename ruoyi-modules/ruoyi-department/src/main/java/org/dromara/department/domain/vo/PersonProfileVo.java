package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("加入日期")
    private LocalDate joinDate;

    @ExcelProperty("离开生效日")
    private LocalDate leaveDate;

    @ExcelProperty("成员类型")
    private String memberType;

    @ExcelProperty("服务状态")
    private String memberStatus;

    @ExcelProperty("结束原因")
    private String endReason;

    private LocalDateTime endedAt;
}
