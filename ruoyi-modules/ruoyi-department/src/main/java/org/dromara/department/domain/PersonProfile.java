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
import java.time.LocalTime;

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

    private String jobTitle;

    private String dailyReportEnabled;

    private LocalTime reminderTime;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
