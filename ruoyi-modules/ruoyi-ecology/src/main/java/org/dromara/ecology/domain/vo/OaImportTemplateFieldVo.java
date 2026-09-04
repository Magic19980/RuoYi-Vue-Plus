package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Excel 模板解析出的字段信息，供管理员确认后生成导入配置。 */
@Data
public class OaImportTemplateFieldVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;
    private String header;
    private String type;
    private String sample;
    private Boolean required;
    private Boolean uniqueKey;
    private String role;
}
