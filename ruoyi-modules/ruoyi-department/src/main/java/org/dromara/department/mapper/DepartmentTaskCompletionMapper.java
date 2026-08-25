package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentTaskCompletion;
import org.dromara.department.domain.vo.DepartmentTaskCompletionVo;

import java.util.List;

/** 任务完成记录数据层。 */
@Mapper
public interface DepartmentTaskCompletionMapper extends BaseMapperPlus<DepartmentTaskCompletion, DepartmentTaskCompletionVo> {

    @Select("select source_id from dm_department_task_completion where instance_id = #{instanceId} and del_flag = '0'")
    List<Long> selectSourceIds(@Param("instanceId") Long instanceId);

    @Insert("insert ignore into dm_department_task_completion (id, instance_id, task_type, source_id, completed_at, del_flag, create_time) values (#{id}, #{instanceId}, #{taskType}, #{sourceId}, now(), '0', now())")
    int insertIgnore(@Param("id") Long id, @Param("instanceId") Long instanceId,
                     @Param("taskType") String taskType, @Param("sourceId") Long sourceId);

    default int deleteByInstanceId(Long instanceId) {
        return delete(Wrappers.<DepartmentTaskCompletion>lambdaUpdate()
            .eq(DepartmentTaskCompletion::getInstanceId, instanceId));
    }
}
