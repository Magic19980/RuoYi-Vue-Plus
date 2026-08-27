package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.PersonUserOptionQueryBo;
import org.dromara.department.domain.bo.ScoreProposalBo;
import org.dromara.department.domain.bo.ScoreProposalQueryBo;
import org.dromara.department.domain.bo.ScoreProposalReviewBo;
import org.dromara.department.domain.vo.PersonUserOptionVo;
import org.dromara.department.domain.vo.ScoreProposalMetricVo;
import org.dromara.department.domain.vo.ScoreProposalVo;
import org.dromara.department.service.IScoreProposalService;
import org.dromara.system.domain.vo.SysOssVo;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/** SCORE提案接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/scoreProposal")
public class ScoreProposalController extends BaseController {

    private final IScoreProposalService scoreProposalService;

    @SaCheckPermission("department:scoreProposal:list")
    @GetMapping("/list")
    public R<PageResult<ScoreProposalVo>> list(ScoreProposalQueryBo bo, PageQuery pageQuery) {
        return R.ok(scoreProposalService.queryPageList(bo, pageQuery));
    }

    /** 提案人员选择只展示当前业务科室人员档案中的有效成员。 */
    @SaCheckPermission("department:scoreProposal:query")
    @GetMapping("/memberOptions")
    public R<List<PersonUserOptionVo>> memberOptions() {
        return R.ok(scoreProposalService.queryMemberOptions());
    }

    /** 企业参与人员可从用户管理中的全部有效用户选择。 */
    @SaCheckPermission("department:scoreProposal:query")
    @GetMapping("/userOptions")
    public R<List<PersonUserOptionVo>> userOptions() {
        return R.ok(scoreProposalService.queryUserOptions());
    }

    /** 企业参与人员弹窗按条件分页查询用户管理中的全部有效人员。 */
    @SaCheckPermission("department:scoreProposal:query")
    @GetMapping("/userOptions/page")
    public R<PageResult<PersonUserOptionVo>> userOptionsPage(PersonUserOptionQueryBo bo, PageQuery pageQuery) {
        return R.ok(scoreProposalService.queryUserOptionsPage(bo, pageQuery));
    }

    /** 回显已选择的企业参与人员，避免打开弹窗时加载全部用户。 */
    @SaCheckPermission("department:scoreProposal:query")
    @GetMapping("/userOptions/selected")
    public R<List<PersonUserOptionVo>> selectedUserOptions(@RequestParam("ids") List<Long> ids) {
        return R.ok(scoreProposalService.queryUserOptionsByIds(ids));
    }

    /** 查询当前科室指定月份的精益提案指标。 */
    @SaCheckPermission("department:scoreProposal:query")
    @GetMapping("/metric")
    public R<ScoreProposalMetricVo> metric(@RequestParam(value = "month", required = false) String month) {
        return R.ok(scoreProposalService.queryMetric(month));
    }

    @SaCheckPermission("department:scoreProposal:query")
    @GetMapping("/{id}")
    public R<ScoreProposalVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(scoreProposalService.queryById(id));
    }

    @SaCheckPermission("department:scoreProposal:add")
    @Log(title = "SCORE提案", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ScoreProposalBo bo) {
        return toAjax(scoreProposalService.insertByBo(bo));
    }

    @SaCheckPermission("department:scoreProposal:edit")
    @Log(title = "SCORE提案", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ScoreProposalBo bo) {
        return toAjax(scoreProposalService.updateByBo(bo));
    }

    @SaCheckPermission("department:scoreProposal:remove")
    @Log(title = "SCORE提案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(scoreProposalService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @SaCheckPermission("department:scoreProposal:review")
    @Log(title = "SCORE提案审核", businessType = BusinessType.UPDATE)
    @PostMapping("/review")
    public R<Void> review(@Validated @RequestBody ScoreProposalReviewBo bo) {
        return toAjax(scoreProposalService.review(bo));
    }

    @SaCheckPermission("department:scoreProposal:edit")
    @PostMapping(value = "/uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssVo> uploadImage(@RequestPart("file") MultipartFile file) {
        return R.ok(scoreProposalService.uploadImage(file));
    }

    @SaCheckPermission("department:scoreProposal:export")
    @Log(title = "SCORE提案", businessType = BusinessType.EXPORT)
    @PostMapping("/export/{id}")
    public void export(@NotNull(message = "主键不能为空") @PathVariable Long id, HttpServletResponse response) throws Exception {
        scoreProposalService.exportXlsx(id, response);
    }

    /** 在线预览提交审核时固化的 XLSX 内容，预览不允许修改原文件。 */
    @SaCheckPermission("department:scoreProposal:query")
    @GetMapping(value = "/preview/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String preview(@NotNull(message = "主键不能为空") @PathVariable Long id) throws Exception {
        return scoreProposalService.previewXlsx(id);
    }
}
