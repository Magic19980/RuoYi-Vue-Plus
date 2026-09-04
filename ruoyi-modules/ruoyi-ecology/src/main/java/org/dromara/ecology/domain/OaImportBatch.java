package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 通用导入批次。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_import_batch")
public class OaImportBatch extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long configId;

    private String businessType;

    private String batchNo;

    private String sourceFileName;

    private String status;

    private Integer totalCount;

    private Integer matchedCount;

    private Integer groupCount;

    private Integer applicationCount;

    private Integer failedCount;

    private Integer skippedCount;

    private String message;

    @TableLogic
    private String delFlag;
}
