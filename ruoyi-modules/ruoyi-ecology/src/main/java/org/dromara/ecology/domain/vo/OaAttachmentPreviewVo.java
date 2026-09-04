package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 泛微申请附件预览内容。 */
@Data
public class OaAttachmentPreviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long ossId;
    private String fileName;
    private String contentType;
    private String previewType;
    private String message;
    private List<String> sheetNames;
    private String sheetName;
    private List<String> columnLabels;
    private List<Integer> rowNumbers;
    private List<List<String>> rows;
    private Boolean truncated;
}
