package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomAmenity;
import org.dromara.department.domain.vo.RoomAmenityVo;

import java.util.List;

/** 房间设施目录数据层。 */
@Mapper
public interface RoomAmenityMapper extends BaseMapperPlus<RoomAmenity, RoomAmenityVo> {

    @Select({
        "select a.id, a.dept_id, d.dept_name, a.amenity_name, a.category, a.icon, a.sort_no, a.status, a.remark, a.create_time",
        "from dm_room_amenity a left join sys_dept d on d.dept_id = a.dept_id",
        "where a.del_flag = '0' and (#{canManageAll} or a.dept_id = #{deptId})",
        "order by a.sort_no asc, a.amenity_name asc, a.id asc"
    })
    Page<RoomAmenityVo> selectPageList(Page<RoomAmenityVo> page,
                                       @Param("deptId") Long deptId,
                                       @Param("canManageAll") boolean canManageAll);

    @Select({
        "select id, dept_id, amenity_name, category, icon, sort_no, status, remark, create_time",
        "from dm_room_amenity where del_flag = '0' and status = 'ENABLED'",
        "and (#{canManageAll} or dept_id = #{deptId})",
        "order by sort_no asc, amenity_name asc, id asc"
    })
    List<RoomAmenityVo> selectOptions(@Param("deptId") Long deptId,
                                      @Param("canManageAll") boolean canManageAll);
}
