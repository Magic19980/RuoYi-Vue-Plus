package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 运维台账汇总，用于周报页面和PPT。
 */
@Data
public class OperationSummaryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer totalCount = 0;

    private Integer resolvedCount = 0;

    private BigDecimal resolutionRate = BigDecimal.ZERO;

    private Integer averageProcessingMinutes = 0;

    private BigDecimal onlineRate = BigDecimal.ZERO;

    private Integer unattributedCount = 0;

    private List<DimensionCountVo> bySystem = new ArrayList<>();

    private List<DimensionCountVo> byFaultType = new ArrayList<>();

    private List<DimensionCountVo> byProcessMethod = new ArrayList<>();

    @Data
    public static class DimensionCountVo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String name;

        private Integer count;

        private BigDecimal percentage;
    }
}
