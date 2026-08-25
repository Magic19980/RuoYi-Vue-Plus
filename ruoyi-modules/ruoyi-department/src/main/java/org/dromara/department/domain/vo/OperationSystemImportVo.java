package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 《物流系统科日常管理表》系统运维报告导入行。
 */
@Data
@ExcelIgnoreUnannotated
public class OperationSystemImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("项目")
    private String projectName;

    @ExcelProperty("系统名称")
    private String systemName;

    @ExcelProperty("负责人")
    private String responsiblePerson;

    @ExcelProperty("系统在线时长天")
    private BigDecimal onlineDays;

    @ExcelProperty("系统停机时间")
    private String downtimeDuration;

    @ExcelProperty("系统可用率")
    private BigDecimal onlineRate;

    @ExcelProperty("备注")
    private String remark;
}
