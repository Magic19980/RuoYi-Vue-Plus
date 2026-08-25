package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 工单台账汇总，用于周报和PPT数据源。
 */
@Data
public class WorkOrderSummaryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer totalCount = 0;

    private BigDecimal totalQuantity = BigDecimal.ZERO;

    /** PDF/人工明细中的工程量合计。 */
    private BigDecimal totalEngineeringQuantity = BigDecimal.ZERO;

    /** PDF/人工明细中的中国人工合计。 */
    private BigDecimal totalChineseLabor = BigDecimal.ZERO;

    /** PDF/人工明细中的印尼人工合计。 */
    private BigDecimal totalIndonesiaLabor = BigDecimal.ZERO;

    /** 中国人工与印尼人工合计，用于人工投入统计。 */
    private BigDecimal totalLaborQuantity = BigDecimal.ZERO;

    /** PDF 原始统计明细行数。 */
    private Integer detailCount = 0;

    private Integer resolvedCount = 0;

    private BigDecimal resolutionRate = BigDecimal.ZERO;

    private Integer averageResolutionMinutes = 0;

    private Integer unattributedCount = 0;

    private List<DimensionCountVo> bySystem = new ArrayList<>();

    private List<DimensionCountVo> byFaultType = new ArrayList<>();

    /**
     * 汇总维度行。
     */
    @Data
    public static class DimensionCountVo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String name;

        private Integer count;

        private BigDecimal quantity;

        private BigDecimal percentage;
    }
}
