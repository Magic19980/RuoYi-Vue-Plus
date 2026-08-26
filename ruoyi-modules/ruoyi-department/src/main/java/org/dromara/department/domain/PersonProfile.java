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
 * 科室人员业务档案对象 dm_person_profile。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_person_profile")
public class PersonProfile extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String employeeNo;

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

    private LocalDateTime endedAt;

    private Long endedBy;

    private String endReason;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
