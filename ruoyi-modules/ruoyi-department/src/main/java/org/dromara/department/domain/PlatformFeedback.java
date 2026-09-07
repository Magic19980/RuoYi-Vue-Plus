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
 * 平台问题与建议实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_platform_feedback")
public class PlatformFeedback extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String feedbackNo;

    private String feedbackType;

    private String title;

    private String description;

    private String moduleName;

    private String pageTitle;

    private String pagePath;

    private String businessRef;

    private String reproduceSteps;

    private String expectedResult;

    private String actualResult;

    private String impactScope;

    private String priority;

    private String status;

    private Long assigneeId;

    private String resolutionNote;

    private String attachmentOssIds;

    private LocalDateTime closedAt;

    @TableLogic
    private String delFlag;
}
