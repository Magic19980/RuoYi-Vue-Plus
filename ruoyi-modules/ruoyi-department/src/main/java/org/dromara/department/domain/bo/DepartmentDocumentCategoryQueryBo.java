package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 科室资料分类查询参数。 */
@Data
public class DepartmentDocumentCategoryQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String categoryName;

    private String status;
}
