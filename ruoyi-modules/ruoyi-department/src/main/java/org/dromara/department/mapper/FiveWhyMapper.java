package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.FiveWhy;
import org.dromara.department.domain.bo.FiveWhyQueryBo;
import org.dromara.department.domain.vo.FiveWhyVo;

/** 5WHY分析数据层。 */
@Mapper
public interface FiveWhyMapper extends BaseMapperPlus<FiveWhy, FiveWhyVo> {

    @Select({
        "<script>",
        "select f.id, f.dept_id, f.company_dept, f.employee_no, f.analyst_name, f.analysis_date,",
        "f.problem_name, f.problem_description, f.impact_scope, f.whys_json, f.improvements_json,",
        "f.before_oss_id, f.after_oss_id, f.effect_verification, f.standardization_plan,",
        "f.standardization_execution, f.review_status, f.review_comment, f.create_time, f.update_time",
        "from dm_five_why f where f.del_flag = '0'",
        "<if test='bo.analystName != null and bo.analystName != \"\"'> and f.analyst_name like concat('%', #{bo.analystName}, '%') </if>",
        "<if test='bo.problemName != null and bo.problemName != \"\"'> and f.problem_name like concat('%', #{bo.problemName}, '%') </if>",
        "<if test='bo.reviewStatus != null and bo.reviewStatus != \"\"'> and f.review_status = #{bo.reviewStatus} </if>",
        "<if test='bo.beginDate != null'> and f.analysis_date &gt;= #{bo.beginDate} </if>",
        "<if test='bo.endDate != null'> and f.analysis_date &lt;= #{bo.endDate} </if>",
        "<choose>",
        "<when test='all == true'></when>",
        "<otherwise> and f.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "order by f.analysis_date desc, f.id desc",
        "</script>"
    })
    Page<FiveWhy> selectPageList(Page<FiveWhy> page,
                                    @Param("bo") FiveWhyQueryBo bo,
                                    @Param("deptId") Long deptId,
                                    @Param("all") boolean all);
}
