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
 * 日报附件归档关系实体，对应 {@code dm_daily_report_attachment} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_daily_report_attachment")
public class DailyReportAttachment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 附件关联主键。 */
    private Long id;

    /** 日报主键。 */
    private Long reportId;

    /** 对象存储文件主键。 */
    private Long ossId;

    /** 附件原始文件名。 */
    private String originalName;

    /** 文件类型或扩展名。 */
    private String fileType;

    /** 同一日报下的显示顺序。 */
    private Integer sortNum;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
