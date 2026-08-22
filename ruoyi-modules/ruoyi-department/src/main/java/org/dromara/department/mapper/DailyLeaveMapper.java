package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.department.domain.DailyLeave;
import org.dromara.department.domain.vo.DailyLeaveVo;

import java.time.LocalDate;
import java.util.List;

/** 科室人员休假安排数据层。 */
@Mapper
public interface DailyLeaveMapper extends BaseMapper<DailyLeave> {

    @Select({
        "<script>",
        "select l.id, l.dept_id, l.user_id, u.user_name, u.nick_name, l.start_date, l.end_date,",
        "l.leave_type, l.reason, l.status",
        "from dm_daily_leave l",
        "left join sys_user u on u.user_id = l.user_id and u.del_flag = '0'",
        "where l.del_flag = '0' and l.status = 'ENABLED'",
        "and l.dept_id = #{deptId}",
        "and l.start_date &lt;= #{endDate} and l.end_date &gt;= #{beginDate}",
        "<if test='userId != null'> and l.user_id = #{userId} </if>",
        "order by l.start_date, l.user_id",
        "</script>"
    })
    List<DailyLeaveVo> selectCalendarLeaves(@Param("deptId") Long deptId,
                                            @Param("beginDate") LocalDate beginDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("userId") Long userId);
}
