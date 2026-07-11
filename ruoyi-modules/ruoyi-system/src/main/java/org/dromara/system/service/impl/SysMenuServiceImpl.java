package org.dromara.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.TreeBuildUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.SysMenu;
import org.dromara.system.domain.SysMenuI18n;
import org.dromara.system.domain.SysRole;
import org.dromara.system.domain.SysRoleMenu;
import org.dromara.system.domain.bo.SysMenuBo;
import org.dromara.system.domain.dto.MenuI18nItem;
import org.dromara.system.domain.vo.MetaVo;
import org.dromara.system.domain.vo.RouterVo;
import org.dromara.system.domain.vo.SysMenuVo;
import org.dromara.system.mapper.SysMenuI18nMapper;
import org.dromara.system.mapper.SysMenuMapper;
import org.dromara.system.mapper.SysRoleMapper;
import org.dromara.system.mapper.SysRoleMenuMapper;
import org.dromara.system.service.ISysMenuService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单 业务层处理
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysMenuServiceImpl implements ISysMenuService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuI18nMapper menuI18nMapper;

    /**
     * 根据用户查询系统菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenuVo> selectMenuList(Long userId) {
        return selectMenuList(new SysMenuBo(), userId);
    }

    /**
     * 查询系统菜单列表
     *
     * @param menu   菜单筛选条件
     * @param userId 当前查询的用户主键
     * @return 菜单列表
     */
    @Override
    public List<SysMenuVo> selectMenuList(SysMenuBo menu, Long userId) {
        // 管理员显示所有菜单信息 不是管理员 按用户id过滤菜单
        if (LoginHelper.isSuperAdmin(userId)) {
            return menuMapper.lambda()
                .likeIfText(SysMenu::getMenuName, menu.getMenuName())
                .eqIfText(SysMenu::getVisible, menu.getVisible())
                .eqIfText(SysMenu::getStatus, menu.getStatus())
                .eqIfText(SysMenu::getMenuType, menu.getMenuType())
                .eqIfPresent(SysMenu::getParentId, menu.getParentId())
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum)
                .voList();
        }
        return menuMapper.selectMenuListByUserId(menu, userId);
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByUserId(Long userId) {
        return menuMapper.selectMenuPermsByUserId(userId);
    }

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByRoleId(Long roleId) {
        return menuMapper.selectMenuPermsByRoleId(roleId);
    }

    /**
     * 根据角色ID列表批量查询权限
     *
     * @param roleIds 角色ID列表
     * @return 角色权限映射
     */
    @Override
    public Map<Long, Set<String>> selectMenuPermsByRoleIds(Collection<Long> roleIds) {
        return menuMapper.selectMenuPermsByRoleIds(roleIds);
    }

    /**
     * 根据用户ID查询菜单树信息
     *
     * @param userId 用户ID
     * @return 按树结构组织的菜单列表
     */
    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> menus;
        if (LoginHelper.isSuperAdmin(userId)) {
            menus = menuMapper.selectMenuTreeAll();
        } else {
            menus = menuMapper.selectMenuTreeByUserId(userId);
        }
        if (CollUtil.isEmpty(menus)) {
            return CollUtil.newArrayList();
        }

        List<SysMenu> menuTree = TreeBuildUtils.build(menus, Constants.TOP_PARENT_ID, SysMenu::getParentId, (menu, nodeTreeMaps) -> {
            // 将当前节点的菜单ID用作父节点ID
            Long menuParentId = menu.getMenuId();
            // 从动态规划表中取出子节点列表
            // 如果不存在子节点，则返回一个空的列表，确保数据在进行JSON序列化时该字段的类型和结构是正确的
            List<SysMenu> childMenus = nodeTreeMaps.getOrDefault(menuParentId, Collections.emptyList());
            // 设置子节点
            // 如果存在根节点指向尾节点的情况，则会出现环形依赖。但在菜单表中基本不会出现这种情况...
            menu.setChildren(childMenus);
        });
        return CollUtil.isEmpty(menuTree) ? CollUtil.newArrayList() : menuTree;
    }

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        return menuMapper.selectMenuListByRoleId(roleId, role.getMenuCheckStrictly());
    }

    /**
     * 构建前端路由所需要的菜单
     * 路由name命名规则 path首字母转大写 + id
     *
     * @param menus 菜单列表
     * @return 路由列表
     */
    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus) {
        if (CollUtil.isEmpty(menus)) {
            return CollUtil.newArrayList();
        }
        // 收集所有菜单ID，批量加载国际化数据
        Set<Long> menuIds = new LinkedHashSet<>();
        collectMenuIds(menus, menuIds);
        Map<Long, Map<String, String>> i18nMap = menuI18nMapper.selectI18nMapByMenuIds(menuIds);
        return buildMenusInternal(menus, i18nMap);
    }

    /**
     * 递归构建前端路由菜单（内部方法）
     */
    private List<RouterVo> buildMenusInternal(List<SysMenu> menus, Map<Long, Map<String, String>> i18nMap) {
        List<RouterVo> routers = new LinkedList<>();
        for (SysMenu menu : menus) {
            String name = menu.getRouteName() + menu.getMenuId();
            String menuTitle = resolveMenuTitle(menu, i18nMap);
            RouterVo router = new RouterVo();
            router.setHidden("1".equals(menu.getVisible()));
            router.setName(name);
            router.setPath(menu.getRouterPath());
            router.setComponent(menu.getComponentInfo());
            router.setQuery(menu.getQueryParam());
            router.setExt(menu.getExt());
            router.setMeta(new MetaVo(menuTitle, menu.getIcon(), StringUtils.equals(SystemConstants.NO, menu.getIsCache()), menu.getPath(), menu.getActiveMenu()));
            List<SysMenu> cMenus = menu.getChildren();
            if (CollUtil.isNotEmpty(cMenus) && SystemConstants.TYPE_DIR.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenusInternal(cMenus, i18nMap));
            } else if (menu.isMenuFrame()) {
                String frameName = StringUtils.capitalize(menu.getPath()) + menu.getMenuId();
                router.setMeta(null);
                List<RouterVo> childrenList = new ArrayList<>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(frameName);
                children.setMeta(new MetaVo(menuTitle, menu.getIcon(), StringUtils.equals(SystemConstants.NO, menu.getIsCache()), menu.getPath(), menu.getActiveMenu()));
                children.setQuery(menu.getQueryParam());
                children.setExt(menu.getExt());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (menu.getParentId().equals(Constants.TOP_PARENT_ID) && menu.isInnerLink()) {
                router.setMeta(new MetaVo(menuTitle, menu.getIcon()));
                router.setPath("/");
                List<RouterVo> childrenList = new ArrayList<>();
                RouterVo children = new RouterVo();
                String routerPath = SysMenu.innerLinkReplaceEach(menu.getPath());
                String innerLinkName = StringUtils.capitalize(routerPath) + menu.getMenuId();
                children.setPath(routerPath);
                children.setComponent(SystemConstants.INNER_LINK);
                children.setName(innerLinkName);
                children.setMeta(new MetaVo(menuTitle, menu.getIcon(), menu.getPath()));
                children.setExt(menu.getExt());
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<Tree<Long>> buildMenuTreeSelect(List<SysMenuVo> menus) {
        if (CollUtil.isEmpty(menus)) {
            return CollUtil.newArrayList();
        }
        // 收集所有菜单ID，批量加载国际化数据
        Set<Long> menuIds = menus.stream().map(SysMenuVo::getMenuId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, Map<String, String>> i18nMap = menuI18nMapper.selectI18nMapByMenuIds(menuIds);
        return TreeBuildUtils.build(menus, (menu, tree) -> {
            String title = resolveMenuTitle(menu.getMenuId(), menu.getMenuName(), i18nMap);
            Tree<Long> menuTree = tree.setId(menu.getMenuId())
                .setParentId(menu.getParentId())
                .setName(title)
                .setWeight(menu.getOrderNum());
            menuTree.put("menuType", menu.getMenuType());
            menuTree.put("icon", menu.getIcon());
            menuTree.put("visible", menu.getVisible());
            menuTree.put("status", menu.getStatus());
        });
    }

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @Override
    public SysMenuVo selectMenuById(Long menuId) {
        SysMenuVo vo = menuMapper.selectVoById(menuId);
        if (vo != null) {
            fillI18nList(vo);
        }
        return vo;
    }

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(Long menuId) {
        return menuMapper.lambda().eq(SysMenu::getParentId, menuId).exists();
    }

    /**
     * 是否存在菜单子节点
     *
     * @param menuIds 菜单ID列表
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(Collection<Long> menuIds) {
        return menuMapper.lambda()
            .in(SysMenu::getParentId, menuIds)
            .notIn(SysMenu::getMenuId, menuIds)
            .exists();
    }

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean checkMenuExistRole(Long menuId) {
        return roleMenuMapper.lambda().eq(SysRoleMenu::getMenuId, menuId).exists();
    }

    /**
     * 新增保存菜单信息
     *
     * @param bo 菜单信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMenu(SysMenuBo bo) {
        SysMenu menu = MapstructUtils.convert(bo, SysMenu.class);
        int result = menuMapper.insert(menu);
        // 保存国际化数据
        saveMenuI18n(menu.getMenuId(), bo.getI18nList());
        return result;
    }

    /**
     * 修改保存菜单信息
     *
     * @param bo 菜单信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMenu(SysMenuBo bo) {
        SysMenu menu = MapstructUtils.convert(bo, SysMenu.class);
        int result = menuMapper.updateById(menu);
        // 仅当前端显式传入 i18nList 时才更新国际化数据（null = 不修改，空列表 = 清空）
        if (bo.getI18nList() != null) {
            menuI18nMapper.deleteByMenuId(menu.getMenuId());
            saveMenuI18n(menu.getMenuId(), bo.getI18nList());
        }
        return result;
    }

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMenuById(Long menuId) {
        // 同时删除国际化数据
        menuI18nMapper.deleteByMenuId(menuId);
        return menuMapper.deleteById(menuId);
    }

    /**
     * 批量删除菜单管理信息
     *
     * @param menuIds 菜单ID串
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenuById(Collection<Long> menuIds) {
        // 同时批量删除国际化数据
        menuI18nMapper.deleteByMenuIds(menuIds);
        menuMapper.deleteByIds(menuIds);
        roleMenuMapper.deleteByMenuIds(menuIds);
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public boolean checkMenuNameUnique(SysMenuBo menu) {
        boolean exist = menuMapper.lambda()
            .eq(SysMenu::getMenuName, menu.getMenuName())
            .eq(SysMenu::getParentId, menu.getParentId())
            .neIfPresent(SysMenu::getMenuId, menu.getMenuId())
            .exists();
        return !exist;
    }

    /**
     * 校验路由组合是否唯一
     *
     * @param menuBo 菜单信息
     * @return 结果
     */
    @Override
    public boolean checkRouteConfigUnique(SysMenuBo menuBo) {
        SysMenu menu = MapstructUtils.convert(menuBo, SysMenu.class);
        if (SystemConstants.TYPE_BUTTON.equals(menu.getMenuType())) {
            return true;
        }
        long menuId = ObjectUtil.isNull(menu.getMenuId()) ? -1L : menu.getMenuId();
        Long parentId = menu.getParentId();
        String path = menu.getPath();
        String routeName = StringUtils.isEmpty(menu.getRouteName()) ? path : menu.getRouteName();
        List<SysMenu> sysMenuList = menuMapper.lambda()
            .in(SysMenu::getMenuType, SystemConstants.TYPE_DIR, SystemConstants.TYPE_MENU)
            .and(w ->
                w.eq(SysMenu::getPath, path).or().eq(SysMenu::getPath, routeName)
            ).list();
        for (SysMenu sysMenu : sysMenuList) {
            if (!sysMenu.getMenuId().equals(menuId)) {
                Long dbParentId = sysMenu.getParentId();
                String dbPath = sysMenu.getPath();
                String dbRouteName = StringUtils.isEmpty(sysMenu.getRouteName()) ? dbPath : sysMenu.getRouteName();
                if (StringUtils.equalsAnyIgnoreCase(path, dbPath) && parentId.equals(dbParentId)) {
                    log.warn("[同级路由冲突] 同级下已存在相同路由路径 '{}'，冲突菜单：{}", dbPath, sysMenu.getMenuName());
                    return false;
                } else if (StringUtils.equalsAnyIgnoreCase(path, dbPath)
                    && Constants.TOP_PARENT_ID.equals(parentId)
                    && Constants.TOP_PARENT_ID.equals(dbParentId)) {
                    log.warn("[根目录路由冲突] 根目录下路由 '{}' 必须唯一，已被菜单 '{}' 占用", path, sysMenu.getMenuName());
                    return false;
                } else if (StringUtils.equalsAnyIgnoreCase(routeName, dbRouteName)
                    && sysMenu.getMenuType().equals(menu.getMenuType())) {
                    log.warn("[路由名称冲突] 路由名称 '{}' 需全局唯一，已被菜单 '{}' 使用", routeName, sysMenu.getMenuName());
                    return false;
                }
            }
        }
        return true;
    }

    // ==================== 菜单国际化 辅助方法 ====================

    /**
     * 收集所有菜单ID（递归）
     */
    private void collectMenuIds(List<SysMenu> menus, Set<Long> menuIds) {
        if (CollUtil.isEmpty(menus)) {
            return;
        }
        for (SysMenu menu : menus) {
            menuIds.add(menu.getMenuId());
            if (CollUtil.isNotEmpty(menu.getChildren())) {
                collectMenuIds(menu.getChildren(), menuIds);
            }
        }
    }

    /**
     * 根据当前语言环境解析菜单标题（SysMenu对象版本）
     */
    private String resolveMenuTitle(SysMenu menu, Map<Long, Map<String, String>> i18nMap) {
        return resolveMenuTitle(menu.getMenuId(), menu.getMenuName(), i18nMap);
    }

    /**
     * 根据当前语言环境解析菜单标题
     * <p>
     * 优先查找当前 locale 对应的国际化名称，找不到则返回默认中文名称。
     */
    private String resolveMenuTitle(Long menuId, String defaultName, Map<Long, Map<String, String>> i18nMap) {
        if (i18nMap == null || i18nMap.isEmpty()) {
            return defaultName;
        }
        Map<String, String> i18nNames = i18nMap.get(menuId);
        if (i18nNames == null || i18nNames.isEmpty()) {
            return defaultName;
        }
        // 获取当前请求的 Locale
        String localeKey = LocaleContextHolder.getLocale().toString();
        // 先精确匹配，再尝试模糊匹配（如 en_US -> en）
        String i18nName = i18nNames.get(localeKey);
        if (i18nName == null && localeKey.contains("_")) {
            String langOnly = localeKey.substring(0, localeKey.indexOf('_'));
            i18nName = i18nNames.get(langOnly);
        }
        return StringUtils.isNotEmpty(i18nName) ? i18nName : defaultName;
    }

    /**
     * 保存菜单国际化数据（批量插入）
     */
    private void saveMenuI18n(Long menuId, List<MenuI18nItem> i18nList) {
        if (CollUtil.isEmpty(i18nList)) {
            return;
        }
        List<SysMenuI18n> entities = new ArrayList<>(i18nList.size());
        for (MenuI18nItem item : i18nList) {
            SysMenuI18n entity = new SysMenuI18n();
            entity.setMenuId(menuId);
            entity.setLocale(item.getLocale());
            entity.setMenuName(item.getMenuName());
            entities.add(entity);
        }
        menuI18nMapper.insertBatch(entities);
    }

    /**
     * 将国际化数据填充到 SysMenuVo 中
     */
    private void fillI18nList(SysMenuVo vo) {
        List<SysMenuI18n> entityList = menuI18nMapper.selectByMenuId(vo.getMenuId());
        if (CollUtil.isNotEmpty(entityList)) {
            List<MenuI18nItem> i18nList = new ArrayList<>(entityList.size());
            for (SysMenuI18n entity : entityList) {
                MenuI18nItem item = new MenuI18nItem();
                item.setLocale(entity.getLocale());
                item.setMenuName(entity.getMenuName());
                i18nList.add(item);
            }
            vo.setI18nList(i18nList);
        }
    }

}
