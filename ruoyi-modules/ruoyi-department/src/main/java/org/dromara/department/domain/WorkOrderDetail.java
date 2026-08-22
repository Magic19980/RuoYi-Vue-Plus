package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * PDF 人工统计明细，对应人工单原始表格的一行。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_work_order_detail")
public class WorkOrderDetail extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long workOrderId;

    private Integer sourcePage;

    private Integer sequenceNo;

    private String requestDept;

    private String settlementUnit;

    private String projectOwner;

    private String projectName;

    private String projectFeature;

    private String unit;

    private String engineeringQuantity;

    private String chineseLabor;

    private String indonesiaLabor;

    private String installDepartment;

    private String installTeam;

    private String workContent;

    private BigDecimal quantity;

    private String parseMessage;

    @TableLogic
    private String delFlag;
}
