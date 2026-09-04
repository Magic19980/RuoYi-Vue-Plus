package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 科室资料主表实体，对应 {@code dm_department_document} 表。
 *
 * <p>资料元数据与文件版本分离保存，当前版本字段用于快速读取最新文件。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_document")
public class DepartmentDocument extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 资料主键。 */
    private Long id;

    /** 业务科室主键。 */
    private Long deptId;

    /** 关联项目主键。 */
    private Long projectId;

    /** 资料分类主键。 */
    private Long categoryId;

    /** 资料标题。 */
    private String title;

    /** 资料说明。 */
    private String description;

    /** 资料标签。 */
    private String tags;

    /** 可见范围。 */
    private String visibility;

    /** 资料状态。 */
    private String status;

    /** 资料有效期。 */
    private LocalDate expireDate;

    /** 当前版本主键。 */
    private Long currentVersionId;

    /** 当前版本号。 */
    private Integer versionNo;

    /** 当前版本对象存储文件主键。 */
    private Long currentOssId;

    /** 当前版本文件名。 */
    private String currentFileName;

    /** 当前版本原始文件名。 */
    private String currentOriginalName;

    /** 当前版本文件后缀。 */
    private String currentFileSuffix;

    /** 当前版本文件大小，单位为字节。 */
    private Long currentFileSize;

    /** 当前版本文件内容类型。 */
    private String currentContentType;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
