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

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 协作社区业务接口。
 */
public interface IDepartmentCommunityService {

    PageResult<DepartmentCommunityPostVo> queryPageList(DepartmentCommunityPostQueryBo bo, PageQuery pageQuery);

    DepartmentCommunityPostVo queryById(Long id);

    Boolean insertByBo(DepartmentCommunityPostBo bo);

    Boolean updateByBo(DepartmentCommunityPostBo bo);

    Boolean deleteById(Long id);

    List<DepartmentCommunityCommentVo> queryComments(Long postId);

    Boolean addComment(Long postId, DepartmentCommunityCommentBo bo);

    Boolean deleteComment(Long id);

    DepartmentCommunityReactionVo toggleReaction(Long postId, String reactionType);

    Boolean resolve(Long postId, Long commentId);

    PageResult<DepartmentCommunityReportVo> queryReportPageList(DepartmentCommunityReportQueryBo bo, PageQuery pageQuery);

    Boolean report(Long postId, DepartmentCommunityReportBo bo);

    Boolean handleReport(DepartmentCommunityReportBo bo);

    DepartmentCommunityMediaVo uploadMedia(MultipartFile file);

    DepartmentCommunityMediaVo uploadCommentMedia(MultipartFile file);
}
