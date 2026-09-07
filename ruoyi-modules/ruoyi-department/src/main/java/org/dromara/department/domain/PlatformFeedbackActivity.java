package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 平台问题与建议操作记录实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_platform_feedback_activity")
public class PlatformFeedbackActivity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long feedbackId;

    private String actionType;

    private String actionNote;

    private String fromStatus;

    private String toStatus;

    @TableLogic
    private String delFlag;
}
