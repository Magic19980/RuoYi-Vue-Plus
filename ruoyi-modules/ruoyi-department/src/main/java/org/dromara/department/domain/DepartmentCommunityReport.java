package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 协作社区举报实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_community_report")
public class DepartmentCommunityReport extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long postId;

    private Long reporterUserId;

    private String reason;

    private String status;

    private Long handledBy;

    private LocalDateTime handledAt;

    private String handleNote;

    @TableLogic
    private String delFlag;
}
