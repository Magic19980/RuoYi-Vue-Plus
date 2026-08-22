package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 科室项目主数据视图。
 */
@Data
@ExcelIgnoreUnannotated
public class DepartmentProjectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("主键")
    private Long id;

    private Long deptId;

    @ExcelProperty("项目编码")
    private String projectCode;

    @ExcelProperty("项目名称")
    private String projectName;

    @ExcelProperty("项目类型")
    private String projectType;

    @ExcelProperty("负责人")
    private String responsiblePerson;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("排序")
    private Integer sortNum;

    @ExcelProperty("备注")
    private String remark;

    private Long operationRecordCount;

    private LocalDateTime createTime;
}
