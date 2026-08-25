package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

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

    @ExcelProperty("加入日期")
    private LocalDate joinDate;

    @ExcelProperty("成员类型")
    private String memberType;
}
