package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.department.domain.DailyCalendarConfig;

/** 科室日报周工作日规则数据层。 */
@Mapper
public interface DailyCalendarConfigMapper extends BaseMapper<DailyCalendarConfig> {
}
