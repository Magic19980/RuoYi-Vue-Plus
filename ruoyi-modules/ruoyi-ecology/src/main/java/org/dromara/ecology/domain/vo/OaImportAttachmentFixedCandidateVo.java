package org.dromara.ecology.domain.vo;

import lombok.Data;

/** 附件模板固定区域候选单元格。 */
@Data
public class OaImportAttachmentFixedCandidateVo {

    /** 单元格地址，例如 A6。 */
    private String cell;

    /** 行号，从 0 开始。 */
    private Integer row;

    /** 列号，从 0 开始。 */
    private Integer column;

    /** 模板中的原始显示内容。 */
    private String originalContent;

    /** 自动识别出的标签部分，例如“制单：”。 */
    private String label;

    /** 自动识别出的原始值部分，例如“唐宗婷”。 */
    private String originalValue;

    /** 是否位于合并区域左上角。 */
    private Boolean merged;
}
