package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 菜单国际化表 sys_menu_i18n
 *
 * @author Lion Li
 */
@Data
@TableName("sys_menu_i18n")
public class SysMenuI18n {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 语言区域（如 en_US, id_ID）
     */
    private String locale;

    /**
     * 菜单名称
     */
    private String menuName;

}
