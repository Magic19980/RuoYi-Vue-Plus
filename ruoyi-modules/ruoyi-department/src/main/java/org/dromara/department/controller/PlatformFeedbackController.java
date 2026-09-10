package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
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
import org.dromara.department.service.IPlatformFeedbackService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 平台问题与建议接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/platformFeedback")
public class PlatformFeedbackController extends BaseController {

    private final IPlatformFeedbackService feedbackService;

    /** 分页查询平台问题与建议。 */
    @SaCheckPermission("department:platformFeedback:list")
    @GetMapping("/list")
    public R<PageResult<PlatformFeedbackVo>> list(PlatformFeedbackQueryBo bo, PageQuery pageQuery) {
        return R.ok(feedbackService.queryPageList(bo, pageQuery));
    }

    /** 查询当前科室的平台反馈统计。 */
    @SaCheckPermission("department:platformFeedback:query")
    @GetMapping("/summary")
    public R<PlatformFeedbackSummaryVo> summary() {
        return R.ok(feedbackService.querySummary());
    }

    /** 查询可分配的平台反馈处理人。 */
    @SaCheckPermission("department:platformFeedback:query")
    @GetMapping("/assignee-options")
    public R<List<PlatformFeedbackUserOptionVo>> assigneeOptions(String keyword) {
        return R.ok(feedbackService.queryUserOptions(keyword));
    }

    /** 查询当前科室已配置的反馈处理人。 */
    @SaCheckPermission("department:platformFeedback:query")
    @GetMapping("/config/handlers")
    public R<List<PlatformFeedbackUserOptionVo>> handlers() {
        return R.ok(feedbackService.queryConfiguredHandlers());
    }

    /** 更新当前科室的平台反馈处理人配置。 */
    @SaCheckPermission("department:platformFeedback:manage")
    @PutMapping("/config/handlers")
    public R<Void> updateHandlers(@Validated @RequestBody PlatformFeedbackHandlerConfigBo bo) {
        return toAjax(feedbackService.updateConfiguredHandlers(bo));
    }

    /** 查询平台反馈详情。 */
    @SaCheckPermission("department:platformFeedback:query")
    @GetMapping("/{id}")
    public R<PlatformFeedbackVo> getInfo(@NotNull(message = "反馈主键不能为空") @PathVariable Long id) {
        return R.ok(feedbackService.queryById(id));
    }

    /** 新增平台问题或建议。 */
    @SaCheckPermission("department:platformFeedback:add")
    @PostMapping
    public R<Void> add(@Validated @RequestBody PlatformFeedbackBo bo) {
        return toAjax(feedbackService.insertByBo(bo));
    }

    /** 更新平台反馈的处理状态和处理结果。 */
    @SaCheckPermission("department:platformFeedback:process")
    @PutMapping("/process")
    public R<Void> process(@Validated @RequestBody PlatformFeedbackProcessBo bo) {
        return toAjax(feedbackService.process(bo));
    }

    /** 分页查询平台反馈评论。 */
    @SaCheckPermission("department:platformFeedback:query")
    @GetMapping("/{feedbackId}/comments")
    public R<PageResult<PlatformFeedbackCommentVo>> comments(
        @NotNull(message = "反馈主键不能为空") @PathVariable Long feedbackId, PageQuery pageQuery) {
        return R.ok(feedbackService.queryComments(feedbackId, pageQuery));
    }

    /** 新增平台反馈评论。 */
    @SaCheckPermission("department:platformFeedback:comment")
    @PostMapping("/{feedbackId}/comments")
    public R<Void> addComment(@NotNull(message = "反馈主键不能为空") @PathVariable Long feedbackId,
                              @Validated @RequestBody PlatformFeedbackCommentBo bo) {
        return toAjax(feedbackService.addComment(feedbackId, bo));
    }

    /** 查询平台反馈处理活动轨迹。 */
    @SaCheckPermission("department:platformFeedback:query")
    @GetMapping("/{feedbackId}/activities")
    public R<List<PlatformFeedbackActivityVo>> activities(@NotNull(message = "反馈主键不能为空") @PathVariable Long feedbackId) {
        return R.ok(feedbackService.queryActivities(feedbackId));
    }

    /** 上传平台反馈附件。 */
    @SaCheckPermission("department:platformFeedback:add")
    @PostMapping(value = "/attachment/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<PlatformFeedbackAttachmentVo> uploadAttachment(@RequestPart("file") MultipartFile file) {
        return R.ok(feedbackService.uploadAttachment(file));
    }
}
