package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/** 房间预约配额策略。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_quota_policy")
public class RoomQuotaPolicy extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long roomId;

    /** USER、DEPT、ROLE、ALL。 */
    private String subjectType;

    private Long subjectId;

    /** DAY、WEEK、MONTH。 */
    private String periodType;

    /** 0 表示不限制。 */
    private Integer quotaMinutes;

    /** 0 表示不限制。 */
    private Integer quotaCount;

    /** BLOCK 超额拦截，APPROVAL 超额转审批。 */
    private String overQuotaAction;

    /** 每小时收费金额；为0表示只做配额不计费。 */
    private BigDecimal unitPrice;

    /** 释放或取消时的退款比例，0-100。 */
    private BigDecimal refundRate;

    private Boolean enabled;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
