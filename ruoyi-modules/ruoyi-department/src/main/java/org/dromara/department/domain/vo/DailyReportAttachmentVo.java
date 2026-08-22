package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 日报附件视图对象。
 */
@Data
public class DailyReportAttachmentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long reportId;

    private Long ossId;

    private String originalName;

    private String fileType;

    private Integer sortNum;

    private String url;
}
