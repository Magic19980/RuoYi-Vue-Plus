package org.dromara.department.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.FiveWhyBo;
import org.dromara.department.domain.bo.FiveWhyQueryBo;
import org.dromara.department.domain.bo.FiveWhyReviewBo;
import org.dromara.department.domain.vo.FiveWhyVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

/** 5WHY分析业务接口。 */
public interface IFiveWhyService {

    PageResult<FiveWhyVo> queryPageList(FiveWhyQueryBo bo, PageQuery pageQuery);

    FiveWhyVo queryById(Long id);

    Boolean insertByBo(FiveWhyBo bo);

    Boolean updateByBo(FiveWhyBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    Boolean review(FiveWhyReviewBo bo);

    SysOssVo uploadImage(MultipartFile file);

    void exportDocx(Long id, HttpServletResponse response) throws Exception;
}
