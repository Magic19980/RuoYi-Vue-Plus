package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DailyReportAttachment;
import org.dromara.department.domain.vo.DailyReportAttachmentVo;

import java.util.List;

/**
 * 日报附件归档关系数据层。
 */
@Mapper
public interface DailyReportAttachmentMapper extends BaseMapperPlus<DailyReportAttachment, DailyReportAttachmentVo> {

    /**
     * 查询日报的有效附件。
     *
     * @param reportId 日报主键
     * @return 附件列表
     */
    @Select("select a.id, a.report_id, a.oss_id, a.original_name, a.file_type, a.sort_num, o.url from dm_daily_report_attachment a left join sys_oss o on o.oss_id = a.oss_id where a.report_id = #{reportId} and a.del_flag = '0' order by a.sort_num asc, a.id asc")
    List<DailyReportAttachmentVo> selectListByReportId(@Param("reportId") Long reportId);

    /**
     * 查询有效日报附件详情。
     *
     * @param id 附件关联记录主键
     * @return 附件详情，不存在时返回 {@code null}
     */
    @Select("select a.id, a.report_id, a.oss_id, a.original_name, a.file_type, a.sort_num, o.url from dm_daily_report_attachment a left join sys_oss o on o.oss_id = a.oss_id where a.id = #{id} and a.del_flag = '0'")
    DailyReportAttachmentVo selectDetailById(@Param("id") Long id);
}
