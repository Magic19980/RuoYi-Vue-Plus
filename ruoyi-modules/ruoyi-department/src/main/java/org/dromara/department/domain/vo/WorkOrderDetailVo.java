package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 人工单 PDF 原始统计明细。
 */
@Data
@ExcelIgnoreUnannotated
public class WorkOrderDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @ExcelProperty("序号")
    private Integer sequenceNo;

    @ExcelProperty("申请部门")
    private String requestDept;

    @ExcelProperty("结算单位")
    private String settlementUnit;

    @ExcelProperty("项目负责人")
    private String projectOwner;

    @ExcelProperty("项目名称（单价表对应名称）")
    private String projectName;

    @ExcelProperty("项目特征（实用物资）")
    private String projectFeature;

    @ExcelProperty("计量单位")
    private String unit;

    @ExcelProperty("工程量")
    private String engineeringQuantity;

    @ExcelProperty("中国人工")
    private String chineseLabor;

    @ExcelProperty("印尼人工")
    private String indonesiaLabor;

    @ExcelProperty("安装车间")
    private String installDepartment;

    @ExcelProperty("安装班组")
    private String installTeam;

    @ExcelProperty("工作内容")
    private String workContent;

    private Long workOrderId;

    private Integer sourcePage;

    private BigDecimal quantity;

    private String parseMessage;
}
