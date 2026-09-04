package org.dromara.ecology.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.ecology.domain.bo.OaApplicationBo;
import org.dromara.ecology.domain.bo.OaApplicationQueryBo;
import org.dromara.ecology.domain.OaCallbackCommand;
import org.dromara.ecology.domain.vo.OaApplicationVo;
import org.dromara.ecology.domain.vo.OaApprovalRulePreviewVo;
import org.dromara.ecology.domain.vo.OaAttachmentPreviewVo;
import org.dromara.ecology.domain.vo.OaProcessEventLogVo;
import org.dromara.system.domain.vo.SysDeptVo;
import org.springframework.http.ResponseEntity;

import java.util.List;

/** 泛微通用审批申请服务。 */
public interface IOaApplicationService {

    PageResult<OaApplicationVo> queryPage(OaApplicationQueryBo bo, PageQuery pageQuery);

    OaApplicationVo queryById(Long id);

    /** 预览申请中已生成的 Excel 附件。 */
    OaAttachmentPreviewVo previewAttachment(Long ossId);

    /** 下载申请中已生成的附件，并复用申请访问权限校验。 */
    ResponseEntity<byte[]> downloadAttachment(Long ossId);

    /**
     * 查询审批发起所需的有效泛微组织节点。
     *
     * @param keyword  部门名称搜索关键字
     * @param deptIds  编辑申请时用于回显已选部门
     * @param parentId 懒加载直属子节点时的父节点，0 表示根节点
     */
    List<SysDeptVo> queryOaDepartments(String keyword, List<Long> deptIds, Long parentId);

    OaApplicationVo save(OaApplicationBo bo);

    OaApplicationVo submit(Long id);

    OaApplicationVo sync(Long id);

    /** 预览申请提交时按业务规则解析出的审批链。 */
    List<OaApprovalRulePreviewVo> previewParticipants(Long id);

    List<OaProcessEventLogVo> queryEvents(Long id);

    /** 处理已完成验签的泛微回调。 */
    void handleCallback(OaCallbackCommand command);

    /** 执行一轮到期对账。 */
    void reconcileDue();
}
