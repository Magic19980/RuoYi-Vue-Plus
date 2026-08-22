package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工单台账列表和导出视图。
 */
@Data
@ExcelIgnoreUnannotated
public class WorkOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("工单编号")
    private String ticketNo;

    @ExcelProperty("发生年月")
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate occurDate;

    @ExcelProperty("申请部门")
    private String requestDept;

    @ExcelProperty("结算单位")
    private String settlementUnit;

    @ExcelProperty("项目负责人")
    private String projectOwner;

    @ExcelProperty("项目名称")
    private String systemName;

    @ExcelProperty("安装车间")
    private String installDepartment;

    @ExcelProperty("安装班组")
    private String installTeam;

    @ExcelProperty("工作类别")
    private String workCategory;

    @ExcelProperty("故障类型")
    private String faultType;

    @ExcelProperty("项目特征")
    private String title;

    @ExcelProperty("工作内容")
    private String workContent;

    @ExcelProperty("计量单位")
    private String unit;

    @ExcelProperty("工单量")
    private BigDecimal quantity;

    @ExcelProperty("责任人")
    private String responsiblePerson;

    @ExcelProperty("处理人")
    private String handler;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("处理时长(分钟)")
    private Integer resolutionMinutes;

    @ExcelProperty("反馈渠道")
    private String feedbackChannel;

    @ExcelProperty("数据状态")
    private String reviewStatus;

    @ExcelProperty("来源")
    private String sourceType;

    @ExcelProperty("来源文件")
    private String sourceFileName;

    @ExcelProperty("来源页码")
    private Integer sourcePage;

    /**
     * PDF 主记录对应的人工统计明细行数。
     */
    private Integer detailCount;

    private Long deptId;

    private Long sourceBatchId;

    private BigDecimal parseConfidence;

    private String parseMessage;

    private String remark;

    private LocalDate sourcePeriodStart;

    private LocalDate sourcePeriodEnd;

    private LocalDateTime createTime;
}
