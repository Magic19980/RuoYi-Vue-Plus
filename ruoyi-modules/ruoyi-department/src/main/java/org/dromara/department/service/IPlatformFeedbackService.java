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

    /** 分页查询平台问题与建议。 */
    PageResult<PlatformFeedbackVo> queryPageList(PlatformFeedbackQueryBo bo, PageQuery pageQuery);

    /** 查询问题与建议详情。 */
    PlatformFeedbackVo queryById(Long id);

    /** 新增平台问题或建议。 */
    Boolean insertByBo(PlatformFeedbackBo bo);

    /** 更新问题处理状态和解决方案。 */
    Boolean process(PlatformFeedbackProcessBo bo);

    /** 分页查询问题处理评论。 */
    PageResult<PlatformFeedbackCommentVo> queryComments(Long feedbackId, PageQuery pageQuery);

    /** 新增问题处理评论。 */
    Boolean addComment(Long feedbackId, PlatformFeedbackCommentBo bo);

    /** 查询问题处理活动轨迹。 */
    List<PlatformFeedbackActivityVo> queryActivities(Long feedbackId);

    /** 查询当前权限范围内的问题统计。 */
    PlatformFeedbackSummaryVo querySummary();

    /** 查询已配置的问题处理人。 */
    List<PlatformFeedbackUserOptionVo> queryConfiguredHandlers();

    /** 更新问题处理人配置。 */
    Boolean updateConfiguredHandlers(PlatformFeedbackHandlerConfigBo bo);

    /** 查询可选的问题处理人。 */
    List<PlatformFeedbackUserOptionVo> queryUserOptions(String keyword);

    /** 上传问题附件。 */
    PlatformFeedbackAttachmentVo uploadAttachment(MultipartFile file);
}
