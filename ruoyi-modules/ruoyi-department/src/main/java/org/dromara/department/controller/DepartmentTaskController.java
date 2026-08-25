package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.department.domain.bo.DepartmentReviewRuleBo;
import org.dromara.department.domain.bo.DepartmentTaskAssignmentBo;
import org.dromara.department.domain.bo.DepartmentTaskRuleBo;
import org.dromara.department.domain.vo.DepartmentReviewRuleVo;
import org.dromara.department.domain.vo.DepartmentTaskAssignmentVo;
import org.dromara.department.domain.vo.DepartmentTaskProgressVo;
import org.dromara.department.domain.vo.DepartmentTaskRuleVo;
import org.dromara.department.service.IDepartmentTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 科室审核配置和周期任务接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department/task")
public class DepartmentTaskController extends BaseController {

    private final IDepartmentTaskService taskService;

    @SaCheckPermission("department:task:list")
    @GetMapping("/rule/list")
    public R<List<DepartmentTaskRuleVo>> ruleList() {
        return R.ok(taskService.queryRuleList());
    }

    @SaCheckPermission("department:task:query")
    @GetMapping("/rule/{id}")
    public R<DepartmentTaskRuleVo> ruleInfo(@NotNull @PathVariable Long id) {
        return R.ok(taskService.queryRuleById(id));
    }

    @SaCheckPermission("department:task:add")
    @Log(title = "科室任务规则", businessType = BusinessType.INSERT)
    @PostMapping("/rule")
    public R<Void> addRule(@Validated(AddGroup.class) @RequestBody DepartmentTaskRuleBo bo) {
        return toAjax(taskService.saveRule(bo));
    }

    @SaCheckPermission("department:task:edit")
    @Log(title = "科室任务规则", businessType = BusinessType.UPDATE)
    @PutMapping("/rule")
    public R<Void> editRule(@Validated(EditGroup.class) @RequestBody DepartmentTaskRuleBo bo) {
        return toAjax(taskService.saveRule(bo));
    }

    @SaCheckPermission("department:task:remove")
    @Log(title = "科室任务规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/rule/{id}")
    public R<Void> removeRule(@NotNull @PathVariable Long id) {
        return toAjax(taskService.deleteRule(id));
    }

    @SaCheckPermission("department:task:list")
    @GetMapping("/assignment/list/{ruleId}")
    public R<List<DepartmentTaskAssignmentVo>> assignmentList(@NotNull @PathVariable Long ruleId) {
        return R.ok(taskService.queryAssignments(ruleId));
    }

    @SaCheckPermission("department:task:edit")
    @Log(title = "科室任务成员分配", businessType = BusinessType.INSERT)
    @PostMapping("/assignment")
    public R<Void> addAssignment(@Validated @RequestBody DepartmentTaskAssignmentBo bo) {
        return toAjax(taskService.saveAssignment(bo));
    }

    @SaCheckPermission("department:task:edit")
    @Log(title = "科室任务成员分配", businessType = BusinessType.UPDATE)
    @PutMapping("/assignment")
    public R<Void> editAssignment(@Validated @RequestBody DepartmentTaskAssignmentBo bo) {
        return toAjax(taskService.saveAssignment(bo));
    }

    @SaCheckPermission("department:task:edit")
    @Log(title = "科室任务成员分配", businessType = BusinessType.DELETE)
    @DeleteMapping("/assignment/{id}")
    public R<Void> removeAssignment(@NotNull @PathVariable Long id) {
        return toAjax(taskService.deleteAssignment(id));
    }

    @SaCheckPermission("department:task:list")
    @GetMapping("/my")
    public R<List<DepartmentTaskProgressVo>> myTasks() {
        return R.ok(taskService.queryMyTasks());
    }

    @SaCheckPermission("department:task:list")
    @GetMapping("/review/list")
    public R<List<DepartmentReviewRuleVo>> reviewList() {
        return R.ok(taskService.queryReviewRuleList());
    }

    @SaCheckPermission("department:task:reviewConfig")
    @Log(title = "科室审核人配置", businessType = BusinessType.INSERT)
    @PostMapping("/review")
    public R<Void> addReview(@Validated(AddGroup.class) @RequestBody DepartmentReviewRuleBo bo) {
        return toAjax(taskService.saveReviewRule(bo));
    }

    @SaCheckPermission("department:task:reviewConfig")
    @Log(title = "科室审核人配置", businessType = BusinessType.UPDATE)
    @PutMapping("/review")
    public R<Void> editReview(@Validated(EditGroup.class) @RequestBody DepartmentReviewRuleBo bo) {
        return toAjax(taskService.saveReviewRule(bo));
    }

    @SaCheckPermission("department:task:reviewConfig")
    @Log(title = "科室审核人配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/review/{id}")
    public R<Void> removeReview(@NotNull @PathVariable Long id) {
        return toAjax(taskService.deleteReviewRule(id));
    }
}
