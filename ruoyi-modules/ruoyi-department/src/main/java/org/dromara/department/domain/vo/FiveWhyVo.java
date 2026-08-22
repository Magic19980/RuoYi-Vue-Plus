package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 5WHY分析列表和详情视图。 */
@Data
public class FiveWhyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long deptId;
    private String companyDept;
    private String employeeNo;
    private String analystName;
    private LocalDate analysisDate;
    private String problemName;
    private String problemDescription;
    private String impactScope;
    private List<FiveWhyWhyVo> whys = new ArrayList<>();
    private List<FiveWhyImprovementVo> improvements = new ArrayList<>();
    private Long beforeOssId;
    private Long afterOssId;
    private String effectVerification;
    private String standardizationPlan;
    private String standardizationExecution;
    private String reviewStatus;
    private String reviewComment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
