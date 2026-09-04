package org.dromara.department.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.WeeklyReportBo;
import org.dromara.department.domain.bo.WeeklyReportQueryBo;
import org.dromara.department.domain.vo.WeeklyReportSummaryVo;
import org.dromara.department.domain.vo.WeeklyReportVo;

/**
 * 周报业务接口。
 */
public interface IWeeklyReportService {

    PageResult<WeeklyReportVo> queryPageList(WeeklyReportQueryBo bo, PageQuery pageQuery);

    WeeklyReportVo queryById(Long id);

    WeeklyReportSummaryVo buildSummary(WeeklyReportBo bo);

    WeeklyReportVo generate(WeeklyReportBo bo);

    Boolean deleteById(Long id);

    void exportPptx(Long id, HttpServletResponse response) throws Exception;
}
