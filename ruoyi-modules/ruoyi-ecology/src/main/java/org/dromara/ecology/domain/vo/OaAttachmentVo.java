package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微申请附件视图。 */
@Data
public class OaAttachmentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long applicationId;
    private Long processId;
    private Long ossId;
    private String attachmentType;
    private String fileName;
    private String fileUrl;
    private Integer sortNo;
    private String uploadStatus;
    private String oaFileId;
    private String oaFilePath;
    private String failReason;
}
