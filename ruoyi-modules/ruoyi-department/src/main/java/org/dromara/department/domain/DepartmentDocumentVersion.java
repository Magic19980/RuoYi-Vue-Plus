package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 科室资料版本实体，对应 {@code dm_department_document_version} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_document_version")
public class DepartmentDocumentVersion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 资料版本主键。 */
    private Long id;

    /** 资料主键。 */
    private Long documentId;

    /** 版本号，从1开始递增。 */
    private Integer versionNo;

    /** 对象存储文件主键。 */
    private Long ossId;

    /** 文件原始名称。 */
    private String originalName;

    /** 文件后缀。 */
    private String fileSuffix;

    /** 文件大小，单位为字节。 */
    private Long fileSize;

    /** 文件内容类型。 */
    private String contentType;

    /** 版本说明。 */
    private String versionNote;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
