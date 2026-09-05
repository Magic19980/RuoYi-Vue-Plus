package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.DepartmentCommunityCommentBo;
import org.dromara.department.domain.bo.DepartmentCommunityPostBo;
import org.dromara.department.domain.bo.DepartmentCommunityPostQueryBo;
import org.dromara.department.domain.bo.DepartmentCommunityReportBo;
import org.dromara.department.domain.bo.DepartmentCommunityReportQueryBo;
import org.dromara.department.domain.vo.DepartmentCommunityCommentVo;
import org.dromara.department.domain.vo.DepartmentCommunityMediaVo;
import org.dromara.department.domain.vo.DepartmentCommunityPostVo;
import org.dromara.department.domain.vo.DepartmentCommunityReactionVo;
import org.dromara.department.domain.vo.DepartmentCommunityReportVo;
import org.dromara.department.service.IDepartmentCommunityService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 协作社区接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/community")
public class DepartmentCommunityController extends BaseController {

    private final IDepartmentCommunityService communityService;

    @SaCheckPermission("department:community:list")
    @GetMapping("/list")
    public R<PageResult<DepartmentCommunityPostVo>> list(DepartmentCommunityPostQueryBo bo, PageQuery pageQuery) {
        return R.ok(communityService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:community:query")
    @GetMapping("/{id}")
    public R<DepartmentCommunityPostVo> getInfo(@NotNull(message = "帖子主键不能为空") @PathVariable Long id) {
        return R.ok(communityService.queryById(id));
    }

    @SaCheckPermission("department:community:add")
    @Log(title = "协作社区", businessType = BusinessType.INSERT)
    @PostMapping("/post")
    public R<Void> add(@Validated @RequestBody DepartmentCommunityPostBo bo) {
        return toAjax(communityService.insertByBo(bo));
    }

    @SaCheckPermission("department:community:edit")
    @Log(title = "协作社区", businessType = BusinessType.UPDATE)
    @PutMapping("/post")
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DepartmentCommunityPostBo bo) {
        return toAjax(communityService.updateByBo(bo));
    }

    @SaCheckPermission("department:community:remove")
    @Log(title = "协作社区", businessType = BusinessType.DELETE)
    @DeleteMapping("/post/{id}")
    public R<Void> remove(@NotNull(message = "帖子主键不能为空") @PathVariable Long id) {
        return toAjax(communityService.deleteById(id));
    }

    @SaCheckPermission("department:community:query")
    @GetMapping("/{postId}/comments")
    public R<List<DepartmentCommunityCommentVo>> comments(
        @NotNull(message = "帖子主键不能为空") @PathVariable Long postId) {
        return R.ok(communityService.queryComments(postId));
    }

    @SaCheckPermission("department:community:comment")
    @Log(title = "协作社区评论", businessType = BusinessType.INSERT)
    @PostMapping("/{postId}/comments")
    public R<Void> addComment(
        @NotNull(message = "帖子主键不能为空") @PathVariable Long postId,
        @Validated @RequestBody DepartmentCommunityCommentBo bo) {
        return toAjax(communityService.addComment(postId, bo));
    }

    @SaCheckPermission("department:community:comment")
    @Log(title = "协作社区评论", businessType = BusinessType.DELETE)
    @DeleteMapping("/comment/{id}")
    public R<Void> removeComment(@NotNull(message = "评论主键不能为空") @PathVariable Long id) {
        return toAjax(communityService.deleteComment(id));
    }

    @SaCheckPermission("department:community:interact")
    @PostMapping("/{postId}/reaction/{reactionType}")
    public R<DepartmentCommunityReactionVo> toggleReaction(
        @NotNull(message = "帖子主键不能为空") @PathVariable Long postId,
        @PathVariable String reactionType) {
        return R.ok(communityService.toggleReaction(postId, reactionType));
    }

    @SaCheckPermission("department:community:edit")
    @Log(title = "协作社区", businessType = BusinessType.UPDATE)
    @PostMapping("/{postId}/resolve/{commentId}")
    public R<Void> resolve(
        @NotNull(message = "帖子主键不能为空") @PathVariable Long postId,
        @NotNull(message = "评论主键不能为空") @PathVariable Long commentId) {
        return toAjax(communityService.resolve(postId, commentId));
    }

    @SaCheckPermission("department:community:report")
    @Log(title = "协作社区举报", businessType = BusinessType.INSERT)
    @PostMapping("/{postId}/report")
    public R<Void> report(
        @NotNull(message = "帖子主键不能为空") @PathVariable Long postId,
        @Validated(DepartmentCommunityReportBo.ReportGroup.class) @RequestBody DepartmentCommunityReportBo bo) {
        return toAjax(communityService.report(postId, bo));
    }

    @SaCheckPermission("department:community:moderate")
    @GetMapping("/report/list")
    public R<PageResult<DepartmentCommunityReportVo>> reportList(DepartmentCommunityReportQueryBo bo, PageQuery pageQuery) {
        return R.ok(communityService.queryReportPageList(bo, pageQuery));
    }

    @SaCheckPermission("department:community:moderate")
    @Log(title = "协作社区举报", businessType = BusinessType.UPDATE)
    @PutMapping("/report")
    public R<Void> handleReport(
        @Validated(DepartmentCommunityReportBo.HandleGroup.class) @RequestBody DepartmentCommunityReportBo bo) {
        return toAjax(communityService.handleReport(bo));
    }

    @SaCheckPermission("department:community:add")
    @Log(title = "协作社区媒体", businessType = BusinessType.INSERT)
    @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<DepartmentCommunityMediaVo> uploadMedia(@RequestPart("file") MultipartFile file) {
        return R.ok(communityService.uploadMedia(file));
    }

    @SaCheckPermission("department:community:comment")
    @Log(title = "协作社区评论图片", businessType = BusinessType.INSERT)
    @PostMapping(value = "/comment/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<DepartmentCommunityMediaVo> uploadCommentMedia(@RequestPart("file") MultipartFile file) {
        return R.ok(communityService.uploadCommentMedia(file));
    }
}
