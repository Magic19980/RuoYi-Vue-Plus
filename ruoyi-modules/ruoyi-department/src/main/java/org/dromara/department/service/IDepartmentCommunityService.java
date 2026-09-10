package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
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

import org.springframework.web.multipart.MultipartFile;

/**
 * 协作社区业务接口。
 */
public interface IDepartmentCommunityService {

    /** 分页查询协作社区帖子。 */
    PageResult<DepartmentCommunityPostVo> queryPageList(DepartmentCommunityPostQueryBo bo, PageQuery pageQuery);

    /** 查询帖子详情及其关联内容。 */
    DepartmentCommunityPostVo queryById(Long id);

    /** 发布协作社区帖子。 */
    Boolean insertByBo(DepartmentCommunityPostBo bo);

    /** 修改协作社区帖子。 */
    Boolean updateByBo(DepartmentCommunityPostBo bo);

    /** 删除协作社区帖子及其关联数据。 */
    Boolean deleteById(Long id);

    /** 分页查询帖子评论。 */
    PageResult<DepartmentCommunityCommentVo> queryComments(Long postId, PageQuery pageQuery);

    /** 新增帖子评论。 */
    Boolean addComment(Long postId, DepartmentCommunityCommentBo bo);

    /** 删除帖子评论。 */
    Boolean deleteComment(Long id);

    /** 切换当前用户对帖子的互动状态。 */
    DepartmentCommunityReactionVo toggleReaction(Long postId, String reactionType);

    /** 将指定评论标记为帖子的解决方案。 */
    Boolean resolve(Long postId, Long commentId);

    /** 分页查询社区举报记录。 */
    PageResult<DepartmentCommunityReportVo> queryReportPageList(DepartmentCommunityReportQueryBo bo, PageQuery pageQuery);

    /** 提交帖子举报。 */
    Boolean report(Long postId, DepartmentCommunityReportBo bo);

    /** 处理社区举报。 */
    Boolean handleReport(DepartmentCommunityReportBo bo);

    /** 上传帖子媒体附件。 */
    DepartmentCommunityMediaVo uploadMedia(MultipartFile file);

    /** 上传评论媒体附件。 */
    DepartmentCommunityMediaVo uploadCommentMedia(MultipartFile file);
}
