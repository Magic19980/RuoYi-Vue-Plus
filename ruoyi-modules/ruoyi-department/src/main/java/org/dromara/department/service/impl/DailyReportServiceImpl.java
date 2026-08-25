package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.DailyReport;
import org.dromara.department.domain.DailyReportStatus;
import org.dromara.department.domain.bo.DailyReportBo;
import org.dromara.department.domain.bo.DailyReportQueryBo;
import org.dromara.department.domain.vo.DailyReportImportVo;
import org.dromara.department.domain.vo.DailyReportVo;
import org.dromara.department.mapper.DailyReportMapper;
import org.dromara.department.service.DepartmentContextResolver;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.IDailyCalendarService;
import org.dromara.department.service.IDailyReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 日报业务实现。
 */
@RequiredArgsConstructor
@Service
public class DailyReportServiceImpl implements IDailyReportService {

    private static final String DEPT_VIEW_PERMISSION = "department:dailyReport:viewDept";

    private final DailyReportMapper dailyReportMapper;
    private final IDailyCalendarService dailyCalendarService;
    private final DepartmentContextResolver departmentContextResolver;
    private final DepartmentAccessService departmentAccessService;

    @Override
    public PageResult<DailyReportVo> queryPageList(DailyReportQueryBo bo, PageQuery pageQuery) {
        Page<DailyReportVo> page = pageQuery.build();
        Page<DailyReportVo> result = dailyReportMapper.selectPageList(page, bo, LoginHelper.getUserId(), scopeDeptId(), canViewAll());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<DailyReportVo> queryList(DailyReportQueryBo bo) {
        Page<DailyReportVo> page = new Page<>(1, Integer.MAX_VALUE);
        return dailyReportMapper.selectPageList(page, bo, LoginHelper.getUserId(), scopeDeptId(), canViewAll()).getRecords();
    }

    @Override
    public DailyReportVo queryById(Long id) {
        DailyReportQueryBo bo = new DailyReportQueryBo();
        bo.setId(id);
        Page<DailyReportVo> page = new Page<>(1, 1);
        List<DailyReportVo> rows = dailyReportMapper.selectPageList(page, bo, LoginHelper.getUserId(), scopeDeptId(), canViewAll()).getRecords();
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(DailyReportBo bo) {
        Long userId = LoginHelper.getUserId();
        Long deptId = currentDeptId();
        if (userId == null || deptId == null) {
            throw new ServiceException("当前登录用户缺少部门信息，无法填写日报");
        }
        ensureWorkday(deptId, userId, bo.getReportDate());
        long count = dailyReportMapper.selectCount(Wrappers.<DailyReport>lambdaQuery()
            .eq(DailyReport::getReportDate, bo.getReportDate())
            .eq(DailyReport::getUserId, userId)
            .eq(DailyReport::getDeptId, deptId));
        if (count > 0) {
            throw new ServiceException("该日期已存在日报，请直接修改已有日报");
        }
        DailyReport entity = new DailyReport();
        entity.setReportDate(bo.getReportDate());
        entity.setUserId(userId);
        entity.setDeptId(deptId);
        entity.setTodayWork(bo.getTodayWork());
        entity.setTomorrowPlan(bo.getTomorrowPlan());
        entity.setCoordinationNote(bo.getCoordinationNote());
        entity.setStatus(DailyReportStatus.SUBMITTED);
        entity.setSourceType("WEB");
        return dailyReportMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(DailyReportBo bo) {
        DailyReport entity = getOwnedEditable(bo.getId());
        ensureWorkday(entity.getDeptId(), entity.getUserId(), bo.getReportDate());
        entity.setReportDate(bo.getReportDate());
        entity.setTodayWork(bo.getTodayWork());
        entity.setTomorrowPlan(bo.getTomorrowPlan());
        entity.setCoordinationNote(bo.getCoordinationNote());
        // 日报被人工修改后，解除休假自动生成关联。
        entity.setLeaveId(null);
        entity.setSourceType("WEB");
        return dailyReportMapper.updateById(entity) > 0;
    }

    @Override
    public void checkEditable(Long id) {
        getOwnedEditable(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        for (Long id : ids) {
            DailyReport entity = getAccessible(id);
            if (!Objects.equals(entity.getUserId(), LoginHelper.getUserId())) {
                throw new ServiceException("只能删除本人日报");
            }
        }
        return dailyReportMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importData(List<DailyReportImportVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return "未读取到日报数据";
        }
        int success = 0;
        int skipped = 0;
        Long currentUserId = LoginHelper.getUserId();
        Long currentDeptId = currentDeptId();
        for (DailyReportImportVo row : rows) {
            if (row.getReportDate() == null || StringUtils.isBlank(row.getTodayWork())) {
                skipped++;
                continue;
            }
            Long targetUserId = currentUserId;
            if (StringUtils.isNotBlank(row.getUserName())) {
                if (!canViewDepartment()) {
                    throw new ServiceException("普通用户导入时不能指定其他填报人");
                }
                targetUserId = dailyReportMapper.selectUserIdByName(row.getUserName());
                if (targetUserId == null) {
                    throw new ServiceException("找不到填报人：" + row.getUserName());
                }
            }
            Long targetDeptId = currentDeptId;
            if (targetDeptId == null || dailyReportMapper.countMemberInDeptAt(targetUserId, targetDeptId, row.getReportDate()) == 0) {
                throw new ServiceException("填报人尚未纳入当前科室人员档案");
            }
            if (!canViewAll() && !Objects.equals(targetDeptId, currentDeptId)) {
                throw new ServiceException("只能导入本部门人员的日报");
            }
            ensureWorkday(targetDeptId, targetUserId, row.getReportDate());
            DailyReport existing = dailyReportMapper.selectOne(Wrappers.<DailyReport>lambdaQuery()
                .eq(DailyReport::getReportDate, row.getReportDate())
                .eq(DailyReport::getUserId, targetUserId)
                .eq(DailyReport::getDeptId, targetDeptId));
            if (existing != null) {
                skipped++;
                continue;
            }
            DailyReport entity = new DailyReport();
            entity.setReportDate(row.getReportDate());
            entity.setUserId(targetUserId);
            entity.setDeptId(targetDeptId);
            entity.setTodayWork(row.getTodayWork());
            entity.setTomorrowPlan(row.getTomorrowPlan());
            entity.setCoordinationNote(row.getCoordinationNote());
            entity.setStatus(DailyReportStatus.SUBMITTED);
            entity.setSourceType("IMPORT");
            dailyReportMapper.insert(entity);
            success++;
        }
        return String.format("导入完成：成功 %d 条，跳过 %d 条", success, skipped);
    }

    private DailyReport getOwnedEditable(Long id) {
        DailyReport entity = getAccessible(id);
        if (!Objects.equals(entity.getUserId(), LoginHelper.getUserId())) {
            throw new ServiceException("只能维护本人日报");
        }
        if (!DailyReportStatus.editable(entity.getStatus())) {
            throw new ServiceException("当前日报状态不允许修改");
        }
        return entity;
    }

    private void ensureWorkday(Long deptId, Long userId, java.time.LocalDate reportDate) {
        if (reportDate != null && reportDate.isAfter(java.time.LocalDate.now())) {
            throw new ServiceException("不能填写未来日期的日报");
        }
        if (reportDate == null || !dailyCalendarService.isDailyReportRequired(deptId, userId, reportDate)) {
            throw new ServiceException("当前成员未分配日报任务，无需填写日报");
        }
        if (!dailyCalendarService.isWorkday(deptId, userId, reportDate)) {
            throw new ServiceException("该日期为休息日，无需填写日报；如需填写请先配置为调休上班");
        }
    }

    private DailyReport getAccessible(Long id) {
        DailyReport entity = dailyReportMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("日报不存在");
        }
        if (canViewAll()) {
            return entity;
        }
        if (departmentAccessService.canViewEntityDept(entity.getDeptId(), DEPT_VIEW_PERMISSION)) {
            return entity;
        }
        if (Objects.equals(entity.getUserId(), LoginHelper.getUserId())) {
            return entity;
        }
        throw new ServiceException("您没有访问该日报的权限");
    }

    private Long scopeDeptId() {
        return canViewAll() ? null : departmentAccessService.scopeDeptId(DEPT_VIEW_PERMISSION);
    }

    private Long currentDeptId() {
        return departmentContextResolver.resolveCurrentDeptId();
    }

    private boolean canViewAll() {
        return false;
    }

    private boolean canViewDepartment() {
        return departmentAccessService.canViewCurrentDepartment(DEPT_VIEW_PERMISSION);
    }
}
