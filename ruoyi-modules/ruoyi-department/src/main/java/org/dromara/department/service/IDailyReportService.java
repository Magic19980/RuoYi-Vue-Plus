package org.dromara.department.service;

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

    PageResult<DailyReportVo> queryPageList(DailyReportQueryBo bo, PageQuery pageQuery);

    List<DailyReportVo> queryList(DailyReportQueryBo bo);

    DailyReportVo queryById(Long id);

    Boolean insertByBo(DailyReportBo bo);

    Boolean updateByBo(DailyReportBo bo);

    void checkEditable(Long id);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    String importData(List<DailyReportImportVo> rows);
}
