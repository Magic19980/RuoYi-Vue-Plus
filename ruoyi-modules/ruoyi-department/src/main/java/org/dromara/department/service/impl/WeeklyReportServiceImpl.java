package org.dromara.department.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.WeeklyReport;
import org.dromara.department.domain.WeeklyReportStatus;
import org.dromara.department.domain.bo.DailyReportQueryBo;
import org.dromara.department.domain.bo.PersonProfileQueryBo;
import org.dromara.department.domain.bo.WeeklyReportBo;
import org.dromara.department.domain.bo.WeeklyReportQueryBo;
import org.dromara.department.domain.vo.DailyReportVo;
import org.dromara.department.domain.vo.PersonProfileVo;
import org.dromara.department.domain.vo.WeeklyReportItemVo;
import org.dromara.department.domain.vo.WeeklyReportSummaryVo;
import org.dromara.department.domain.vo.WeeklyReportVo;
import org.dromara.department.mapper.WeeklyReportMapper;
import org.dromara.department.service.IDailyReportService;
import org.dromara.department.service.IOperationLedgerService;
import org.dromara.department.service.IPersonProfileService;
import org.dromara.department.service.IWeeklyReportService;
import org.dromara.department.service.IWorkOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 周报业务实现。
 */
@RequiredArgsConstructor
@Service
public class WeeklyReportServiceImpl implements IWeeklyReportService {

    private static final String DEPT_VIEW_PERMISSION = "department:weeklyReport:viewDept";
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final WeeklyReportMapper weeklyReportMapper;
    private final IDailyReportService dailyReportService;
    private final IPersonProfileService personProfileService;
    private final IWorkOrderService workOrderService;
    private final IOperationLedgerService operationLedgerService;
    private final WeeklyReportPptxService weeklyReportPptxService;

    @Override
    public PageResult<WeeklyReportVo> queryPageList(WeeklyReportQueryBo bo, PageQuery pageQuery) {
        Page<WeeklyReport> page = pageQuery.build();
        Page<WeeklyReport> result = weeklyReportMapper.selectPage(page, buildScopeWrapper(bo));
        List<WeeklyReportVo> rows = result.getRecords().stream().map(this::toVo).toList();
        return PageResult.build(rows, result.getTotal());
    }

    @Override
    public WeeklyReportVo queryById(Long id) {
        return toVo(getAccessible(id));
    }

    @Override
    public WeeklyReportSummaryVo buildSummary(WeeklyReportBo bo) {
        LocalDate weekStart = requireWeekStart(bo.getWeekStart());
        LocalDate weekEnd = normalizeWeekEnd(weekStart, bo.getWeekEnd());
        DailyReportQueryBo query = new DailyReportQueryBo();
        query.setBeginDate(weekStart);
        query.setEndDate(weekEnd);
        List<DailyReportVo> reports = dailyReportService.queryList(query);

        PersonProfileQueryBo profileQuery = new PersonProfileQueryBo();
        profileQuery.setDailyReportEnabled("1");
        List<PersonProfileVo> profiles = personProfileService.queryList(profileQuery);

        WeeklyReportSummaryVo summary = new WeeklyReportSummaryVo();
        summary.setWeekStart(weekStart);
        summary.setWeekEnd(weekEnd);
        summary.setReportCount(reports.size());
        summary.setRequiredUserCount(profiles.size());
        summary.setReportCountByDate(new java.util.LinkedHashMap<>());
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            summary.getReportCountByDate().put(date, 0);
        }

        Set<Long> filledUserIds = new LinkedHashSet<>();
        List<WeeklyReportItemVo> items = new ArrayList<>();
        for (DailyReportVo report : reports) {
            filledUserIds.add(report.getUserId());
            summary.getReportCountByDate().computeIfPresent(report.getReportDate(), (key, value) -> value + 1);
            WeeklyReportItemVo item = new WeeklyReportItemVo();
            item.setReportDate(report.getReportDate());
            item.setUserName(displayName(report.getNickName(), report.getUserName()));
            item.setTodayWork(report.getTodayWork());
            item.setTomorrowPlan(report.getTomorrowPlan());
            item.setCoordinationNote(report.getCoordinationNote());
            items.add(item);
            if (StringUtils.isNotBlank(report.getTomorrowPlan())) {
                summary.getTomorrowPlans().add(item.getUserName() + "：" + report.getTomorrowPlan());
            }
            if (StringUtils.isNotBlank(report.getCoordinationNote())) {
                summary.getCoordinationNotes().add(item.getUserName() + "：" + report.getCoordinationNote());
            }
        }
        items.sort(Comparator.comparing(WeeklyReportItemVo::getReportDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(item -> Objects.toString(item.getUserName(), "")));
        summary.setReportItems(items);

        List<String> missingNames = new ArrayList<>();
        for (PersonProfileVo profile : profiles) {
            if (!filledUserIds.contains(profile.getUserId())) {
                missingNames.add(displayName(profile.getNickName(), profile.getUserName()));
            }
        }
        summary.setFilledUserCount(filledUserIds.size());
        summary.setMissingUserCount(missingNames.size());
        summary.setMissingUserNames(missingNames);
        summary.setManualOrderSummary(workOrderService.buildSummary(weekStart, weekEnd));
        summary.setOperationSummary(operationLedgerService.buildSummary(weekStart, weekEnd));
        // 保留旧字段，避免历史前端或旧快照读取时报空；新周报统一使用两个明确的数据源字段。
        summary.setWorkOrderSummary(summary.getManualOrderSummary());
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReportVo generate(WeeklyReportBo bo) {
        WeeklyReportSummaryVo summary = buildSummary(bo);
        String title = StringUtils.isBlank(bo.getTitle())
            ? "科室周报 " + summary.getWeekStart() + " 至 " + summary.getWeekEnd()
            : bo.getTitle().trim();
        WeeklyReport entity = weeklyReportMapper.selectOne(Wrappers.<WeeklyReport>lambdaQuery()
            .eq(WeeklyReport::getWeekStart, summary.getWeekStart())
            .eq(WeeklyReport::getWeekEnd, summary.getWeekEnd()));
        if (entity == null) {
            entity = new WeeklyReport();
            entity.setWeekStart(summary.getWeekStart());
            entity.setWeekEnd(summary.getWeekEnd());
        }
        entity.setTitle(title);
        entity.setReportCount(summary.getReportCount());
        entity.setRequiredUserCount(summary.getRequiredUserCount());
        entity.setFilledUserCount(summary.getFilledUserCount());
        entity.setMissingUserCount(summary.getMissingUserCount());
        entity.setStatus(WeeklyReportStatus.GENERATED);
        entity.setSnapshotJson(JsonUtils.toJsonString(summary));
        if (entity.getId() == null) {
            weeklyReportMapper.insert(entity);
        } else {
            weeklyReportMapper.updateById(entity);
        }
        return toVo(entity);
    }

    @Override
    public void exportPptx(Long id, HttpServletResponse response) throws Exception {
        WeeklyReport entity = getAccessible(id);
        WeeklyReportSummaryVo summary = JsonUtils.parseObject(entity.getSnapshotJson(), WeeklyReportSummaryVo.class);
        if (summary == null) {
            throw new ServiceException("周报快照不存在，请重新生成");
        }
        byte[] bytes = weeklyReportPptxService.generate(summary, entity.getTitle());
        String filename = "weekly_report_" + entity.getWeekStart().format(FILE_DATE_FORMATTER) + ".pptx";
        response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''"
            + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"));
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private WeeklyReportVo toVo(WeeklyReport entity) {
        if (entity == null) {
            return null;
        }
        WeeklyReportVo vo = new WeeklyReportVo();
        vo.setId(entity.getId());
        vo.setWeekStart(entity.getWeekStart());
        vo.setWeekEnd(entity.getWeekEnd());
        vo.setTitle(entity.getTitle());
        vo.setReportCount(entity.getReportCount());
        vo.setRequiredUserCount(entity.getRequiredUserCount());
        vo.setFilledUserCount(entity.getFilledUserCount());
        vo.setMissingUserCount(entity.getMissingUserCount());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private WeeklyReport getAccessible(Long id) {
        WeeklyReport entity = weeklyReportMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("周报不存在");
        }
        if (canViewAll()) {
            return entity;
        }
        if (canViewDepartment() && Objects.equals(entity.getCreateDept(), LoginHelper.getDeptId())) {
            return entity;
        }
        if (Objects.equals(entity.getCreateBy(), LoginHelper.getUserId())) {
            return entity;
        }
        throw new ServiceException("您没有访问该周报的权限");
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WeeklyReport> buildScopeWrapper(WeeklyReportQueryBo bo) {
        var wrapper = Wrappers.<WeeklyReport>lambdaQuery();
        if (bo != null) {
            wrapper.ge(bo.getBeginDate() != null, WeeklyReport::getWeekStart, bo.getBeginDate())
                .le(bo.getEndDate() != null, WeeklyReport::getWeekStart, bo.getEndDate())
                .eq(StringUtils.isNotBlank(bo.getStatus()), WeeklyReport::getStatus, bo.getStatus());
        }
        if (canViewAll()) {
            return wrapper.orderByDesc(WeeklyReport::getWeekStart);
        }
        if (canViewDepartment()) {
            return wrapper.eq(WeeklyReport::getCreateDept, LoginHelper.getDeptId()).orderByDesc(WeeklyReport::getWeekStart);
        }
        return wrapper.eq(WeeklyReport::getCreateBy, LoginHelper.getUserId()).orderByDesc(WeeklyReport::getWeekStart);
    }

    private LocalDate requireWeekStart(LocalDate weekStart) {
        if (weekStart == null) {
            throw new ServiceException("周开始日期不能为空");
        }
        return weekStart.with(DayOfWeek.MONDAY);
    }

    private LocalDate normalizeWeekEnd(LocalDate weekStart, LocalDate weekEnd) {
        LocalDate end = weekEnd == null ? weekStart.plusDays(6) : weekEnd;
        if (end.isBefore(weekStart) || end.isAfter(weekStart.plusDays(6))) {
            throw new ServiceException("周报日期范围必须为同一周的7天");
        }
        return weekStart.plusDays(6);
    }

    private String displayName(String nickName, String userName) {
        return StringUtils.isNotBlank(nickName) ? nickName : userName;
    }

    private boolean canViewAll() {
        return LoginHelper.isSuperAdmin();
    }

    private boolean canViewDepartment() {
        return canViewAll() || StpUtil.hasPermission(DEPT_VIEW_PERMISSION);
    }
}
