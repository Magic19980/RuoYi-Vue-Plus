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
 * 科室资料版本对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_document_version")
public class DepartmentDocumentVersion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long documentId;

    private Integer versionNo;

    private Long ossId;

    private String originalName;

    private String fileSuffix;

    private Long fileSize;

    private String contentType;

    private String versionNote;

    @TableLogic
    private String delFlag;
}
