package org.dromara.department.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运维工作记录列表和导出视图。
 */
@Data
@ExcelIgnoreUnannotated
public class OperationRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("请求人")
    private String requestPerson;

    @ExcelProperty("客户单位")
    private String customerUnit;

    @ExcelProperty("请求岗位类型")
    private String requestRoleType;

    @ExcelProperty("请求时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;

    @ExcelProperty("处理人")
    private String handler;

    @ExcelProperty("处理时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processTime;

    @ExcelProperty("完成时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    @ExcelProperty("响应耗时(分钟)")
    private Integer responseMinutes;

    @ExcelProperty("处理耗时(分钟)")
    private Integer processingMinutes;

    @ExcelProperty("是否午休")
    private String lunchBreak;

    @ExcelProperty("处理状态")
    private String processStatus;

    @ExcelProperty("处理方式")
    private String processMethod;

    @ExcelProperty("提交人")
    private String submitter;

    @ExcelProperty("系统/项目")
    private String systemName;

    @ExcelProperty("故障类型")
    private String faultType;

    @ExcelProperty("业务描述")
    private String businessDescription;

    @ExcelProperty("解决方案")
    private String solution;

    @ExcelProperty("备注")
    private String remark;

    private Long deptId;

    private Long projectId;

    private String projectName;

    private String sourceType;

    private String sourceFileName;

    private LocalDateTime createTime;
}
