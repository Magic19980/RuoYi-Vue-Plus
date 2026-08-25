package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 人工单台账导出行。
 *
 * <p>一个人工统计明细导出一行，主记录信息在明细行中重复，避免多明细主记录的“多项（详见明细）”占位值造成数据丢失。</p>
 */
@Data
@ExcelIgnoreUnannotated
public class WorkOrderExportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("工单编号")
    private String ticketNo;

    @ExcelProperty("发生年月")
    @DateTimeFormat("yyyy-MM")
    private LocalDate occurDate;

    @ExcelProperty("明细序号")
    private Integer detailSequence;

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

    @ExcelProperty("工单量")
    private BigDecimal quantity;

    @ExcelProperty("工作类别")
    private String workCategory;

    @ExcelProperty("故障类型")
    private String faultType;

    @ExcelProperty("处理时长(分钟)")
    private Integer resolutionMinutes;

    @ExcelProperty("反馈渠道")
    private String feedbackChannel;

    @ExcelProperty("来源")
    private String sourceType;

    @ExcelProperty("来源文件")
    private String sourceFileName;

    @ExcelProperty("来源页码")
    private Integer sourcePage;
}
