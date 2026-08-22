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
 * 工单PDF导入批次对象 dm_work_order_import_batch。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_work_order_import_batch")
public class WorkOrderImportBatch extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String sourceFileName;

    private Long ossId;

    private LocalDate sourcePeriodStart;

    private LocalDate sourcePeriodEnd;

    private Integer pageCount;

    private Integer recordCount;

    private Integer parsedRecordCount;

    private Integer pendingRecordCount;

    private String status;

    private String errorMessage;

    @TableLogic
    private String delFlag;
}
