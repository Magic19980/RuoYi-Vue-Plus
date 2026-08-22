package org.dromara.department.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.PersonProfile;
import org.dromara.department.domain.bo.PersonProfileBo;
import org.dromara.department.domain.bo.PersonProfileQueryBo;
import org.dromara.department.domain.vo.PersonProfileVo;
import org.dromara.department.domain.vo.PersonProfileImportVo;
import org.dromara.department.domain.vo.PersonUserOptionVo;
import org.dromara.department.mapper.PersonProfileMapper;
import org.dromara.department.service.IPersonProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 人员档案业务实现。
 */
@RequiredArgsConstructor
@Service
public class PersonProfileServiceImpl implements IPersonProfileService {

    private static final String DEPT_VIEW_PERMISSION = "department:person:viewDept";

    private final PersonProfileMapper personProfileMapper;

    @Override
    public PageResult<PersonProfileVo> queryPageList(PersonProfileQueryBo bo, PageQuery pageQuery) {
        Page<PersonProfileVo> page = pageQuery.build();
        Page<PersonProfileVo> result = personProfileMapper.selectPageList(page, bo, LoginHelper.getUserId(), scopeDeptId(), canViewAll());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public PersonProfileVo queryById(Long id) {
        PersonProfile entity = getAccessible(id);
        PersonProfileQueryBo bo = new PersonProfileQueryBo();
        bo.setId(id);
        Page<PersonProfileVo> page = new Page<>(1, 1);
        Page<PersonProfileVo> result = personProfileMapper.selectPageList(page, bo, entity.getUserId(), entity.getCreateDept(), true);
        return result.getRecords().isEmpty() ? null : result.getRecords().get(0);
    }

    @Override
    public List<PersonProfileVo> queryList(PersonProfileQueryBo bo) {
        Page<PersonProfileVo> page = new Page<>(1, Integer.MAX_VALUE);
        return personProfileMapper.selectPageList(page, bo, LoginHelper.getUserId(), scopeDeptId(), canViewAll()).getRecords();
    }

    @Override
    public List<PersonUserOptionVo> queryUserOptions() {
        return personProfileMapper.selectUserOptions(scopeDeptId(), canViewAll());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(PersonProfileBo bo) {
        PersonUserOptionVo user = findUserOption(bo.getUserId());
        checkTargetDept(user.getDeptId());
        long count = personProfileMapper.selectCount(Wrappers.<PersonProfile>lambdaQuery()
            .eq(PersonProfile::getUserId, bo.getUserId()));
        if (count > 0) {
            throw new ServiceException("该系统用户已经存在人员档案");
        }
        PersonProfile entity = new PersonProfile();
        fillEntity(entity, bo);
        return personProfileMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(PersonProfileBo bo) {
        PersonProfile entity = getAccessible(bo.getId());
        PersonUserOptionVo user = findUserOption(bo.getUserId());
        checkTargetDept(user.getDeptId());
        if (!Objects.equals(entity.getUserId(), bo.getUserId())) {
            long count = personProfileMapper.selectCount(Wrappers.<PersonProfile>lambdaQuery()
                .eq(PersonProfile::getUserId, bo.getUserId()));
            if (count > 0) {
                throw new ServiceException("该系统用户已经存在人员档案");
            }
        }
        fillEntity(entity, bo);
        return personProfileMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        for (Long id : ids) {
            getAccessible(id);
        }
        return personProfileMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importData(List<PersonProfileImportVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return "未读取到人员档案数据";
        }
        int success = 0;
        int skipped = 0;
        for (PersonProfileImportVo row : rows) {
            if (org.dromara.common.core.utils.StringUtils.isBlank(row.getUserName())) {
                skipped++;
                continue;
            }
            PersonUserOptionVo user = personProfileMapper.selectUserOptions(null, true).stream()
                .filter(item -> row.getUserName().equals(item.getUserName()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到有效系统用户：" + row.getUserName()));
            checkTargetDept(user.getDeptId());
            PersonProfile entity = personProfileMapper.selectOne(Wrappers.<PersonProfile>lambdaQuery()
                .eq(PersonProfile::getUserId, user.getUserId()));
            if (entity == null) {
                entity = new PersonProfile();
                entity.setUserId(user.getUserId());
                fillEntity(entity, row);
                personProfileMapper.insert(entity);
            } else {
                fillEntity(entity, row);
                personProfileMapper.updateById(entity);
            }
            success++;
        }
        return String.format("导入完成：成功 %d 条，跳过 %d 条", success, skipped);
    }

    private void fillEntity(PersonProfile entity, PersonProfileBo bo) {
        entity.setUserId(bo.getUserId());
        entity.setEmployeeNo(bo.getEmployeeNo());
        entity.setJobTitle(bo.getJobTitle());
        entity.setDailyReportEnabled(bo.getDailyReportEnabled() == null ? "1" : bo.getDailyReportEnabled());
        entity.setReminderTime(bo.getReminderTime());
        entity.setRemark(bo.getRemark());
    }

    private void fillEntity(PersonProfile entity, PersonProfileImportVo row) {
        entity.setEmployeeNo(row.getEmployeeNo());
        entity.setJobTitle(row.getJobTitle());
        entity.setDailyReportEnabled(row.getDailyReportEnabled() == null ? "1" : row.getDailyReportEnabled());
        entity.setReminderTime(row.getReminderTime());
        entity.setRemark(row.getRemark());
    }

    private PersonUserOptionVo findUserOption(Long userId) {
        return personProfileMapper.selectUserOptions(null, true).stream()
            .filter(item -> Objects.equals(item.getUserId(), userId))
            .findFirst()
            .orElseThrow(() -> new ServiceException("系统用户不存在或已停用"));
    }

    private void checkTargetDept(Long targetDeptId) {
        if (!canViewAll() && !Objects.equals(targetDeptId, LoginHelper.getDeptId())) {
            throw new ServiceException("只能维护本部门人员档案");
        }
    }

    private PersonProfile getAccessible(Long id) {
        PersonProfile entity = personProfileMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("人员档案不存在");
        }
        if (canViewAll()) {
            return entity;
        }
        if (canViewDepartment() && Objects.equals(personProfileMapper.selectDeptIdByProfileId(id), LoginHelper.getDeptId())) {
            return entity;
        }
        if (Objects.equals(entity.getUserId(), LoginHelper.getUserId())) {
            return entity;
        }
        throw new ServiceException("您没有访问该人员档案的权限");
    }

    private Long scopeDeptId() {
        return canViewDepartment() && !canViewAll() ? LoginHelper.getDeptId() : null;
    }

    private boolean canViewAll() {
        return LoginHelper.isSuperAdmin();
    }

    private boolean canViewDepartment() {
        return canViewAll() || StpUtil.hasPermission(DEPT_VIEW_PERMISSION);
    }
}
