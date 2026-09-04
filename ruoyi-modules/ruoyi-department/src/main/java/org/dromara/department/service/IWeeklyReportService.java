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

    /**
     * 分页查询周报快照历史。
     *
     * @param bo        周报查询条件
     * @param pageQuery 分页参数
     * @return 分页周报快照
     */
    PageResult<WeeklyReportVo> queryPageList(WeeklyReportQueryBo bo, PageQuery pageQuery);

    /**
     * 查询周报快照详情。
     *
     * @param id 周报快照主键
     * @return 周报快照详情
     */
    WeeklyReportVo queryById(Long id);

    /**
     * 汇总指定周期的周报数据。
     *
     * @param bo 周报周期参数
     * @return 周报汇总数据
     */
    WeeklyReportSummaryVo buildSummary(WeeklyReportBo bo);

    /**
     * 生成并保存周报快照。
     *
     * @param bo 周报生成参数
     * @return 生成后的周报快照
     */
    WeeklyReportVo generate(WeeklyReportBo bo);

    /**
     * 删除周报快照。
     *
     * @param id 周报快照主键
     * @return 是否删除成功
     */
    Boolean deleteById(Long id);

    /**
     * 导出周报 PPTX 文件。
     *
     * @param id       周报快照主键
     * @param response HTTP响应对象
     * @throws Exception 导出或文件处理失败
     */
    void exportPptx(Long id, HttpServletResponse response) throws Exception;
}
