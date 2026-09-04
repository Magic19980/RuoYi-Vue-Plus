package org.dromara.department.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.DailyReportBo;
import org.dromara.department.domain.bo.DailyReportQueryBo;
import org.dromara.department.domain.vo.DailyReportImportVo;
import org.dromara.department.domain.vo.DailyReportVo;

import java.util.Collection;
import java.util.List;

/**
 * 日报业务接口。
 */
public interface IDailyReportService {

    /**
     * 分页查询日报记录。
     *
     * @param bo        日报查询条件
     * @param pageQuery 分页参数
     * @return 分页日报数据
     */
    PageResult<DailyReportVo> queryPageList(DailyReportQueryBo bo, PageQuery pageQuery);

    /**
     * 查询日报列表。
     *
     * @param bo 日报查询条件
     * @return 日报数据列表
     */
    List<DailyReportVo> queryList(DailyReportQueryBo bo);

    /**
     * 导出日报 Excel 文件。
     *
     * @param bo       日报查询条件
     * @param response HTTP响应对象
     */
    void exportXlsx(DailyReportQueryBo bo, HttpServletResponse response);

    /**
     * 导出日报导入模板。
     *
     * @param response HTTP响应对象
     */
    void exportTemplate(HttpServletResponse response);

    /**
     * 查询日报详情。
     *
     * @param id 日报主键
     * @return 日报详情，记录不存在时返回 {@code null}
     */
    DailyReportVo queryById(Long id);

    /**
     * 新增日报。
     *
     * @param bo 日报新增参数
     * @return 是否新增成功
     */
    Boolean insertByBo(DailyReportBo bo);

    /**
     * 修改日报。
     *
     * @param bo 日报修改参数
     * @return 是否修改成功
     */
    Boolean updateByBo(DailyReportBo bo);

    /**
     * 校验日报是否允许编辑。
     *
     * @param id 日报主键
     */
    void checkEditable(Long id);

    /**
     * 删除允许删除的日报记录。
     *
     * @param ids 日报主键集合
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /**
     * 导入日报数据。
     *
     * @param rows 导入行数据
     * @return 导入结果说明
     */
    String importData(List<DailyReportImportVo> rows);
}
