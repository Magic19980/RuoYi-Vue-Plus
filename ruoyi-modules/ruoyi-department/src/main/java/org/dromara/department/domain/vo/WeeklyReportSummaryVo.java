package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 周报汇总快照。
 */
@Data
public class WeeklyReportSummaryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate weekStart;

    private LocalDate weekEnd;

    private Integer reportCount = 0;

    private Integer requiredUserCount = 0;

    private Integer filledUserCount = 0;

    private Integer missingUserCount = 0;

    private Map<LocalDate, Integer> reportCountByDate = new LinkedHashMap<>();

    private List<String> missingUserNames = new ArrayList<>();

    private List<WeeklyReportItemVo> reportItems = new ArrayList<>();

    private List<String> tomorrowPlans = new ArrayList<>();

    private List<String> coordinationNotes = new ArrayList<>();

    /** 人工单台账汇总：PDF/手动维护的人工单数据。 */
    private WorkOrderSummaryVo manualOrderSummary = new WorkOrderSummaryVo();

    /** 运维台账汇总：工作记录与系统在线率数据。 */
    private OperationSummaryVo operationSummary = new OperationSummaryVo();

}
