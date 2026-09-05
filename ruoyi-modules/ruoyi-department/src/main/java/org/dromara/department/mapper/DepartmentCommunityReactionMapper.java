package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentCommunityReaction;
import org.dromara.department.domain.vo.DepartmentCommunityReactionVo;

/**
 * 协作社区互动数据层。
 */
@Mapper
public interface DepartmentCommunityReactionMapper extends BaseMapperPlus<DepartmentCommunityReaction, DepartmentCommunityReactionVo> {
}
