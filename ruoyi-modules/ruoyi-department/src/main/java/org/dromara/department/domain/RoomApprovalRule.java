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

/** 房间预约的串行审批规则。每个步骤配置一个审批主体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_approval_rule")
public class RoomApprovalRule extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long roomId;

    private Integer stepNo;

    /** USER、DEPT、ROLE、ALL。 */
    private String approverType;

    /** ALL 主体为空，其余主体对应用户、科室或角色主键。 */
    private Long approverId;

    /** 当前步骤提交后允许等待审批的分钟数，0 表示不自动超时。 */
    private Integer timeoutMinutes;

    private Boolean enabled;

    private String remark;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
