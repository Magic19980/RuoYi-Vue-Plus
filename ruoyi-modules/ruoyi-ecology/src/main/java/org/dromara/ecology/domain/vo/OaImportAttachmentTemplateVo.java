package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 附件模板上传后的解析结果。 */
@Data
public class OaImportAttachmentTemplateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long ossId;
    private String fileName;
    private String sheetName;
    private List<String> sheetNames;
    /** 模板表头行，从 0 开始。 */
    private Integer headerRow;
    /** 默认明细起始行，从 0 开始。 */
    private Integer dataStartRow;
    /** 模板中识别出的列标题。 */
    private List<String> headers;
    /** 模板中的列及其原始位置，空标题列用于保留模板公式或固定内容。 */
    private List<OaImportAttachmentColumnVo> columns;
    /** 根据“合计/总计”文本识别出的合计行，从 0 开始。 */
    private Integer totalRow;

    /** 明细和合计区域之外的固定内容候选，需要管理员确认后才会参与生成。 */
    private List<OaImportAttachmentFixedCandidateVo> fixedCandidates;
}
