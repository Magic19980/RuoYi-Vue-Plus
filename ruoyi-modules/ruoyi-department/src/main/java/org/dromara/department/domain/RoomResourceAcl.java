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

/** 房间级用户、科室和角色授权。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_room_resource_acl")
public class RoomResourceAcl extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long roomId;

    /** USER、DEPT、ROLE 或 ALL。 */
    private String subjectType;

    private Long subjectId;

    /** VIEW 查看、BOOK 预约、APPROVE 审批。 */
    private String permissionType;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
