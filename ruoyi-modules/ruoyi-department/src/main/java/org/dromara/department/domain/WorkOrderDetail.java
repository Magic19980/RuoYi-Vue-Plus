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
 * PDF 人工统计明细实体，对应人工单原始表格的一行。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_work_order_detail")
public class WorkOrderDetail extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 工单明细主键。 */
    private Long id;

    /** 所属工单主键。 */
    private Long workOrderId;

    /** 来源PDF页码。 */
    private Integer sourcePage;

    /** 来源表格中的行序号。 */
    private Integer sequenceNo;

    /** 请求部门。 */
    private String requestDept;

    /** 结算单位。 */
    private String settlementUnit;

    /** 项目负责人。 */
    private String projectOwner;

    /** 项目名称。 */
    private String projectName;

    /** 项目特征。 */
    private String projectFeature;

    /** 计量单位。 */
    private String unit;

    /** 工程量原始文本。 */
    private String engineeringQuantity;

    /** 中文人工内容。 */
    private String chineseLabor;

    /** 印尼语人工内容。 */
    private String indonesiaLabor;

    /** 安装部门。 */
    private String installDepartment;

    /** 安装班组。 */
    private String installTeam;

    /** 工作内容。 */
    private String workContent;

    /** 解析后的数量。 */
    private BigDecimal quantity;

    /** 明细解析说明或异常信息。 */
    private String parseMessage;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
