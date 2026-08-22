package org.dromara.department.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.department.domain.DailyReportAttachment;
import org.dromara.department.domain.vo.DailyReportAttachmentVo;
import org.dromara.department.mapper.DailyReportAttachmentMapper;
import org.dromara.department.service.IDailyReportAttachmentService;
import org.dromara.department.service.IDailyReportService;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 日报附件归档业务实现。
 */
@RequiredArgsConstructor
@Service
public class DailyReportAttachmentServiceImpl implements IDailyReportAttachmentService {

    private final DailyReportAttachmentMapper attachmentMapper;
    private final IDailyReportService dailyReportService;
    private final ISysOssService ossService;

    @Override
    public List<DailyReportAttachmentVo> listByReportId(Long reportId) {
        if (dailyReportService.queryById(reportId) == null) {
            throw new ServiceException("日报不存在或没有访问权限");
        }
        return attachmentMapper.selectListByReportId(reportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyReportAttachmentVo upload(Long reportId, MultipartFile file) {
        dailyReportService.checkEditable(reportId);
        if (file == null || file.isEmpty()) {
            throw new ServiceException("附件不能为空");
        }
        SysOssExt ossExt = new SysOssExt();
        ossExt.setBizType("DEPARTMENT_DAILY_REPORT");
        ossExt.setRefId(String.valueOf(reportId));
        ossExt.setRefType("DAILY_REPORT");
        SysOssVo oss = ossService.upload(file, ossExt);

        DailyReportAttachment entity = new DailyReportAttachment();
        entity.setReportId(reportId);
        entity.setOssId(oss.getOssId());
        entity.setOriginalName(oss.getOriginalName());
        entity.setFileType(oss.getFileSuffix());
        entity.setSortNum(0);
        attachmentMapper.insert(entity);
        return attachmentMapper.selectDetailById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean remove(Long id) {
        DailyReportAttachmentVo attachment = attachmentMapper.selectDetailById(id);
        if (attachment == null) {
            throw new ServiceException("附件归档记录不存在");
        }
        dailyReportService.checkEditable(attachment.getReportId());
        // 只解除业务归档关系，OSS 文件保留，避免误删被其他业务引用的对象。
        return attachmentMapper.deleteById(id) > 0;
    }
}
