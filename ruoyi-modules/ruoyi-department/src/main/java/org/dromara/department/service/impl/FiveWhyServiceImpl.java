package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.FiveWhy;
import org.dromara.department.domain.bo.FiveWhyBo;
import org.dromara.department.domain.bo.FiveWhyImprovementBo;
import org.dromara.department.domain.bo.FiveWhyQueryBo;
import org.dromara.department.domain.bo.FiveWhyReviewBo;
import org.dromara.department.domain.bo.FiveWhyWhyBo;
import org.dromara.department.domain.vo.FiveWhyImprovementVo;
import org.dromara.department.domain.vo.FiveWhyVo;
import org.dromara.department.domain.vo.FiveWhyWhyVo;
import org.dromara.department.mapper.FiveWhyMapper;
import org.dromara.department.service.IFiveWhyService;
import org.dromara.department.service.IDepartmentTaskService;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** 5WHY分析业务实现。 */
@RequiredArgsConstructor
@Service
public class FiveWhyServiceImpl implements IFiveWhyService {

    private static final String REVIEW_PENDING = "PENDING";
    private static final String REVIEW_APPROVED = "APPROVED";
    private static final String REVIEW_REJECTED = "REJECTED";

    private final FiveWhyMapper fiveWhyMapper;
    private final FiveWhyDocxService fiveWhyDocxService;
    private final ISysOssService ossService;
    private final IDepartmentTaskService departmentTaskService;
    private final DepartmentAccessService departmentAccessService;

    @Override
    public PageResult<FiveWhyVo> queryPageList(FiveWhyQueryBo bo, PageQuery pageQuery) {
        FiveWhyQueryBo query = bo == null ? new FiveWhyQueryBo() : bo;
        Page<FiveWhy> page = pageQuery.build();
        Page<FiveWhy> result = fiveWhyMapper.selectPageList(page, query, scopeDeptId(), canViewAll());
        return PageResult.build(result.getRecords().stream().map(this::toVo).toList(), result.getTotal());
    }

    @Override
    public FiveWhyVo queryById(Long id) {
        return toVo(getAccessible(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(FiveWhyBo bo) {
        requireDept();
        validateWhys(bo.getWhys());
        FiveWhy entity = new FiveWhy();
        copyBo(bo, entity);
        entity.setDeptId(departmentAccessService.currentDeptId());
        entity.setAnalystUserId(bo.getAnalystUserId() == null ? LoginHelper.getUserId() : bo.getAnalystUserId());
        entity.setReviewStatus(REVIEW_PENDING);
        entity.setReviewComment(null);
        return fiveWhyMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(FiveWhyBo bo) {
        FiveWhy entity = getAccessible(bo.getId());
        validateWhys(bo.getWhys());
        copyBo(bo, entity);
        // 重新编辑后必须重新审核，避免导出和后续统计继续使用旧审核结果。
        entity.setReviewStatus(REVIEW_PENDING);
        entity.setReviewComment(null);
        return fiveWhyMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        for (Long id : ids) {
            getAccessible(id);
        }
        return fiveWhyMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean review(FiveWhyReviewBo bo) {
        FiveWhy entity = getAccessible(bo.getId());
        departmentTaskService.checkReviewer("FIVE_WHY", entity.getDeptId());
        if (!REVIEW_APPROVED.equals(bo.getReviewStatus()) && !REVIEW_REJECTED.equals(bo.getReviewStatus())) {
            throw new ServiceException("审核结果只能是通过或驳回");
        }
        entity.setReviewStatus(bo.getReviewStatus());
        entity.setReviewComment(bo.getReviewComment());
        entity.setReviewerUserId(LoginHelper.getUserId());
        return fiveWhyMapper.updateById(entity) > 0;
    }

    @Override
    public SysOssVo uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("图片不能为空");
        }
        String originalName = StringUtils.isBlank(file.getOriginalFilename()) ? "image" : file.getOriginalFilename();
        String lowerName = originalName.toLowerCase();
        if (!(lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png")
            || lowerName.endsWith(".gif") || lowerName.endsWith(".bmp"))) {
            throw new ServiceException("只支持JPG、PNG、GIF、BMP图片");
        }
        SysOssExt ossExt = new SysOssExt();
        ossExt.setBizType("DEPARTMENT_FIVE_WHY");
        ossExt.setSource("userUpload");
        ossExt.setRefType("FIVE_WHY");
        return ossService.upload(file, ossExt);
    }

    @Override
    public void exportDocx(Long id, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        fiveWhyDocxService.export(queryById(id), response);
    }

    private void copyBo(FiveWhyBo bo, FiveWhy entity) {
        entity.setCompanyDept(bo.getCompanyDept());
        entity.setEmployeeNo(bo.getEmployeeNo());
        entity.setAnalystName(bo.getAnalystName());
        if (bo.getAnalystUserId() != null) {
            entity.setAnalystUserId(bo.getAnalystUserId());
        }
        entity.setAnalysisDate(bo.getAnalysisDate());
        entity.setProblemName(bo.getProblemName());
        entity.setProblemDescription(bo.getProblemDescription());
        entity.setImpactScope(bo.getImpactScope());
        entity.setWhysJson(JsonUtils.toJsonString(bo.getWhys() == null ? List.of() : bo.getWhys()));
        entity.setImprovementsJson(JsonUtils.toJsonString(bo.getImprovements() == null ? List.of() : bo.getImprovements()));
        entity.setBeforeOssId(bo.getBeforeOssId());
        entity.setAfterOssId(bo.getAfterOssId());
        entity.setEffectVerification(bo.getEffectVerification());
        entity.setStandardizationPlan(bo.getStandardizationPlan());
        entity.setStandardizationExecution(bo.getStandardizationExecution());
    }

    private FiveWhyVo toVo(FiveWhy entity) {
        FiveWhyVo vo = new FiveWhyVo();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setCompanyDept(entity.getCompanyDept());
        vo.setEmployeeNo(entity.getEmployeeNo());
        vo.setAnalystUserId(entity.getAnalystUserId());
        vo.setAnalystName(entity.getAnalystName());
        vo.setAnalysisDate(entity.getAnalysisDate());
        vo.setProblemName(entity.getProblemName());
        vo.setProblemDescription(entity.getProblemDescription());
        vo.setImpactScope(entity.getImpactScope());
        vo.setWhys(parseList(entity.getWhysJson(), FiveWhyWhyVo.class));
        vo.setImprovements(parseList(entity.getImprovementsJson(), FiveWhyImprovementVo.class));
        vo.setBeforeOssId(entity.getBeforeOssId());
        vo.setAfterOssId(entity.getAfterOssId());
        vo.setEffectVerification(entity.getEffectVerification());
        vo.setStandardizationPlan(entity.getStandardizationPlan());
        vo.setStandardizationExecution(entity.getStandardizationExecution());
        vo.setReviewStatus(entity.getReviewStatus());
        vo.setReviewComment(entity.getReviewComment());
        vo.setReviewerUserId(entity.getReviewerUserId());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private <T> List<T> parseList(String json, Class<T> type) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return JsonUtils.parseArray(json, type);
        } catch (Exception ex) {
            throw new ServiceException("5WHY明细数据格式错误，请联系管理员");
        }
    }

    private void validateWhys(List<FiveWhyWhyBo> whys) {
        if (whys == null || whys.size() != 5) {
            throw new ServiceException("必须填写1至5共5层WHY分析");
        }
    }

    private FiveWhy getAccessible(Long id) {
        FiveWhy entity = fiveWhyMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("5WHY分析记录不存在");
        }
        if (departmentAccessService.canViewEntityDept(entity.getDeptId(), "department:fiveWhy:viewDept")) {
            return entity;
        }
        throw new ServiceException("您没有访问该5WHY分析的权限");
    }

    private void requireDept() {
        if (departmentAccessService.currentDeptId() == null) {
            throw new ServiceException("当前登录用户缺少部门信息，无法维护5WHY分析");
        }
    }

    private boolean canViewAll() {
        return false;
    }

    private Long scopeDeptId() {
        return canViewAll() ? null : departmentAccessService.scopeDeptId("department:fiveWhy:viewDept");
    }
}
