package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.WeeklyReport;
import org.dromara.department.domain.vo.WeeklyReportVo;

/**
 * 周报快照数据层。
 */
@Mapper
public interface WeeklyReportMapper extends BaseMapperPlus<WeeklyReport, WeeklyReportVo> {
}
