package org.dromara.department.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.department.domain.ScoreCategory;
import org.dromara.department.domain.bo.ScoreCategoryBo;
import org.dromara.department.domain.vo.ScoreCategoryVo;
import org.dromara.department.mapper.ScoreCategoryMapper;
import org.dromara.department.service.IScoreCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** SCORE 提案分类配置业务实现。 */
@RequiredArgsConstructor
@Service
public class ScoreCategoryServiceImpl implements IScoreCategoryService {

    private static final long ROOT_PARENT_ID = 0L;
    private static final int LEVEL_MAIN = 1;
    private static final int LEVEL_SUB = 2;
    private static final String STATUS_ENABLED = "ENABLED";
    private static final String STATUS_DISABLED = "DISABLED";

    private final ScoreCategoryMapper scoreCategoryMapper;

    @Override
    public List<ScoreCategoryVo> queryTree(boolean enabledOnly) {
        List<ScoreCategoryVo> source = enabledOnly ? scoreCategoryMapper.selectEnabledList() : scoreCategoryMapper.selectAllList();
        Map<Long, ScoreCategoryVo> nodeMap = new LinkedHashMap<>();
        source.forEach(node -> {
            node.setChildren(new ArrayList<>());
            nodeMap.put(node.getId(), node);
        });
        List<ScoreCategoryVo> roots = new ArrayList<>();
        source.forEach(node -> {
            Long parentId = normalizeParentId(node.getParentId());
            if (Objects.equals(parentId, ROOT_PARENT_ID)) {
                roots.add(node);
            } else {
                ScoreCategoryVo parent = nodeMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        });
        return roots;
    }

    @Override
    public ScoreCategoryVo queryById(Long id) {
        ScoreCategory entity = getById(id);
        return toVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ScoreCategoryBo bo) {
        ScoreCategory entity = new ScoreCategory();
        copyBo(bo, entity, null);
        return scoreCategoryMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ScoreCategoryBo bo) {
        ScoreCategory entity = getById(bo.getId());
        copyBo(bo, entity, entity.getId());
        return scoreCategoryMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            ScoreCategory entity = getById(id);
            if (scoreCategoryMapper.countChildren(id) > 0) {
                throw new ServiceException("分类“" + entity.getCategoryName() + "”下存在子分类，不能删除");
            }
            if (scoreCategoryMapper.countProposalReferences(entity.getId(), entity.getCategoryName(), entity.getCategoryLevel()) > 0) {
                throw new ServiceException("分类“" + entity.getCategoryName() + "”已被SCORE提案使用，只能停用不能删除");
            }
        }
        return scoreCategoryMapper.deleteByIds(ids) > 0;
    }

    private void copyBo(ScoreCategoryBo bo, ScoreCategory entity, Long currentId) {
        Long parentId = normalizeParentId(bo.getParentId());
        String categoryName = StringUtils.trim(bo.getCategoryName());
        if (StringUtils.isBlank(categoryName)) {
            throw new ServiceException("分类名称不能为空");
        }
        if (currentId != null && Objects.equals(currentId, parentId)) {
            throw new ServiceException("分类不能设置自己为父分类");
        }
        ScoreCategory parent = null;
        if (!Objects.equals(parentId, ROOT_PARENT_ID)) {
            parent = getById(parentId);
            if (!Objects.equals(parent.getParentId(), ROOT_PARENT_ID) || !Objects.equals(parent.getCategoryLevel(), LEVEL_MAIN)) {
                throw new ServiceException("只能在提案大类下维护提案小类");
            }
            if (STATUS_DISABLED.equals(parent.getStatus())) {
                throw new ServiceException("停用的大类不能新增或调整小类");
            }
        }
        if (scoreCategoryMapper.countDuplicate(parentId, categoryName, currentId == null ? 0L : currentId) > 0) {
            throw new ServiceException("同一父分类下已存在同名分类");
        }
        String status = normalizeStatus(bo.getStatus());
        int level = parent == null ? LEVEL_MAIN : LEVEL_SUB;
        if (level == LEVEL_MAIN && STATUS_DISABLED.equals(status) && scoreCategoryMapper.countEnabledChildren(entity.getId()) > 0) {
            throw new ServiceException("该大类下存在启用的小类，请先停用小类");
        }
        entity.setParentId(parentId);
        entity.setCategoryName(categoryName);
        entity.setCategoryLevel(level);
        entity.setSortNum(bo.getSortNum() == null ? 0 : bo.getSortNum());
        entity.setStatus(status);
        entity.setRemark(bo.getRemark());
    }

    private ScoreCategory getById(Long id) {
        ScoreCategory entity = scoreCategoryMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("SCORE分类不存在");
        }
        return entity;
    }

    private ScoreCategoryVo toVo(ScoreCategory entity) {
        ScoreCategoryVo vo = new ScoreCategoryVo();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setCategoryName(entity.getCategoryName());
        vo.setCategoryLevel(entity.getCategoryLevel());
        vo.setSortNum(entity.getSortNum());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }

    private String normalizeStatus(String status) {
        return STATUS_DISABLED.equalsIgnoreCase(status) || "停用".equals(status) ? STATUS_DISABLED : STATUS_ENABLED;
    }
}
