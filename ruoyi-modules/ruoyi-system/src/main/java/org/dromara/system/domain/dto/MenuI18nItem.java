package org.dromara.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单国际化项
 *
 * @author Lion Li
 */
@Data
@Schema(description = "菜单国际化项")
public class MenuI18nItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "语言区域（en_US / id_ID 等）", example = "en_US")
    private String locale;

    @Schema(description = "国际化名称", example = "System Management")
    private String menuName;

}
