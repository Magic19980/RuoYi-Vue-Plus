package org.dromara.department.service;

import org.dromara.department.domain.vo.DailyReportAttachmentVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 日报附件归档业务接口。
 */
public interface IDailyReportAttachmentService {

    List<DailyReportAttachmentVo> listByReportId(Long reportId);

    DailyReportAttachmentVo upload(Long reportId, MultipartFile file);

    Boolean remove(Long id);
}
