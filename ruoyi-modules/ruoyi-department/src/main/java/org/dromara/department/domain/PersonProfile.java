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
import java.time.LocalDateTime;

/**
 * 科室人员业务档案实体，对应 {@code dm_person_profile} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_person_profile")
public class PersonProfile extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 人员档案主键。 */
    private Long id;

    /** 系统用户主键。 */
    private Long userId;

    /** 人员工号快照。 */
    private String employeeNo;

    /** 档案备注。 */
    private String remark;

    /** 纳入目标科室的生效日期。 */
    private LocalDate joinDate;

    /** 离开生效日期（不含当日），空表示仍在服务。 */
    private LocalDate leaveDate;

    /** FULL正式成员 / TEMP临时协作成员。 */
    private String memberType;

    /** ACTIVE有效 / ENDED已结束。 */
    private String memberStatus;

    /** MANUAL人工纳入 / AUTO_MAIN由系统用户主部门自动同步。 */
    private String memberSource;

    /** 服务关系结束时间。 */
    private LocalDateTime endedAt;

    /** 结束服务操作人用户主键。 */
    private Long endedBy;

    /** 结束服务原因。 */
    private String endReason;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
