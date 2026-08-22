package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 人员档案查询参数。
 */
@Data
public class PersonProfileQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userName;

    private Long id;

    private String jobTitle;

    private String dailyReportEnabled;
}
