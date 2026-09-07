package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.PlatformFeedbackBo;
import org.dromara.department.domain.bo.PlatformFeedbackCommentBo;
import org.dromara.department.domain.bo.PlatformFeedbackHandlerConfigBo;
import org.dromara.department.domain.bo.PlatformFeedbackProcessBo;
import org.dromara.department.domain.bo.PlatformFeedbackQueryBo;
import org.dromara.department.domain.vo.PlatformFeedbackActivityVo;
import org.dromara.department.domain.vo.PlatformFeedbackAttachmentVo;
import org.dromara.department.domain.vo.PlatformFeedbackCommentVo;
import org.dromara.department.domain.vo.PlatformFeedbackSummaryVo;
import org.dromara.department.domain.vo.PlatformFeedbackUserOptionVo;
import org.dromara.department.domain.vo.PlatformFeedbackVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 平台问题与建议业务接口。
 */
public interface IPlatformFeedbackService {

    PageResult<PlatformFeedbackVo> queryPageList(PlatformFeedbackQueryBo bo, PageQuery pageQuery);

    PlatformFeedbackVo queryById(Long id);

    Boolean insertByBo(PlatformFeedbackBo bo);

    Boolean process(PlatformFeedbackProcessBo bo);

    PageResult<PlatformFeedbackCommentVo> queryComments(Long feedbackId, PageQuery pageQuery);

    Boolean addComment(Long feedbackId, PlatformFeedbackCommentBo bo);

    List<PlatformFeedbackActivityVo> queryActivities(Long feedbackId);

    PlatformFeedbackSummaryVo querySummary();

    List<PlatformFeedbackUserOptionVo> queryConfiguredHandlers();

    Boolean updateConfiguredHandlers(PlatformFeedbackHandlerConfigBo bo);

    List<PlatformFeedbackUserOptionVo> queryUserOptions(String keyword);

    PlatformFeedbackAttachmentVo uploadAttachment(MultipartFile file);
}
