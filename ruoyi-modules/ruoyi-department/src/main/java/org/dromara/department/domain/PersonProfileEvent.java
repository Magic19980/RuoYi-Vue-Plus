package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDate;

/** 人员服务关系历史事件。人员档案结束服务不删除，事件用于追溯重复加入、移出和自动同步。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_person_profile_event")
public class PersonProfileEvent extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long profileId;

    private Long userId;

    private Long deptId;

    private String eventType;

    private LocalDate effectiveDate;

    private String memberType;

    private String reason;

    private Long operatorId;

    private String delFlag;
}
