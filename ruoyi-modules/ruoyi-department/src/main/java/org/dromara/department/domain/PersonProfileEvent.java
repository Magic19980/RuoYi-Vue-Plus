package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 人员服务关系历史事件实体，对应 {@code dm_person_profile_event} 表。
 *
 * <p>人员档案结束服务不删除，事件用于追溯重复加入、移出和自动同步。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_person_profile_event")
public class PersonProfileEvent extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 服务关系事件主键。 */
    private Long id;

    /** 人员档案主键。 */
    private Long profileId;

    /** 系统用户主键。 */
    private Long userId;

    /** 业务科室主键。 */
    private Long deptId;

    /** 事件类型。 */
    private String eventType;

    /** 事件生效日期。 */
    private LocalDate effectiveDate;

    /** 成员类型。 */
    private String memberType;

    /** 事件原因或说明。 */
    private String reason;

    /** 操作人用户主键。 */
    private Long operatorId;

    /** 逻辑删除标记。 */
    private String delFlag;
}
