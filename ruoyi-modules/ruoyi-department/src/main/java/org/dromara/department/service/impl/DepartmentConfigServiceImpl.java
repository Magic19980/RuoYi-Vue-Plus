package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.DepartmentConfig;
import org.dromara.department.domain.bo.DepartmentConfigBo;
import org.dromara.department.domain.bo.DepartmentConfigMigrationBo;
import org.dromara.department.domain.bo.DepartmentConfigQueryBo;
import org.dromara.department.domain.vo.DepartmentConfigVo;
import org.dromara.department.mapper.DepartmentConfigMapper;
import org.dromara.department.mapper.DepartmentConfigMigrationMapper;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.DepartmentMembershipSyncService;
import org.dromara.department.service.DepartmentScope;
import org.dromara.department.service.IDepartmentConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/** 业务科室配置服务实现。 */
@RequiredArgsConstructor
@Service
public class DepartmentConfigServiceImpl implements IDepartmentConfigService {

    private static final String ENABLED = "ENABLED";
    private static final String DISABLED = "DISABLED";

    private final DepartmentConfigMapper departmentConfigMapper;
    private final DepartmentConfigMigrationMapper migrationMapper;
    private final DepartmentAccessService departmentAccessService;
    private final DepartmentMembershipSyncService membershipSyncService;

    @Override
    public PageResult<DepartmentConfigVo> queryPageList(DepartmentConfigQueryBo bo, org.dromara.common.mybatis.core.page.PageQuery pageQuery) {
        DepartmentScope scope = LoginHelper.isSuperAdmin()
            ? DepartmentScope.all()
            : DepartmentScope.current(departmentAccessService.currentDeptId());
        Page<DepartmentConfigVo> page = departmentConfigMapper.selectPageList(
            pageQuery.build(), bo == null ? new DepartmentConfigQueryBo() : bo, scope);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public DepartmentConfigVo queryById(Long deptId) {
        DepartmentConfigVo result = departmentConfigMapper.selectDetail(deptId);
        if (result == null || (!LoginHelper.isSuperAdmin()
            && !departmentAccessService.canViewEntityDept(deptId, "department:department:viewDept"))) {
            throw new ServiceException("业务科室不存在或无权访问");
        }
        return result;
    }

    @Override
    public List<DepartmentConfigVo> queryAvailableDepartments(String deptName) {
        if (StringUtils.isBlank(deptName)) {
            return List.of();
        }
        List<DepartmentConfigVo> result = departmentConfigMapper.selectAvailableDepartments(deptName.trim());
        if (!LoginHelper.isSuperAdmin()) {
            Long currentDeptId = departmentAccessService.currentDeptId();
            return result.stream().filter(item -> Objects.equals(item.getDeptId(), currentDeptId)).toList();
        }
        return result;
    }

    @Override
    public List<DepartmentConfigVo> queryOrganizationChildren(Long parentId) {
        List<DepartmentConfigVo> result = departmentConfigMapper.selectOrganizationChildren(parentId == null ? 0L : parentId);
        if (!LoginHelper.isSuperAdmin()) {
            Long currentDeptId = departmentAccessService.currentDeptId();
            return result.stream().filter(item -> Objects.equals(item.getDeptId(), currentDeptId)).toList();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(DepartmentConfigBo bo) {
        checkTargetDept(bo.getDeptId());
        if (departmentConfigMapper.selectById(bo.getDeptId()) != null) {
            throw new ServiceException("该系统部门已经配置为业务科室");
        }
        DepartmentConfig entity = new DepartmentConfig();
        entity.setDeptId(bo.getDeptId());
        copyBo(bo, entity);
        boolean success = departmentConfigMapper.insert(entity) > 0;
        if (success && ENABLED.equals(entity.getStatus())) {
            membershipSyncService.syncConfiguredDepartment(entity.getDeptId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(DepartmentConfigBo bo) {
        DepartmentConfig entity = departmentConfigMapper.selectById(bo.getDeptId());
        if (entity == null) {
            throw new ServiceException("业务科室配置不存在");
        }
        checkTargetDept(entity.getDeptId());
        String oldStatus = entity.getStatus();
        copyBo(bo, entity);
        boolean success = departmentConfigMapper.updateById(entity) > 0;
        if (success && ENABLED.equals(entity.getStatus())) {
            membershipSyncService.syncConfiguredDepartment(entity.getDeptId());
        } else if (success && ENABLED.equals(oldStatus)) {
            membershipSyncService.disableDepartmentAutoMemberships(entity.getDeptId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void migrate(DepartmentConfigMigrationBo bo) {
        if (!LoginHelper.isSuperAdmin()) {
            throw new ServiceException("科室配置迁移仅允许超级管理员操作");
        }
        Long sourceDeptId = bo.getSourceDeptId();
        Long targetDeptId = bo.getTargetDeptId();
        if (Objects.equals(sourceDeptId, targetDeptId)) {
            throw new ServiceException("原科室和目标部门不能相同");
        }
        if (migrationMapper.selectConfigForUpdate(sourceDeptId) == null) {
            throw new ServiceException("原科室配置不存在、已迁移或已删除");
        }
        if (departmentConfigMapper.countActiveSystemDepartment(targetDeptId) == 0) {
            throw new ServiceException("目标部门不存在或已停用，请先完成泛微组织同步");
        }
        if (migrationMapper.countAnyConfig(targetDeptId) > 0) {
            throw new ServiceException("目标部门已经存在科室配置，请先处理目标配置");
        }

        List<org.dromara.department.domain.vo.DepartmentConfigMigrationConflictVo> conflicts =
            migrationMapper.selectConflicts(sourceDeptId, targetDeptId);
        if (!conflicts.isEmpty()) {
            StringJoiner message = new StringJoiner("、");
            conflicts.forEach(item -> message.add(item.getDataName() + " " + item.getConflictCount() + " 条"));
            throw new ServiceException("迁移存在重复数据，已取消本次迁移：" + message);
        }

        Long operatorId = LoginHelper.getUserId() == null ? 0L : LoginHelper.getUserId();
        int movedConfig = migrationMapper.moveConfig(sourceDeptId, targetDeptId, operatorId, java.time.LocalDateTime.now());
        if (movedConfig != 1) {
            throw new ServiceException("科室配置已发生变化，请刷新后重试");
        }
        migrationMapper.movePersonProfiles(sourceDeptId, targetDeptId);
        migrationMapper.movePersonProfileEvents(sourceDeptId, targetDeptId);
        migrationMapper.moveDailyReports(sourceDeptId, targetDeptId);
        migrationMapper.moveDailyCalendarOverrides(sourceDeptId, targetDeptId);
        migrationMapper.moveDailyLeaves(sourceDeptId, targetDeptId);
        migrationMapper.moveDocumentCategories(sourceDeptId, targetDeptId);
        migrationMapper.moveDocuments(sourceDeptId, targetDeptId);
        migrationMapper.moveWorkOrders(sourceDeptId, targetDeptId);
        migrationMapper.moveProjects(sourceDeptId, targetDeptId);
        migrationMapper.moveOperationRecords(sourceDeptId, targetDeptId);
        migrationMapper.moveOperationSystems(sourceDeptId, targetDeptId);
        migrationMapper.moveFiveWhy(sourceDeptId, targetDeptId);
        migrationMapper.moveScoreProposals(sourceDeptId, targetDeptId);
        migrationMapper.moveScoreProposalReviewTasks(sourceDeptId, targetDeptId);
        migrationMapper.moveReviewRules(sourceDeptId, targetDeptId);
        migrationMapper.moveTaskRules(sourceDeptId, targetDeptId);
        migrationMapper.moveTaskAssignments(sourceDeptId, targetDeptId);
        migrationMapper.moveTaskInstances(sourceDeptId, targetDeptId);

        DepartmentConfig targetConfig = departmentConfigMapper.selectById(targetDeptId);
        if (targetConfig != null && ENABLED.equals(targetConfig.getStatus())) {
            membershipSyncService.syncConfiguredDepartment(targetDeptId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disableByIds(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return false;
        }
        for (Long deptId : deptIds) {
            DepartmentConfig entity = departmentConfigMapper.selectById(deptId);
            if (entity == null) {
                continue;
            }
            checkTargetDept(deptId);
            entity.setStatus(DISABLED);
            departmentConfigMapper.updateById(entity);
            membershipSyncService.disableDepartmentAutoMemberships(deptId);
        }
        return true;
    }

    private void checkTargetDept(Long deptId) {
        if (deptId == null) {
            throw new ServiceException("业务科室不能为空");
        }
        if (departmentConfigMapper.countActiveSystemDepartment(deptId) == 0) {
            throw new ServiceException("目标系统部门不存在或已停用");
        }
        if (!LoginHelper.isSuperAdmin() && !Objects.equals(departmentAccessService.currentDeptId(), deptId)) {
            throw new ServiceException("只能配置当前服务科室");
        }
    }

    private void copyBo(DepartmentConfigBo bo, DepartmentConfig entity) {
        entity.setStatus(DISABLED.equalsIgnoreCase(bo.getStatus()) ? DISABLED : ENABLED);
        entity.setManagerUserId(bo.getManagerUserId());
        entity.setSortNum(bo.getSortNum() == null ? 0 : Math.max(bo.getSortNum(), 0));
        entity.setRemark(StringUtils.trim(bo.getRemark()));
    }
}
