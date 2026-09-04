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
 * 工单PDF导入批次实体，对应 {@code dm_work_order_import_batch} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_work_order_import_batch")
public class WorkOrderImportBatch extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 导入批次主键。 */
    private Long id;

    /** 来源PDF文件名称。 */
    private String sourceFileName;

    /** 对象存储文件主键。 */
    private Long ossId;

    /** 来源数据统计周期开始日期。 */
    private LocalDate sourcePeriodStart;

    /** 来源数据统计周期结束日期。 */
    private LocalDate sourcePeriodEnd;

    /** PDF总页数。 */
    private Integer pageCount;

    /** 解析出的总记录数。 */
    private Integer recordCount;

    /** 已成功解析的记录数。 */
    private Integer parsedRecordCount;

    /** 待人工确认的记录数。 */
    private Integer pendingRecordCount;

    /** 导入批次状态。 */
    private String status;

    /** 导入或解析错误信息。 */
    private String errorMessage;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
