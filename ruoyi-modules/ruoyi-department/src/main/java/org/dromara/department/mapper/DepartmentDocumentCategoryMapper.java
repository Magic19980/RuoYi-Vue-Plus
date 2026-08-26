package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentDocumentCategory;
import org.dromara.department.domain.bo.DepartmentDocumentCategoryQueryBo;
import org.dromara.department.domain.vo.DepartmentDocumentCategoryVo;
import org.dromara.department.service.DepartmentScope;

import java.util.List;

/** 科室资料分类数据层。 */
@Mapper
public interface DepartmentDocumentCategoryMapper extends BaseMapperPlus<DepartmentDocumentCategory, DepartmentDocumentCategoryVo> {

    @Select({
        "<script>",
        "select c.id, c.dept_id, c.parent_id, c.category_name, c.sort_num, c.status, c.remark, c.create_time, c.update_time,",
        "(select count(1) from dm_department_document d where d.category_id = c.id and d.del_flag = '0') as document_count",
        "from dm_department_document_category c where c.del_flag = '0'",
        "<if test='bo.categoryName != null and bo.categoryName != \"\"'> and c.category_name like concat('%', #{bo.categoryName}, '%') </if>",
        "<if test='bo.status != null and bo.status != \"\"'> and c.status = #{bo.status} </if>",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and c.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by c.sort_num asc, c.id asc",
        "</script>"
    })
    Page<DepartmentDocumentCategoryVo> selectPageList(Page<DepartmentDocumentCategoryVo> page,
                                                       @Param("bo") DepartmentDocumentCategoryQueryBo bo,
                                                       @Param("scope") DepartmentScope scope);

    @Select({
        "<script>",
        "select c.id, c.dept_id, c.parent_id, c.category_name, c.sort_num, c.status, c.remark",
        "from dm_department_document_category c where c.del_flag = '0' and c.status = 'ENABLED'",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and c.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by c.sort_num asc, c.category_name asc, c.id asc",
        "</script>"
    })
    List<DepartmentDocumentCategoryVo> selectOptions(@Param("scope") DepartmentScope scope);

    @Select("select count(1) from dm_department_document_category where del_flag = '0' and dept_id = #{deptId} and parent_id = #{parentId} and category_name = #{categoryName} and id <> #{id}")
    int countDuplicate(@Param("deptId") Long deptId, @Param("parentId") Long parentId, @Param("categoryName") String categoryName, @Param("id") Long id);

    @Select({
        "select c.id, c.dept_id, c.parent_id, c.category_name, c.sort_num, c.status, c.remark, c.create_time, c.update_time,",
        "(select count(1) from dm_department_document d where d.category_id = c.id and d.del_flag = '0') as document_count",
        "from dm_department_document_category c where c.del_flag = '0'",
        "and (#{scope.all} = true or c.dept_id = #{scope.deptId})",
        "order by c.parent_id asc, c.sort_num asc, c.id asc"
    })
    List<DepartmentDocumentCategoryVo> selectTreeList(@Param("scope") DepartmentScope scope);

    @Select("select count(1) from dm_department_document_category where del_flag = '0' and dept_id = #{deptId} and parent_id = #{parentId}")
    int countChildren(@Param("deptId") Long deptId, @Param("parentId") Long parentId);

    @Select("select count(1) from dm_department_document_category where del_flag = '0' and dept_id = #{deptId} and parent_id = #{parentId} and status = 'ENABLED'")
    int countEnabledChildren(@Param("deptId") Long deptId, @Param("parentId") Long parentId);

    @Select("select count(1) from dm_department_document where category_id = #{categoryId} and del_flag = '0'")
    int countDocuments(@Param("categoryId") Long categoryId);
}
