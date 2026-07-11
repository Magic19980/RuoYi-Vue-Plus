package org.dromara.system.mapper;

import cn.hutool.core.collection.CollUtil;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.system.domain.SysMenuI18n;

import java.util.*;

/**
 * 菜单国际化 数据层
 *
 * @author Lion Li
 */
public interface SysMenuI18nMapper extends BaseMapperPlus<SysMenuI18n, SysMenuI18n> {

    /**
     * 根据菜单ID列表查询国际化数据
     *
     * @param menuIds 菜单ID列表
     * @return 菜单ID -> (locale -> menuName)
     */
    default Map<Long, Map<String, String>> selectI18nMapByMenuIds(Collection<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return Map.of();
        }
        List<SysMenuI18n> list = this.lambda()
            .in(SysMenuI18n::getMenuId, menuIds)
            .list();
        Map<Long, Map<String, String>> result = new LinkedHashMap<>();
        for (SysMenuI18n item : list) {
            result.computeIfAbsent(item.getMenuId(), k -> new LinkedHashMap<>())
                .put(item.getLocale(), item.getMenuName());
        }
        return result;
    }

    /**
     * 根据菜单ID查询国际化数据
     *
     * @param menuId 菜单ID
     * @return locale -> menuName
     */
    default List<SysMenuI18n> selectByMenuId(Long menuId) {
        if (menuId == null) {
            return List.of();
        }
        return this.lambda()
            .eq(SysMenuI18n::getMenuId, menuId)
            .list();
    }

    /**
     * 根据菜单ID删除国际化数据
     *
     * @param menuId 菜单ID
     */
    default void deleteByMenuId(Long menuId) {
        this.lambda().eq(SysMenuI18n::getMenuId, menuId).delete();
    }

    /**
     * 根据菜单ID列表批量删除国际化数据
     *
     * @param menuIds 菜单ID列表
     */
    default void deleteByMenuIds(Collection<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return;
        }
        this.lambda().in(SysMenuI18n::getMenuId, menuIds).delete();
    }

}
