package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomCalendarException;
import org.dromara.department.domain.bo.RoomCalendarExceptionQueryBo;
import org.dromara.department.domain.vo.RoomCalendarExceptionVo;

import java.time.LocalDate;
import java.util.List;

/** 房间日历例外数据层。 */
@Mapper
public interface RoomCalendarExceptionMapper extends BaseMapperPlus<RoomCalendarException, RoomCalendarExceptionVo> {

    @Select({
        "<script>",
        "select e.id, e.dept_id, d.dept_name, e.exception_date, e.exception_type, e.exception_name, e.remark, e.create_time",
        "from dm_room_calendar_exception e left join sys_dept d on d.dept_id = e.dept_id",
        "where e.del_flag = '0'",
        "<if test='bo.beginDate != null'> and e.exception_date &gt;= #{bo.beginDate} </if>",
        "<if test='bo.endDate != null'> and e.exception_date &lt;= #{bo.endDate} </if>",
        "<choose>",
        "<when test='canManageAll'></when>",
        "<otherwise> and e.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "order by e.exception_date asc, e.id asc",
        "</script>"
    })
    Page<RoomCalendarExceptionVo> selectPageList(Page<RoomCalendarExceptionVo> page,
                                                   @Param("bo") RoomCalendarExceptionQueryBo bo,
                                                   @Param("deptId") Long deptId,
                                                   @Param("canManageAll") boolean canManageAll);

    @Select({
        "select id, dept_id, exception_date, exception_type, exception_name, remark, version, del_flag",
        "from dm_room_calendar_exception",
        "where del_flag = '0' and dept_id = #{deptId} and exception_date = #{exceptionDate}",
        "limit 1"
    })
    RoomCalendarException selectByDeptAndDate(@Param("deptId") Long deptId,
                                               @Param("exceptionDate") LocalDate exceptionDate);

    @Select({
        "select id, dept_id, exception_date, exception_type, exception_name, remark, version, del_flag",
        "from dm_room_calendar_exception",
        "where del_flag = '0' and dept_id = #{deptId} and exception_date between #{beginDate} and #{endDate}",
        "order by exception_date asc"
    })
    List<RoomCalendarException> selectByDeptAndDateRange(@Param("deptId") Long deptId,
                                                         @Param("beginDate") LocalDate beginDate,
                                                         @Param("endDate") LocalDate endDate);
}
