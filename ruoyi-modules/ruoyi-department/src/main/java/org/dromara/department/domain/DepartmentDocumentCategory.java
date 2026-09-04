package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 科室资料分类配置实体，对应 {@code dm_department_document_category} 表。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_document_category")
public class DepartmentDocumentCategory extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 分类主键。 */
    private Long id;

    /** 业务科室主键。 */
    private Long deptId;

    /** 父分类ID，0表示顶级分类。 */
    private Long parentId;

    /** 分类名称。 */
    private String categoryName;

    /** 同级分类显示顺序。 */
    private Integer sortNum;

    /** 分类状态。 */
    private String status;

    /** 分类备注。 */
    private String remark;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
