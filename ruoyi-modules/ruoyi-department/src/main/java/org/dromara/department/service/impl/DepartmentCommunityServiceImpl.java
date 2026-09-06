package org.dromara.department.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.DepartmentCommunityComment;
import org.dromara.department.domain.DepartmentCommunityCommentMedia;
import org.dromara.department.domain.DepartmentCommunityMedia;
import org.dromara.department.domain.DepartmentCommunityPost;
import org.dromara.department.domain.DepartmentCommunityReaction;
import org.dromara.department.domain.DepartmentCommunityReport;
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
import org.dromara.department.mapper.DepartmentCommunityCommentMapper;
import org.dromara.department.mapper.DepartmentCommunityCommentMediaMapper;
import org.dromara.department.mapper.DepartmentCommunityMediaMapper;
import org.dromara.department.mapper.DepartmentCommunityPostMapper;
import org.dromara.department.mapper.DepartmentCommunityReactionMapper;
import org.dromara.department.mapper.DepartmentCommunityReportMapper;
import org.dromara.department.service.IDepartmentCommunityService;
import org.dromara.system.api.domain.PushPayloadDTO;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysMessageService;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

/**
 * 协作社区业务实现。
 */
@RequiredArgsConstructor
@Service
public class DepartmentCommunityServiceImpl implements IDepartmentCommunityService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_RESOLVED = "RESOLVED";
    private static final String VISIBILITY_ALL = "ALL";
    private static final String VISIBILITY_DEPT = "DEPT";
    private static final String TYPE_DISCUSSION = "DISCUSSION";
    private static final String TYPE_QUESTION = "QUESTION";
    private static final String TYPE_EXPERIENCE = "EXPERIENCE";
    private static final String TYPE_IMPROVEMENT = "IMPROVEMENT";
    private static final String REACTION_LIKE = "LIKE";
    private static final String REACTION_FAVORITE = "FAVORITE";
    private static final String PERMISSION_MODERATE = "department:community:moderate";
    private static final String REPORT_PENDING = "PENDING";
    private static final String REPORT_REJECTED = "REJECTED";
    private static final String REPORT_TAKEN_DOWN = "TAKEN_DOWN";
    private static final String MEDIA_IMAGE = "IMAGE";
    private static final String MEDIA_VIDEO = "VIDEO";
    private static final int MAX_MEDIA_COUNT = 9;
    private static final int MAX_COMMENT_MEDIA_COUNT = 3;
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 500L * 1024 * 1024;
    private static final Set<String> IMAGE_SUFFIXES = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> VIDEO_SUFFIXES = Set.of(".mp4", ".webm", ".ogg");

    private final DepartmentCommunityPostMapper postMapper;
    private final DepartmentCommunityCommentMapper commentMapper;
    private final DepartmentCommunityCommentMediaMapper commentMediaMapper;
    private final DepartmentCommunityMediaMapper mediaMapper;
    private final DepartmentCommunityReactionMapper reactionMapper;
    private final DepartmentCommunityReportMapper reportMapper;
    private final ISysMessageService messageService;
    private final ISysOssService ossService;

    @Override
    public PageResult<DepartmentCommunityPostVo> queryPageList(DepartmentCommunityPostQueryBo bo, PageQuery pageQuery) {
        DepartmentCommunityPostQueryBo query = bo == null ? new DepartmentCommunityPostQueryBo() : bo;
        var page = postMapper.selectPageList(pageQuery.build(), query, LoginHelper.getUserId(), LoginHelper.getDeptId());
        fillMedia(page.getRecords());
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public DepartmentCommunityPostVo queryById(Long id) {
        DepartmentCommunityPostVo result = postMapper.selectDetailById(id, LoginHelper.getUserId(), LoginHelper.getDeptId());
        if (result == null) {
            throw new ServiceException("帖子不存在或您没有访问权限");
        }
        postMapper.incrementViewCount(id);
        result.setViewCount((result.getViewCount() == null ? 0 : result.getViewCount()) + 1);
        result.setMediaList(buildMediaList(id));
        result.setMediaCount(result.getMediaList().size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(DepartmentCommunityPostBo bo) {
        validatePost(bo);
        DepartmentCommunityPost entity = new DepartmentCommunityPost();
        entity.setDeptId(LoginHelper.getDeptId());
        copyPost(bo, entity, false);
        boolean inserted = postMapper.insert(entity) > 0;
        if (inserted) {
            replaceMedia(entity.getId(), bo.getMediaOssIds());
        }
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(DepartmentCommunityPostBo bo) {
        if (bo == null || bo.getId() == null) {
            throw new ServiceException("帖子主键不能为空");
        }
        validatePost(bo);
        DepartmentCommunityPost entity = getAccessiblePost(bo.getId());
        assertCanManage(entity, "修改帖子");
        copyPost(bo, entity, true);
        boolean updated = postMapper.updateById(entity) > 0;
        if (updated && bo.getMediaOssIds() != null) {
            replaceMedia(entity.getId(), bo.getMediaOssIds());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        DepartmentCommunityPost entity = getAccessiblePost(id);
        assertCanManage(entity, "删除帖子");
        boolean deleted = postMapper.deleteById(id) > 0;
        if (deleted) {
            mediaMapper.deleteActiveByPostId(id);
            commentMediaMapper.deleteActiveByPostId(id);
        }
        return deleted;
    }

    @Override
    public List<DepartmentCommunityCommentVo> queryComments(Long postId) {
        getAccessiblePost(postId);
        List<DepartmentCommunityCommentVo> result = commentMapper.selectListByPostId(postId, LoginHelper.getUserId());
        fillCommentMedia(result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addComment(Long postId, DepartmentCommunityCommentBo bo) {
        DepartmentCommunityPost post = getAccessiblePost(postId);
        if (bo == null) {
            throw new ServiceException("评论内容不能为空");
        }
        String content = StringUtils.trim(bo.getContent());
        List<Long> mediaOssIds = parseMediaIds(bo.getMediaOssIds());
        if (StringUtils.isBlank(content) && mediaOssIds.isEmpty()) {
            throw new ServiceException("评论内容或图片至少填写一项");
        }
        if (content != null && content.length() > 2000) {
            throw new ServiceException("评论不能超过2000个字符");
        }
        if (mediaOssIds.size() > MAX_COMMENT_MEDIA_COUNT) {
            throw new ServiceException("一条评论最多上传3张图片");
        }
        Long parentId = bo.getParentId() == null ? 0L : bo.getParentId();
        if (parentId > 0) {
            DepartmentCommunityComment parent = commentMapper.selectById(parentId);
            if (parent == null || !Objects.equals(parent.getPostId(), postId)) {
                throw new ServiceException("回复对象不存在");
            }
        }
        DepartmentCommunityComment entity = new DepartmentCommunityComment();
        entity.setPostId(post.getId());
        entity.setParentId(parentId);
        entity.setContent(content == null ? "" : content);
        entity.setStatus("ENABLED");
        entity.setCreateDept(LoginHelper.getDeptId());
        entity.setCreateBy(LoginHelper.getUserId());
        boolean inserted = commentMapper.insert(entity) > 0;
        if (inserted) {
            replaceCommentMedia(entity.getId(), mediaOssIds);
            postMapper.refreshCounts(postId);
            notifyUser(post.getCreateBy(), "有人回复了你的社区内容：“" + post.getTitle() + "”", postId);
            if (parentId > 0) {
                DepartmentCommunityComment parent = commentMapper.selectById(parentId);
                if (parent != null) {
                    notifyUser(parent.getCreateBy(), "有人回复了你的社区评论", postId);
                }
            }
        }
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteComment(Long id) {
        DepartmentCommunityComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new ServiceException("评论不存在");
        }
        if (!Objects.equals(comment.getCreateBy(), LoginHelper.getUserId()) && !isModerator()) {
            throw new ServiceException("您没有删除该评论的权限");
        }
        boolean deleted = commentMapper.deleteById(id) > 0;
        if (deleted) {
            commentMediaMapper.deleteActiveByCommentId(id);
            postMapper.refreshCounts(comment.getPostId());
        }
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentCommunityReactionVo toggleReaction(Long postId, String reactionType) {
        getAccessiblePost(postId);
        String normalizedType = normalizeReactionType(reactionType);
        Long userId = LoginHelper.getUserId();
        DepartmentCommunityReaction reaction = reactionMapper.selectOne(Wrappers.<DepartmentCommunityReaction>lambdaQuery()
            .eq(DepartmentCommunityReaction::getPostId, postId)
            .eq(DepartmentCommunityReaction::getUserId, userId)
            .eq(DepartmentCommunityReaction::getReactionType, normalizedType));
        boolean added = reaction == null;
        if (reaction == null) {
            reaction = new DepartmentCommunityReaction();
            reaction.setPostId(postId);
            reaction.setUserId(userId);
            reaction.setReactionType(normalizedType);
            reaction.setCreateDept(LoginHelper.getDeptId());
            reaction.setCreateBy(userId);
            reactionMapper.insert(reaction);
        } else {
            reactionMapper.deleteById(reaction.getId());
        }
        postMapper.refreshCounts(postId);
        DepartmentCommunityPost entity = postMapper.selectById(postId);
        if (added) {
            notifyUser(entity.getCreateBy(), REACTION_LIKE.equals(normalizedType) ? "有人赞了你的社区内容" : "有人收藏了你的社区内容", postId);
        }
        return reactionResult(entity, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resolve(Long postId, Long commentId) {
        DepartmentCommunityPost post = getAccessiblePost(postId);
        assertCanManage(post, "采纳回复");
        DepartmentCommunityComment comment = commentMapper.selectById(commentId);
        if (comment == null || !Objects.equals(comment.getPostId(), postId)) {
            throw new ServiceException("回复不存在或不属于当前帖子");
        }
        post.setStatus(STATUS_RESOLVED);
        post.setAcceptedCommentId(commentId);
        boolean updated = postMapper.updateById(post) > 0;
        if (updated) {
            notifyUser(post.getCreateBy(), "你的社区问题已被标记为已解决：“" + post.getTitle() + "”", postId);
        }
        return updated;
    }

    @Override
    public PageResult<DepartmentCommunityReportVo> queryReportPageList(DepartmentCommunityReportQueryBo bo, PageQuery pageQuery) {
        assertModerator();
        DepartmentCommunityReportQueryBo query = bo == null ? new DepartmentCommunityReportQueryBo() : bo;
        var page = reportMapper.selectPageList(pageQuery.build(), query);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean report(Long postId, DepartmentCommunityReportBo bo) {
        DepartmentCommunityPost post = getAccessiblePost(postId);
        if (Objects.equals(post.getCreateBy(), LoginHelper.getUserId())) {
            throw new ServiceException("不能举报自己的内容");
        }
        if (bo == null || StringUtils.isBlank(bo.getReason())) {
            throw new ServiceException("举报原因不能为空");
        }
        if (bo.getReason().length() > 500) {
            throw new ServiceException("举报原因不能超过500个字符");
        }
        Long userId = LoginHelper.getUserId();
        Long pendingCount = reportMapper.selectCount(Wrappers.<DepartmentCommunityReport>lambdaQuery()
            .eq(DepartmentCommunityReport::getPostId, postId)
            .eq(DepartmentCommunityReport::getReporterUserId, userId)
            .eq(DepartmentCommunityReport::getStatus, REPORT_PENDING));
        if (pendingCount > 0) {
            throw new ServiceException("你已经举报过该内容，请等待处理");
        }
        DepartmentCommunityReport entity = new DepartmentCommunityReport();
        entity.setPostId(postId);
        entity.setReporterUserId(userId);
        entity.setReason(StringUtils.trim(bo.getReason()));
        entity.setStatus(REPORT_PENDING);
        entity.setCreateDept(LoginHelper.getDeptId());
        entity.setCreateBy(userId);
        return reportMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleReport(DepartmentCommunityReportBo bo) {
        assertModerator();
        if (bo == null || bo.getId() == null) {
            throw new ServiceException("举报主键不能为空");
        }
        if (!REPORT_REJECTED.equals(bo.getStatus()) && !REPORT_TAKEN_DOWN.equals(bo.getStatus())) {
            throw new ServiceException("不支持的举报处理状态");
        }
        DepartmentCommunityReport entity = reportMapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("举报记录不存在");
        }
        entity.setStatus(bo.getStatus());
        entity.setHandleNote(StringUtils.trim(bo.getHandleNote()));
        entity.setHandledBy(LoginHelper.getUserId());
        entity.setHandledAt(LocalDateTime.now());
        boolean updated = reportMapper.updateById(entity) > 0;
        if (updated && REPORT_TAKEN_DOWN.equals(bo.getStatus())) {
            DepartmentCommunityPost post = postMapper.selectById(entity.getPostId());
            if (post != null) {
                post.setStatus("ARCHIVED");
                postMapper.updateById(post);
            }
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentCommunityMediaVo uploadMedia(MultipartFile file) {
        return uploadCommunityMedia(file, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentCommunityMediaVo uploadCommentMedia(MultipartFile file) {
        return uploadCommunityMedia(file, false);
    }

    private DepartmentCommunityMediaVo uploadCommunityMedia(MultipartFile file, boolean allowVideo) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("媒体文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        String suffix = fileSuffix(fileName);
        String mediaType = mediaType(suffix);
        if (mediaType == null || (!allowVideo && !MEDIA_IMAGE.equals(mediaType))) {
            throw new ServiceException("仅支持 JPG、PNG、GIF、WEBP 图片和 MP4、WebM、Ogg 视频");
        }
        long maxSize = MEDIA_VIDEO.equals(mediaType) ? MAX_VIDEO_SIZE : MAX_IMAGE_SIZE;
        if (file.getSize() > maxSize) {
            throw new ServiceException(MEDIA_VIDEO.equals(mediaType) ? "视频不能超过500MB" : "图片不能超过10MB");
        }
        SysOssExt ossExt = new SysOssExt();
        ossExt.setBizType("DEPARTMENT_COMMUNITY");
        ossExt.setSource("community");
        ossExt.setRefType(allowVideo ? "COMMUNITY_MEDIA" : "COMMUNITY_COMMENT_MEDIA");
        // 媒体会在帖子保存时建立业务关系，不能标记为临时文件，避免被通用清理任务误删。
        ossExt.setIsTemp(false);
        SysOssVo oss = ossService.upload(file, ossExt);
        DepartmentCommunityMediaVo result = new DepartmentCommunityMediaVo();
        result.setOssId(oss.getOssId());
        result.setMediaType(mediaType);
        result.setFileName(oss.getOriginalName());
        result.setFileSuffix(suffix);
        result.setContentType(contentType(file.getContentType(), mediaType, suffix));
        result.setFileSize(file.getSize());
        result.setPreviewUrl(ossService.previewUrl(oss.getOssId()));
        return result;
    }

    private void validatePost(DepartmentCommunityPostBo bo) {
        if (bo == null) {
            throw new ServiceException("帖子内容不能为空");
        }
        if (StringUtils.isBlank(bo.getTitle())) {
            throw new ServiceException("帖子标题不能为空");
        }
        if (StringUtils.isBlank(bo.getContent())) {
            throw new ServiceException("帖子内容不能为空");
        }
    }

    private void copyPost(DepartmentCommunityPostBo bo, DepartmentCommunityPost entity, boolean editing) {
        entity.setTitle(StringUtils.trim(bo.getTitle()));
        entity.setSubtitle(StringUtils.trim(bo.getSubtitle()));
        entity.setContent(StringUtils.trim(bo.getContent()));
        entity.setPostType(normalizePostType(bo.getPostType()));
        entity.setTags(StringUtils.trim(bo.getTags()));
        entity.setVisibility(normalizeVisibility(bo.getVisibility()));
        if (!editing || !STATUS_RESOLVED.equals(entity.getStatus())) {
            entity.setStatus(normalizeStatus(bo.getStatus()));
        }
    }

    private void fillMedia(List<DepartmentCommunityPostVo> posts) {
        posts.forEach(post -> {
            List<DepartmentCommunityMediaVo> mediaList = buildMediaList(post.getId());
            post.setMediaList(mediaList);
            post.setMediaCount(mediaList.size());
        });
    }

    /**
     * 批量补充评论图片，避免每条评论单独查询附件造成 N+1 查询。
     */
    private void fillCommentMedia(List<DepartmentCommunityCommentVo> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        List<Long> commentIds = comments.stream().map(DepartmentCommunityCommentVo::getId).toList();
        Map<Long, List<DepartmentCommunityMediaVo>> mediaByCommentId = commentMediaMapper.selectListByCommentIds(commentIds).stream()
            .peek(media -> fillPreviewUrl(media))
            .collect(Collectors.groupingBy(DepartmentCommunityMediaVo::getCommentId));
        comments.forEach(comment -> comment.setMediaList(mediaByCommentId.getOrDefault(comment.getId(), List.of())));
    }

    private void fillPreviewUrl(DepartmentCommunityMediaVo media) {
        if (media.getOssId() == null) {
            return;
        }
        try {
            media.setPreviewUrl(ossService.previewUrl(media.getOssId()));
        } catch (Exception ignored) {
            media.setPreviewUrl(null);
        }
    }

    private List<DepartmentCommunityMediaVo> buildMediaList(Long postId) {
        return mediaMapper.selectListByPostId(postId).stream().peek(media -> {
            if (media.getOssId() == null) {
                return;
            }
            try {
                media.setPreviewUrl(ossService.previewUrl(media.getOssId()));
            } catch (Exception ignored) {
                media.setPreviewUrl(null);
            }
        }).toList();
    }

    private void replaceMedia(Long postId, String mediaOssIds) {
        List<Long> ossIds = parseMediaIds(mediaOssIds);
        if (ossIds.size() > MAX_MEDIA_COUNT) {
            throw new ServiceException("一条内容最多上传9个媒体文件");
        }
        List<DepartmentCommunityMedia> currentMedia = mediaMapper.selectList(Wrappers.<DepartmentCommunityMedia>lambdaQuery()
            .eq(DepartmentCommunityMedia::getPostId, postId)
            .orderByAsc(DepartmentCommunityMedia::getSortNum)
            .orderByAsc(DepartmentCommunityMedia::getId));
        List<Long> currentOssIdList = currentMedia.stream()
            .map(DepartmentCommunityMedia::getOssId)
            .toList();
        if (currentOssIdList.equals(ossIds)) {
            return;
        }
        Set<Long> currentOssIds = currentMedia.stream().map(DepartmentCommunityMedia::getOssId).collect(Collectors.toSet());
        Long userId = LoginHelper.getUserId();
        List<DepartmentCommunityMedia> entities = new ArrayList<>();
        for (int index = 0; index < ossIds.size(); index++) {
            Long ossId = ossIds.get(index);
            SysOssVo oss = ossService.getById(ossId);
            if (oss == null || (oss.getCreateBy() != null && !Objects.equals(oss.getCreateBy(), userId) && !currentOssIds.contains(ossId))) {
                throw new ServiceException("媒体文件不存在或无权使用");
            }
            String suffix = fileSuffix(oss.getOriginalName());
            String mediaType = mediaType(suffix);
            if (mediaType == null) {
                throw new ServiceException("存在不支持的媒体文件类型");
            }
            DepartmentCommunityMedia entity = new DepartmentCommunityMedia();
            entity.setPostId(postId);
            entity.setOssId(ossId);
            entity.setMediaType(mediaType);
            entity.setOriginalName(oss.getOriginalName());
            entity.setFileSuffix(suffix);
            entity.setContentType(contentType(null, mediaType, suffix));
            entity.setFileSize(null);
            entity.setSortNum(index);
            entity.setCreateDept(LoginHelper.getDeptId());
            entity.setCreateBy(userId);
            entities.add(entity);
        }
        mediaMapper.deleteActiveByPostId(postId);
        entities.forEach(mediaMapper::insert);
    }

    /**
     * 保存评论图片关联，并校验图片只能由当前用户使用。
     */
    private void replaceCommentMedia(Long commentId, List<Long> ossIds) {
        if (ossIds.size() > MAX_COMMENT_MEDIA_COUNT) {
            throw new ServiceException("一条评论最多上传3张图片");
        }
        Long userId = LoginHelper.getUserId();
        List<DepartmentCommunityCommentMedia> entities = new ArrayList<>();
        for (int index = 0; index < ossIds.size(); index++) {
            Long ossId = ossIds.get(index);
            SysOssVo oss = ossService.getById(ossId);
            if (oss == null || (oss.getCreateBy() != null && !Objects.equals(oss.getCreateBy(), userId))) {
                throw new ServiceException("图片文件不存在或无权使用");
            }
            String suffix = fileSuffix(oss.getOriginalName());
            if (!IMAGE_SUFFIXES.contains(suffix)) {
                throw new ServiceException("评论仅支持 JPG、PNG、GIF、WEBP 图片");
            }
            DepartmentCommunityCommentMedia entity = new DepartmentCommunityCommentMedia();
            entity.setCommentId(commentId);
            entity.setOssId(ossId);
            entity.setMediaType(MEDIA_IMAGE);
            entity.setOriginalName(oss.getOriginalName());
            entity.setFileSuffix(suffix);
            entity.setContentType(contentType(null, MEDIA_IMAGE, suffix));
            entity.setFileSize(null);
            entity.setSortNum(index);
            entity.setCreateDept(LoginHelper.getDeptId());
            entity.setCreateBy(userId);
            entities.add(entity);
        }
        commentMediaMapper.deleteActiveByCommentId(commentId);
        entities.forEach(commentMediaMapper::insert);
    }

    private List<Long> parseMediaIds(String mediaOssIds) {
        if (StringUtils.isBlank(mediaOssIds)) {
            return List.of();
        }
        Set<Long> ids = new LinkedHashSet<>();
        Arrays.stream(mediaOssIds.split("[,，]"))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .forEach(value -> {
                try {
                    ids.add(Long.valueOf(value));
                } catch (NumberFormatException e) {
                    throw new ServiceException("媒体文件参数无效");
                }
            });
        return new ArrayList<>(ids);
    }

    private String fileSuffix(String fileName) {
        if (StringUtils.isBlank(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    }

    private String mediaType(String suffix) {
        if (IMAGE_SUFFIXES.contains(suffix)) return MEDIA_IMAGE;
        if (VIDEO_SUFFIXES.contains(suffix)) return MEDIA_VIDEO;
        return null;
    }

    private String contentType(String uploadContentType, String mediaType, String suffix) {
        boolean validUploadType = StringUtils.isNotBlank(uploadContentType)
            && ((MEDIA_IMAGE.equals(mediaType) && uploadContentType.toLowerCase().startsWith("image/"))
            || (MEDIA_VIDEO.equals(mediaType) && uploadContentType.toLowerCase().startsWith("video/")));
        if (validUploadType) {
            return uploadContentType;
        }
        if (MEDIA_IMAGE.equals(mediaType)) {
            return switch (suffix) {
                case ".png" -> "image/png";
                case ".gif" -> "image/gif";
                case ".webp" -> "image/webp";
                default -> "image/jpeg";
            };
        }
        return switch (suffix) {
            case ".webm" -> "video/webm";
            case ".ogg" -> "video/ogg";
            default -> "video/mp4";
        };
    }

    private DepartmentCommunityPost getAccessiblePost(Long id) {
        if (id == null) {
            throw new ServiceException("帖子主键不能为空");
        }
        DepartmentCommunityPost entity = postMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("帖子不存在");
        }
        if (Objects.equals(entity.getCreateBy(), LoginHelper.getUserId())) {
            return entity;
        }
        boolean publicPost = STATUS_PUBLISHED.equals(entity.getStatus()) || STATUS_RESOLVED.equals(entity.getStatus());
        boolean visible = VISIBILITY_ALL.equals(entity.getVisibility())
            || (VISIBILITY_DEPT.equals(entity.getVisibility()) && Objects.equals(entity.getDeptId(), LoginHelper.getDeptId()));
        if (publicPost && visible) {
            return entity;
        }
        throw new ServiceException("帖子不存在或您没有访问权限");
    }

    private void assertCanManage(DepartmentCommunityPost entity, String action) {
        if (!Objects.equals(entity.getCreateBy(), LoginHelper.getUserId()) && !isModerator()) {
            throw new ServiceException("您没有" + action + "的权限");
        }
    }

    private boolean isModerator() {
        return LoginHelper.isSuperAdmin() || StpUtil.hasPermission(PERMISSION_MODERATE);
    }

    private void assertModerator() {
        if (!isModerator()) {
            throw new ServiceException("您没有社区举报处理权限");
        }
    }

    private void notifyUser(Long userId, String message, Long postId) {
        if (userId == null || Objects.equals(userId, LoginHelper.getUserId())) {
            return;
        }
        try {
            PushPayloadDTO payload = PushPayloadDTO.of("MESSAGE", "BACKEND", message, Map.of("postId", postId, "module", "COMMUNITY"));
            payload.setPath("/department/community?id=" + postId);
            messageService.publishMessage(List.of(userId), payload);
        } catch (Exception ignored) {
            // 消息推送失败不影响社区正文、评论和互动事实。
        }
    }

    private String normalizePostType(String type) {
        if (TYPE_QUESTION.equalsIgnoreCase(type)) return TYPE_QUESTION;
        if (TYPE_EXPERIENCE.equalsIgnoreCase(type)) return TYPE_EXPERIENCE;
        if (TYPE_IMPROVEMENT.equalsIgnoreCase(type)) return TYPE_IMPROVEMENT;
        return TYPE_DISCUSSION;
    }

    private String normalizeVisibility(String visibility) {
        return VISIBILITY_DEPT.equalsIgnoreCase(visibility) ? VISIBILITY_DEPT : VISIBILITY_ALL;
    }

    private String normalizeStatus(String status) {
        return STATUS_DRAFT.equalsIgnoreCase(status) ? STATUS_DRAFT : STATUS_PUBLISHED;
    }

    private String normalizeReactionType(String type) {
        if (REACTION_FAVORITE.equalsIgnoreCase(type)) {
            return REACTION_FAVORITE;
        }
        if (REACTION_LIKE.equalsIgnoreCase(type)) {
            return REACTION_LIKE;
        }
        throw new ServiceException("不支持的互动类型");
    }

    private DepartmentCommunityReactionVo reactionResult(DepartmentCommunityPost entity, Long userId) {
        DepartmentCommunityReactionVo result = new DepartmentCommunityReactionVo();
        result.setLiked(reactionMapper.selectCount(Wrappers.<DepartmentCommunityReaction>lambdaQuery()
            .eq(DepartmentCommunityReaction::getPostId, entity.getId())
            .eq(DepartmentCommunityReaction::getUserId, userId)
            .eq(DepartmentCommunityReaction::getReactionType, REACTION_LIKE)) > 0);
        result.setFavorited(reactionMapper.selectCount(Wrappers.<DepartmentCommunityReaction>lambdaQuery()
            .eq(DepartmentCommunityReaction::getPostId, entity.getId())
            .eq(DepartmentCommunityReaction::getUserId, userId)
            .eq(DepartmentCommunityReaction::getReactionType, REACTION_FAVORITE)) > 0);
        result.setLikeCount(entity.getLikeCount() == null ? 0 : entity.getLikeCount());
        result.setFavoriteCount(entity.getFavoriteCount() == null ? 0 : entity.getFavoriteCount());
        return result;
    }
}
