package org.dromara.department.domain.bo;

import lombok.Data;

/**
 * 科室资料查询参数。
 */
@Data
public class DepartmentDocumentQueryBo {

    private String title;

    private Long categoryId;

    private Long projectId;

    private String fileSuffix;

    private String status;
}
