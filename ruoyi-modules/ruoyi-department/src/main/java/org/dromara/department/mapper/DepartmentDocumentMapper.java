package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentDocument;
import org.dromara.department.domain.bo.DepartmentDocumentQueryBo;
import org.dromara.department.domain.vo.DepartmentDocumentVo;
import org.dromara.department.service.DepartmentScope;

import java.util.Collection;

/**
 * 科室资料数据层。
 */
@Mapper
public interface DepartmentDocumentMapper extends BaseMapperPlus<DepartmentDocument, DepartmentDocumentVo> {

    @Select({
        "<script>",
        "select d.id, d.dept_id, d.project_id, p.project_name, d.category_id, c.category_name, d.title, d.description, d.tags,",
        "d.visibility, d.status, d.expire_date, d.current_version_id, d.version_no, d.current_oss_id,",
        "d.current_original_name, d.current_file_suffix, d.current_file_size, d.current_content_type,",
        "coalesce(u.nick_name, u.user_name) as create_by_name, d.create_time, d.update_time",
        "from dm_department_document d",
        "left join dm_department_project p on p.id = d.project_id and p.del_flag = '0'",
        "left join dm_department_document_category c on c.id = d.category_id and c.del_flag = '0'",
        "left join sys_user u on u.user_id = d.create_by and u.del_flag = '0'",
        "where d.del_flag = '0'",
        "<if test='bo.title != null and bo.title != \"\"'> and (d.title like concat('%', #{bo.title}, '%') or d.tags like concat('%', #{bo.title}, '%')) </if>",
        "<if test='bo.categoryId != null'> and d.category_id = #{bo.categoryId} </if>",
        "<if test='bo.projectId != null'> and d.project_id = #{bo.projectId} </if>",
        "<if test='bo.fileSuffix != null and bo.fileSuffix != \"\"'> and d.current_file_suffix = #{bo.fileSuffix} </if>",
        "<if test='bo.status != null and bo.status != \"\"'> and d.status = #{bo.status} </if>",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and d.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by d.update_time desc, d.id desc",
        "</script>"
    })
    Page<DepartmentDocumentVo> selectPageList(Page<DepartmentDocumentVo> page,
                                               @Param("bo") DepartmentDocumentQueryBo bo,
                                               @Param("scope") DepartmentScope scope);

    @Select({
        "<script>",
        "select d.id, d.dept_id, d.project_id, p.project_name, d.category_id, c.category_name, d.title, d.description, d.tags,",
        "d.visibility, d.status, d.expire_date, d.current_version_id, d.version_no, d.current_oss_id,",
        "d.current_original_name, d.current_file_suffix, d.current_file_size, d.current_content_type,",
        "coalesce(u.nick_name, u.user_name) as create_by_name, d.create_time, d.update_time",
        "from dm_department_document d",
        "left join dm_department_project p on p.id = d.project_id and p.del_flag = '0'",
        "left join dm_department_document_category c on c.id = d.category_id and c.del_flag = '0'",
        "left join sys_user u on u.user_id = d.create_by and u.del_flag = '0'",
        "where d.del_flag = '1'",
        "<if test='bo.title != null and bo.title != \"\"'> and (d.title like concat('%', #{bo.title}, '%') or d.tags like concat('%', #{bo.title}, '%')) </if>",
        "<if test='bo.categoryId != null'> and d.category_id = #{bo.categoryId} </if>",
        "<if test='bo.projectId != null'> and d.project_id = #{bo.projectId} </if>",
        "<if test='bo.fileSuffix != null and bo.fileSuffix != \"\"'> and d.current_file_suffix = #{bo.fileSuffix} </if>",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and d.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by d.update_time desc, d.id desc",
        "</script>"
    })
    Page<DepartmentDocumentVo> selectRecyclePageList(Page<DepartmentDocumentVo> page,
                                                      @Param("bo") DepartmentDocumentQueryBo bo,
                                                      @Param("scope") DepartmentScope scope);

    @Select({
        "select d.id, d.dept_id, d.project_id, p.project_name, d.category_id, c.category_name, d.title, d.description, d.tags,",
        "d.visibility, d.status, d.expire_date, d.current_version_id, d.version_no, d.current_oss_id,",
        "d.current_original_name, d.current_file_suffix, d.current_file_size, d.current_content_type,",
        "coalesce(u.nick_name, u.user_name) as create_by_name, d.create_time, d.update_time",
        "from dm_department_document d",
        "left join dm_department_project p on p.id = d.project_id and p.del_flag = '0'",
        "left join dm_department_document_category c on c.id = d.category_id and c.del_flag = '0'",
        "left join sys_user u on u.user_id = d.create_by and u.del_flag = '0'",
        "where d.id = #{id} and d.del_flag = '0'"
    })
    DepartmentDocumentVo selectDetailById(@Param("id") Long id);

    @Select("select * from dm_department_document where id = #{id}")
    DepartmentDocument selectAnyById(@Param("id") Long id);

    @Update({
        "<script>",
        "update dm_department_document set del_flag = '0', update_by = #{userId}, update_time = now()",
        "where del_flag = '1' and id in",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
        "</script>"
    })
    int restoreByIds(@Param("ids") Collection<Long> ids, @Param("userId") Long userId);
}
