package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 科室资料分类配置视图。 */
@Data
public class DepartmentDocumentCategoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long deptId;

    private Long parentId;

    private String categoryName;

    private Integer sortNum;

    private String status;

    private String remark;

    private Long documentCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<DepartmentDocumentCategoryVo> children = new ArrayList<>();
}
