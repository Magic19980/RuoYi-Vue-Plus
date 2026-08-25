package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 科室资料分类配置。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_document_category")
public class DepartmentDocumentCategory extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    /** 父分类ID，0表示顶级分类。 */
    private Long parentId;

    private String categoryName;

    private Integer sortNum;

    private String status;

    private String remark;

    @TableLogic
    private String delFlag;
}
