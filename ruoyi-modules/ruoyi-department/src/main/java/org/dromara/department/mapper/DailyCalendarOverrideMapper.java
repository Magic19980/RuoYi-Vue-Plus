package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.department.domain.DailyCalendarOverride;

/** 科室日报日期例外规则数据层。 */
@Mapper
public interface DailyCalendarOverrideMapper extends BaseMapper<DailyCalendarOverride> {
}
