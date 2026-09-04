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
import java.time.LocalDate;

/** 科室业务审核人配置实体，对应 {@code dm_review_rule} 表。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_review_rule")
public class DepartmentReviewRule extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 审核规则主键。 */
    private Long id;
    /** 业务科室主键。 */
    private Long deptId;
    /** 业务任务类型。 */
    private String taskType;
    /** 主审核人用户主键。 */
    private Long reviewerUserId;
    /** 备用审核人用户主键。 */
    private Long backupReviewerUserId;
    /** 规则生效开始日期，包含当天。 */
    private LocalDate effectiveStart;
    /** 规则生效结束日期，包含当天。 */
    private LocalDate effectiveEnd;
    /** 规则状态。 */
    private String status;
    /** 规则备注。 */
    private String remark;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
