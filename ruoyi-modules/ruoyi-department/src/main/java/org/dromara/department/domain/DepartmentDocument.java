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
 * 科室资料主表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_document")
public class DepartmentDocument extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    private Long projectId;

    private Long categoryId;

    private String title;

    private String description;

    private String tags;

    private String visibility;

    private String status;

    private LocalDate expireDate;

    private Long currentVersionId;

    private Integer versionNo;

    private Long currentOssId;

    private String currentFileName;

    private String currentOriginalName;

    private String currentFileSuffix;

    private Long currentFileSize;

    private String currentContentType;

    @TableLogic
    private String delFlag;
}
