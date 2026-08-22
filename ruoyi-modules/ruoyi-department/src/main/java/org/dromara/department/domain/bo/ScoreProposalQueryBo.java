package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** SCORE提案查询参数。 */
@Data
public class ScoreProposalQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String proposerName;
    private String mainCategory;
    private String subCategory;
    private String completionStatus;
    private String reviewStatus;
    private LocalDate beginDate;
    private LocalDate endDate;
}
