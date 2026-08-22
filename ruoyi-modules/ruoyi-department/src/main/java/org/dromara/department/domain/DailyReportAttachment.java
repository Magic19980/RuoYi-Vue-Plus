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
 * 日报附件归档关系对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_daily_report_attachment")
public class DailyReportAttachment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long reportId;

    private Long ossId;

    private String originalName;

    private String fileType;

    private Integer sortNum;

    @TableLogic
    private String delFlag;
}
