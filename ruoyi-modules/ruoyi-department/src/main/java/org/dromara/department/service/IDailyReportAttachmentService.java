package org.dromara.department.service;

import org.dromara.department.domain.vo.DailyReportAttachmentVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 日报附件归档业务接口。
 */
public interface IDailyReportAttachmentService {

    /**
     * 查询日报关联的附件列表。
     *
     * @param reportId 日报主键
     * @return 按显示顺序排列的附件列表
     */
    List<DailyReportAttachmentVo> listByReportId(Long reportId);

    /**
     * 上传并归档日报附件。
     *
     * @param reportId 日报主键
     * @param file     待上传的附件
     * @return 上传成功后的附件信息
     */
    DailyReportAttachmentVo upload(Long reportId, MultipartFile file);

    /**
     * 删除日报附件关联记录。
     *
     * @param id 附件关联记录主键
     * @return 是否删除成功
     */
    Boolean remove(Long id);
}
