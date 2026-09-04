package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** Excel 模板解析预览结果。 */
@Data
public class OaImportTemplatePreviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sheetName;
    /** 表头行使用 Excel 的自然行号，从 0 开始，和导入配置保持一致。 */
    private Integer headerRow;
    private Integer sampleRowCount;
    private List<OaImportTemplateFieldVo> fields;
    private String fieldDefinitionsJson;
    private String deptField;
    private String companyField;
    private String groupByJson;
}
