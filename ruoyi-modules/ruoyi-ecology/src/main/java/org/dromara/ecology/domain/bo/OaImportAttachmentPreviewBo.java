package org.dromara.ecology.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/** 提交泛微前预览某个导入分组附件的参数。 */
@Data
public class OaImportAttachmentPreviewBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 与导入明细上的 groupKey 一致；空分组使用 __ALL__。 */
    private String groupKey;

    /** 附件模板中的参数替换值。 */
    private Map<String, Object> parameters;
}
