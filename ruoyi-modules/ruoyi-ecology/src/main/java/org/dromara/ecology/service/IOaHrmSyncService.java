package org.dromara.ecology.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.ecology.domain.vo.OaOrganizationTreeVo;
import org.dromara.ecology.domain.vo.OaSyncBatchVo;
import org.dromara.ecology.domain.vo.OaSyncDetailVo;
import org.dromara.ecology.domain.vo.OaSyncResultVo;
import org.dromara.ecology.domain.bo.OaHrmUserPasswordBo;
import org.dromara.ecology.domain.vo.OaHrmUserPasswordVo;

import java.util.List;

/** 泛微 HRM 组织与人员同步服务。 */
public interface IOaHrmSyncService {

    OaSyncResultVo syncOrganization(boolean full);

    OaSyncResultVo syncUsers(boolean full);

    /** 查询泛微新人员初始密码是否已配置，不返回密码内容。 */
    OaHrmUserPasswordVo queryUserPasswordStatus();

    /** 保存泛微新人员初始密码。 */
    void updateUserPassword(OaHrmUserPasswordBo bo);

    List<OaOrganizationTreeVo> queryOrganizationTree(boolean includeDisabled, String subcompanyId);

    PageResult<OaSyncBatchVo> queryBatches(String syncType, PageQuery pageQuery);

    PageResult<OaSyncDetailVo> queryDetails(Long batchId, String detailStatus, PageQuery pageQuery);

}
