package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 平台问题与建议附件视图。
 */
@Data
public class PlatformFeedbackAttachmentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long ossId;

    private String originalName;

    private String fileSuffix;

    private Long fileSize;

    private String url;
}
