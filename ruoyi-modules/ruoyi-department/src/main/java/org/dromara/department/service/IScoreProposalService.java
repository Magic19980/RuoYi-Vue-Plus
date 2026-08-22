package org.dromara.department.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.ScoreProposalBo;
import org.dromara.department.domain.bo.ScoreProposalQueryBo;
import org.dromara.department.domain.bo.ScoreProposalReviewBo;
import org.dromara.department.domain.vo.ScoreProposalVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

/** SCORE提案业务接口。 */
public interface IScoreProposalService {

    PageResult<ScoreProposalVo> queryPageList(ScoreProposalQueryBo bo, PageQuery pageQuery);

    ScoreProposalVo queryById(Long id);

    Boolean insertByBo(ScoreProposalBo bo);

    Boolean updateByBo(ScoreProposalBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    Boolean review(ScoreProposalReviewBo bo);

    SysOssVo uploadImage(MultipartFile file);

    void exportXlsx(Long id, HttpServletResponse response) throws Exception;
}
