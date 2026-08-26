package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.DepartmentProject;
import org.dromara.department.domain.bo.DepartmentProjectBo;
import org.dromara.department.domain.bo.DepartmentProjectQueryBo;
import org.dromara.department.domain.vo.DepartmentProjectVo;
import org.dromara.department.mapper.DepartmentProjectMapper;
import org.dromara.department.service.IDepartmentProjectService;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.DepartmentScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 科室项目主数据业务实现。
 */
@RequiredArgsConstructor
@Service
public class DepartmentProjectServiceImpl implements IDepartmentProjectService {

    private static final String STATUS_ENABLED = "ENABLED";
    private static final String STATUS_DISABLED = "DISABLED";

    private final DepartmentProjectMapper departmentProjectMapper;
    private final DepartmentAccessService departmentAccessService;

    @Override
    public PageResult<DepartmentProjectVo> queryPageList(DepartmentProjectQueryBo bo, PageQuery pageQuery) {
        Page<DepartmentProjectVo> page = pageQuery.build();
        Page<DepartmentProjectVo> result = departmentProjectMapper.selectPageList(page, bo == null ? new DepartmentProjectQueryBo() : bo, scope());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public DepartmentProjectVo queryById(Long id) {
        DepartmentProject entity = getAccessible(id);
        return toVo(entity);
    }

    @Override
    public List<DepartmentProjectVo> queryOptions() {
        return departmentProjectMapper.selectOptions(scope());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(DepartmentProjectBo bo) {
        requireDept("新增项目");
        DepartmentProject entity = new DepartmentProject();
        entity.setDeptId(departmentAccessService.currentDeptId());
        copyBo(bo, entity);
        checkDuplicate(entity);
        return departmentProjectMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(DepartmentProjectBo bo) {
        DepartmentProject entity = getAccessible(bo.getId());
        copyBo(bo, entity);
        checkDuplicate(entity);
        return departmentProjectMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            getAccessible(id);
            if (departmentProjectMapper.countOperationRecords(id) > 0) {
                throw new ServiceException("项目已绑定运维工单，请先解除工单绑定后再删除");
            }
        }
        return departmentProjectMapper.deleteByIds(ids) > 0;
    }

    private void copyBo(DepartmentProjectBo bo, DepartmentProject entity) {
        entity.setProjectCode(StringUtils.trim(bo.getProjectCode()));
        entity.setProjectName(StringUtils.trim(bo.getProjectName()));
        entity.setProjectType(StringUtils.trim(bo.getProjectType()));
        entity.setResponsiblePerson(StringUtils.trim(bo.getResponsiblePerson()));
        entity.setStatus(StringUtils.isBlank(bo.getStatus()) ? STATUS_ENABLED : normalizeStatus(bo.getStatus()));
        entity.setSortNum(bo.getSortNum() == null ? 0 : bo.getSortNum());
        entity.setRemark(bo.getRemark());
    }

    private void checkDuplicate(DepartmentProject entity) {
        DepartmentProject duplicate = departmentProjectMapper.selectOne(Wrappers.<DepartmentProject>lambdaQuery()
            .eq(DepartmentProject::getDeptId, entity.getDeptId())
            .eq(DepartmentProject::getProjectName, entity.getProjectName())
            .ne(entity.getId() != null, DepartmentProject::getId, entity.getId()));
        if (duplicate != null) {
            throw new ServiceException("本部门已存在同名项目");
        }
    }

    private String normalizeStatus(String status) {
        return "DISABLED".equalsIgnoreCase(status) || "停用".equals(status) ? STATUS_DISABLED : STATUS_ENABLED;
    }

    private DepartmentProject getAccessible(Long id) {
        DepartmentProject entity = departmentProjectMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("项目不存在");
        }
        if (departmentAccessService.canViewEntityDept(entity.getDeptId(), "department:project:viewDept")) {
            return entity;
        }
        throw new ServiceException("您没有访问该项目的权限");
    }

    private DepartmentProjectVo toVo(DepartmentProject entity) {
        DepartmentProjectVo vo = new DepartmentProjectVo();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setProjectCode(entity.getProjectCode());
        vo.setProjectName(entity.getProjectName());
        vo.setProjectType(entity.getProjectType());
        vo.setResponsiblePerson(entity.getResponsiblePerson());
        vo.setStatus(entity.getStatus());
        vo.setSortNum(entity.getSortNum());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private void requireDept(String action) {
        if (departmentAccessService.currentDeptId() == null) {
            throw new ServiceException("当前登录用户缺少部门信息，无法" + action);
        }
    }

    private DepartmentScope scope() {
        return departmentAccessService.scope("department:project:viewDept");
    }

}
