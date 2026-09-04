package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 通用导入批次明细，业务字段以 JSON 保存。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_import_record")
public class OaImportRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private Integer rowNo;

    private String dataJson;

    private String groupKey;

    private String groupName;

    private Long deptId;

    private Long companyId;

    private Long applicationId;

    private Long attachmentOssId;

    private String status;

    private String errorMessage;

    private String skipReason;

    @TableLogic
    private String delFlag;
}
