package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.ScoreCategory;
import org.dromara.department.domain.vo.ScoreCategoryVo;

import java.util.List;

/** SCORE 提案分类数据层。 */
@Mapper
public interface ScoreCategoryMapper extends BaseMapperPlus<ScoreCategory, ScoreCategoryVo> {

    @Select("select c.id, c.parent_id, c.category_name, c.category_level, c.sort_num, c.status, c.remark, c.create_time, c.update_time, "
        + "(select count(1) from dm_score_proposal p where p.del_flag = '0' and "
        + "((c.category_level = 1 and (p.main_category_id = c.id or p.main_category = c.category_name)) "
        + "or (c.category_level = 2 and (p.sub_category_id = c.id or p.sub_category = c.category_name)))) as proposal_count "
        + "from dm_score_category c where c.del_flag = '0' order by c.parent_id asc, c.sort_num asc, c.id asc")
    List<ScoreCategoryVo> selectAllList();

    @Select("select c.id, c.parent_id, c.category_name, c.category_level, c.sort_num, c.status, c.remark, c.create_time, c.update_time, 0 as proposal_count "
        + "from dm_score_category c where c.del_flag = '0' and c.status = 'ENABLED' "
        + "order by c.parent_id asc, c.sort_num asc, c.id asc")
    List<ScoreCategoryVo> selectEnabledList();

    @Select("select count(1) from dm_score_category c where c.del_flag = '0' and c.parent_id = #{parentId} and c.category_name = #{categoryName} "
        + "and c.id <> #{id}")
    int countDuplicate(@Param("parentId") Long parentId, @Param("categoryName") String categoryName, @Param("id") Long id);

    @Select("select count(1) from dm_score_category c where c.del_flag = '0' and c.parent_id = #{parentId}")
    int countChildren(@Param("parentId") Long parentId);

    @Select("select count(1) from dm_score_category c where c.del_flag = '0' and c.parent_id = #{parentId} and c.status = 'ENABLED'")
    int countEnabledChildren(@Param("parentId") Long parentId);

    @Select("select count(1) from dm_score_proposal p where p.del_flag = '0' and "
        + "((#{categoryLevel} = 1 and (p.main_category_id = #{categoryId} or p.main_category = #{categoryName})) "
        + "or (#{categoryLevel} = 2 and (p.sub_category_id = #{categoryId} or p.sub_category = #{categoryName})))")
    int countProposalReferences(@Param("categoryId") Long categoryId,
                                @Param("categoryName") String categoryName,
                                @Param("categoryLevel") Integer categoryLevel);
}
