package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentDocumentVersion;
import org.dromara.department.domain.vo.DepartmentDocumentVersionVo;

import java.util.List;

/**
 * 科室资料版本数据层。
 */
@Mapper
public interface DepartmentDocumentVersionMapper extends BaseMapperPlus<DepartmentDocumentVersion, DepartmentDocumentVersionVo> {

    @Select({
        "select v.id, v.document_id, v.version_no, v.oss_id, v.original_name, v.file_suffix, v.file_size,",
        "v.content_type, v.version_note, coalesce(u.nick_name, u.user_name) as create_by_name, v.create_time",
        "from dm_department_document_version v",
        "left join sys_user u on u.user_id = v.create_by and u.del_flag = '0'",
        "where v.document_id = #{documentId} and v.del_flag = '0'",
        "order by v.version_no desc, v.id desc"
    })
    List<DepartmentDocumentVersionVo> selectListByDocumentId(@Param("documentId") Long documentId);
}
