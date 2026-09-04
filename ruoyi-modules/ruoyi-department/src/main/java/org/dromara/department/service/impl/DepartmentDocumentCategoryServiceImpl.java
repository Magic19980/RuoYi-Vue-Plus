package org.dromara.department.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.department.domain.DepartmentDocumentCategory;
import org.dromara.department.domain.bo.DepartmentDocumentCategoryBo;
import org.dromara.department.domain.bo.DepartmentDocumentCategoryQueryBo;
import org.dromara.department.domain.vo.DepartmentDocumentCategoryVo;
import org.dromara.department.mapper.DepartmentDocumentCategoryMapper;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.DepartmentScope;
import org.dromara.department.service.IDepartmentDocumentCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** 科室资料分类配置业务实现。 */
@RequiredArgsConstructor
@Service
public class DepartmentDocumentCategoryServiceImpl implements IDepartmentDocumentCategoryService {

    private static final String STATUS_ENABLED = "ENABLED";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String NORMAL = "0";
    private static final long ROOT_PARENT_ID = 0L;

    private final DepartmentDocumentCategoryMapper categoryMapper;
    private final DepartmentAccessService departmentAccessService;

    @Override
    public List<DepartmentDocumentCategoryVo> queryOptions() {
        requireDept("获取资料分类");
        return buildTree(categoryMapper.selectOptions(DepartmentScope.current(departmentAccessService.currentDeptId())), null, true);
    }

    @Override
    public List<DepartmentDocumentCategoryVo> queryTreeList(DepartmentDocumentCategoryQueryBo bo) {
        List<DepartmentDocumentCategoryVo> source = categoryMapper.selectTreeList(scope());
        return buildTree(source, bo, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(DepartmentDocumentCategoryBo bo) {
        requireDept("新增资料分类");
        DepartmentDocumentCategory entity = new DepartmentDocumentCategory();
        entity.setDeptId(departmentAccessService.currentDeptId());
        copyBo(bo, entity, null);
        checkDuplicate(entity);
        return categoryMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(DepartmentDocumentCategoryBo bo) {
        DepartmentDocumentCategory entity = getAccessible(bo.getId());
        copyBo(bo, entity, entity.getId());
        checkDuplicate(entity);
        return categoryMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            DepartmentDocumentCategory entity = getAccessible(id);
            if (categoryMapper.countChildren(entity.getDeptId(), normalizeParentId(entity.getId())) > 0) {
                throw new ServiceException("分类“" + entity.getCategoryName() + "”下存在子分类，请先删除或移动子分类");
            }
            if (categoryMapper.countDocuments(id) > 0) {
                throw new ServiceException("分类“" + entity.getCategoryName() + "”已被资料使用，请先调整资料分类后再删除");
            }
        }
        return categoryMapper.deleteByIds(ids) > 0;
    }

    private void copyBo(DepartmentDocumentCategoryBo bo, DepartmentDocumentCategory entity, Long currentId) {
        if (bo == null) {
            throw new ServiceException("资料分类信息不能为空");
        }
        String categoryName = StringUtils.trim(bo.getCategoryName());
        if (StringUtils.isBlank(categoryName)) {
            throw new ServiceException("分类名称不能为空");
        }
        if (categoryName.length() > 100) {
            throw new ServiceException("分类名称不能超过100个字符");
        }
        Long parentId = normalizeParentId(bo.getParentId());
        validateParent(entity.getDeptId(), parentId, currentId);
        entity.setParentId(parentId);
        entity.setCategoryName(categoryName);
        entity.setSortNum(bo.getSortNum() == null ? 0 : Math.max(bo.getSortNum(), 0));
        entity.setStatus(normalizeStatus(bo.getStatus()));
        if (STATUS_DISABLED.equals(entity.getStatus()) && categoryMapper.countEnabledChildren(entity.getDeptId(), entity.getId() == null ? -1L : entity.getId()) > 0) {
            throw new ServiceException("该分类下存在启用的子分类，请先停用子分类");
        }
        String remark = StringUtils.trim(bo.getRemark());
        if (remark != null && remark.length() > 500) {
            throw new ServiceException("备注不能超过500个字符");
        }
        entity.setRemark(remark);
    }

    private void checkDuplicate(DepartmentDocumentCategory entity) {
        if (categoryMapper.countDuplicate(entity.getDeptId(), normalizeParentId(entity.getParentId()), entity.getCategoryName(), entity.getId() == null ? 0L : entity.getId()) > 0) {
            throw new ServiceException("同一上级分类下已存在同名资料分类");
        }
    }

    private void validateParent(Long deptId, Long parentId, Long currentId) {
        if (Objects.equals(parentId, ROOT_PARENT_ID)) {
            return;
        }
        DepartmentDocumentCategory parent = getAccessible(parentId);
        if (!Objects.equals(parent.getDeptId(), deptId)) {
            throw new ServiceException("上级分类不属于当前科室");
        }
        if (STATUS_DISABLED.equals(parent.getStatus())) {
            throw new ServiceException("停用分类不能作为上级分类");
        }
        Set<Long> visited = new LinkedHashSet<>();
        Long cursor = parent.getId();
        while (!Objects.equals(cursor, ROOT_PARENT_ID)) {
            if (!visited.add(cursor)) {
                throw new ServiceException("资料分类层级存在循环引用");
            }
            if (Objects.equals(cursor, currentId)) {
                throw new ServiceException("不能将分类移动到自己的子分类下");
            }
            DepartmentDocumentCategory current = categoryMapper.selectById(cursor);
            if (current == null || !Objects.equals(current.getDelFlag(), NORMAL)) {
                throw new ServiceException("上级分类不存在");
            }
            cursor = normalizeParentId(current.getParentId());
        }
    }

    private DepartmentDocumentCategory getAccessible(Long id) {
        DepartmentDocumentCategory entity = categoryMapper.selectById(id);
        if (entity == null || !Objects.equals(entity.getDelFlag(), NORMAL)) {
            throw new ServiceException("资料分类不存在");
        }
        if (!departmentAccessService.canViewEntityDept(entity.getDeptId(), "department:documentCategory:viewDept")) {
            throw new ServiceException("您没有访问该资料分类的权限");
        }
        return entity;
    }

    private List<DepartmentDocumentCategoryVo> buildTree(List<DepartmentDocumentCategoryVo> source,
                                                         DepartmentDocumentCategoryQueryBo bo,
                                                         boolean enabledOnly) {
        Map<Long, DepartmentDocumentCategoryVo> nodeMap = new LinkedHashMap<>();
        for (DepartmentDocumentCategoryVo node : source) {
            node.setParentId(normalizeParentId(node.getParentId()));
            node.setChildren(new ArrayList<>());
            nodeMap.put(node.getId(), node);
        }

        Set<Long> keep = new LinkedHashSet<>();
        for (DepartmentDocumentCategoryVo node : source) {
            if (matches(node, bo, enabledOnly)) {
                keep.add(node.getId());
            }
        }
        if (bo != null && (StringUtils.isNotBlank(bo.getCategoryName()) || StringUtils.isNotBlank(bo.getStatus()))) {
            for (Long id : new ArrayList<>(keep)) {
                DepartmentDocumentCategoryVo node = nodeMap.get(id);
                while (node != null && !Objects.equals(node.getParentId(), ROOT_PARENT_ID)) {
                    keep.add(node.getParentId());
                    node = nodeMap.get(node.getParentId());
                }
            }
        } else {
            keep.addAll(nodeMap.keySet());
        }

        List<DepartmentDocumentCategoryVo> roots = new ArrayList<>();
        for (DepartmentDocumentCategoryVo node : source) {
            if (!keep.contains(node.getId())) {
                continue;
            }
            Long parentId = normalizeParentId(node.getParentId());
            if (Objects.equals(parentId, ROOT_PARENT_ID)) {
                roots.add(node);
            } else {
                DepartmentDocumentCategoryVo parent = nodeMap.get(parentId);
                if (parent != null && keep.contains(parent.getId())) {
                    parent.getChildren().add(node);
                }
            }
        }
        return roots;
    }

    private boolean matches(DepartmentDocumentCategoryVo node, DepartmentDocumentCategoryQueryBo bo, boolean enabledOnly) {
        if (enabledOnly && !STATUS_ENABLED.equals(node.getStatus())) {
            return false;
        }
        if (bo == null) {
            return true;
        }
        if (StringUtils.isNotBlank(bo.getStatus()) && !bo.getStatus().equalsIgnoreCase(node.getStatus())) {
            return false;
        }
        return StringUtils.isBlank(bo.getCategoryName())
            || (node.getCategoryName() != null && node.getCategoryName().toLowerCase().contains(bo.getCategoryName().toLowerCase()));
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }

    private String normalizeStatus(String status) {
        return STATUS_DISABLED.equalsIgnoreCase(status) || "停用".equals(status) ? STATUS_DISABLED : STATUS_ENABLED;
    }

    private void requireDept(String action) {
        if (departmentAccessService.currentDeptId() == null) {
            throw new ServiceException("当前登录用户缺少部门信息，无法" + action);
        }
    }

    private DepartmentScope scope() {
        return departmentAccessService.scope("department:documentCategory:viewDept");
    }
}
