package org.dromara.ecology.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.ecology.domain.OaBusinessWorkflowBinding;
import org.dromara.ecology.domain.OaBusinessWorkflowOption;
import org.dromara.ecology.domain.OaFormWorkflow;
import org.dromara.ecology.domain.OaFormWorkflowOption;
import org.dromara.ecology.domain.OaWorkflowConfig;
import org.dromara.ecology.domain.bo.OaFormWorkflowBo;
import org.dromara.ecology.domain.bo.OaWorkflowOptionBo;
import org.dromara.ecology.domain.vo.OaFormWorkflowVo;
import org.dromara.ecology.domain.vo.OaWorkflowConfigVo;
import org.dromara.ecology.domain.vo.OaWorkflowOptionVo;
import org.dromara.ecology.mapper.OaBusinessWorkflowBindingMapper;
import org.dromara.ecology.mapper.OaBusinessWorkflowOptionMapper;
import org.dromara.ecology.mapper.OaFormWorkflowMapper;
import org.dromara.ecology.mapper.OaFormWorkflowOptionMapper;
import org.dromara.ecology.mapper.OaProcessInstanceMapper;
import org.dromara.ecology.service.IOaWorkflowConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 泛微表单、审批方式以及提交时使用的审批选项服务。 */
@Service
@RequiredArgsConstructor
public class OaWorkflowConfigServiceImpl implements IOaWorkflowConfigService {

    private static final String ENABLED = "ENABLED";
    private static final String DISABLED = "DISABLED";

    private final OaFormWorkflowMapper formMapper;
    private final OaFormWorkflowOptionMapper optionMapper;
    private final OaBusinessWorkflowBindingMapper bindingMapper;
    private final OaBusinessWorkflowOptionMapper bindingOptionMapper;
    private final OaProcessInstanceMapper processMapper;

    @Override
    public List<OaWorkflowConfigVo> queryList(String businessType, boolean enabledOnly) {
        List<OaFormWorkflowOption> options = optionMapper.selectList(Wrappers.<OaFormWorkflowOption>lambdaQuery()
            .eq(enabledOnly, OaFormWorkflowOption::getStatus, ENABLED)
            .orderByAsc(OaFormWorkflowOption::getSortNo)
            .orderByAsc(OaFormWorkflowOption::getId));
        if (options.isEmpty()) return List.of();

        if (StringUtils.isBlank(businessType)) {
            return options.stream()
                .map(option -> toVo(null, option, false))
                .toList();
        }

        OaBusinessWorkflowBinding binding = bindingMapper.selectOne(Wrappers.<OaBusinessWorkflowBinding>lambdaQuery()
            .eq(OaBusinessWorkflowBinding::getBusinessType, StringUtils.trim(businessType))
            .eq(OaBusinessWorkflowBinding::getStatus, ENABLED));
        if (binding == null) return List.of();
        OaFormWorkflow form = formMapper.selectOne(Wrappers.<OaFormWorkflow>lambdaQuery()
            .eq(OaFormWorkflow::getId, binding.getFormId())
            .eq(OaFormWorkflow::getStatus, ENABLED));
        if (form == null) return List.of();
        Set<Long> allowedIds = bindingOptionMapper.selectList(Wrappers.<OaBusinessWorkflowOption>lambdaQuery()
                .eq(OaBusinessWorkflowOption::getBindingId, binding.getId()))
            .stream().map(OaBusinessWorkflowOption::getOptionId).collect(Collectors.toSet());
        return options.stream()
            .filter(option -> allowedIds.contains(option.getId()))
            .map(option -> toVo(form, option, binding.getDefaultOptionId() != null
                && binding.getDefaultOptionId().equals(option.getId())))
            .toList();
    }

    @Override
    public OaWorkflowConfigVo queryById(Long id) {
        return toVo(requireEnabled(id, null), false);
    }

    @Override
    public OaWorkflowConfigVo queryById(Long id, String businessType) {
        return toVo(requireEnabled(id, businessType), false);
    }

    @Override
    public List<OaFormWorkflowVo> queryFormList(boolean enabledOnly) {
        List<OaFormWorkflow> forms = formMapper.selectList(Wrappers.<OaFormWorkflow>lambdaQuery()
            .eq(enabledOnly, OaFormWorkflow::getStatus, ENABLED)
            .orderByAsc(OaFormWorkflow::getFormName)
            .orderByAsc(OaFormWorkflow::getWorkflowId));
        if (forms.isEmpty()) return List.of();
        return forms.stream().map(form -> toFormVo(form, List.of())).toList();
    }

    @Override
    public OaFormWorkflowVo queryFormById(Long id) {
        OaFormWorkflow form = requireForm(id, false);
        return toFormVo(form, List.of());
    }

    @Override
    public List<OaWorkflowOptionVo> queryOptions(boolean enabledOnly) {
        return queryOptionEntities(enabledOnly).stream().map(this::toOptionVo).toList();
    }

    private List<OaFormWorkflowOption> queryOptionEntities(boolean enabledOnly) {
        return optionMapper.selectList(Wrappers.<OaFormWorkflowOption>lambdaQuery()
                .eq(enabledOnly, OaFormWorkflowOption::getStatus, ENABLED)
                .orderByAsc(OaFormWorkflowOption::getSortNo)
                .orderByAsc(OaFormWorkflowOption::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertFormByBo(OaFormWorkflowBo bo) {
        validateForm(bo);
        OaFormWorkflow form = new OaFormWorkflow();
        copyForm(bo, form);
        formMapper.insert(form);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFormByBo(OaFormWorkflowBo bo) {
        validateForm(bo);
        OaFormWorkflow form = requireForm(bo.getId(), false);
        copyForm(bo, form);
        formMapper.updateById(form);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertOptionByBo(OaWorkflowOptionBo bo) {
        validateOption(bo, null);
        OaFormWorkflowOption option = new OaFormWorkflowOption();
        copyOption(bo, option);
        return optionMapper.insert(option) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOptionByBo(OaWorkflowOptionBo bo) {
        if (bo == null || bo.getId() == null) throw new ServiceException("审批方式主键不能为空");
        OaFormWorkflowOption option = optionMapper.selectById(bo.getId());
        if (option == null) throw new ServiceException("审批方式配置不存在");
        validateOption(bo, option.getId());
        copyOption(bo, option);
        return optionMapper.updateById(option) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteOptionById(Long id) {
        OaFormWorkflowOption option = optionMapper.selectById(id);
        if (option == null) throw new ServiceException("审批方式配置不存在");
        long bindingCount = bindingOptionMapper.selectCount(Wrappers.<OaBusinessWorkflowOption>lambdaQuery()
            .eq(OaBusinessWorkflowOption::getOptionId, id));
        if (bindingCount > 0 || processMapper.countByWorkflowConfigId(id) > 0) {
            option.setStatus(DISABLED);
            return optionMapper.updateById(option) > 0;
        }
        return optionMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFormById(Long id) {
        requireForm(id, false);
        if (bindingMapper.selectCount(Wrappers.<OaBusinessWorkflowBinding>lambdaQuery()
            .eq(OaBusinessWorkflowBinding::getFormId, id)
            .eq(OaBusinessWorkflowBinding::getStatus, ENABLED)) > 0) {
            throw new ServiceException("该表单已被业务类型使用，请先在业务配置中解除绑定");
        }
        return formMapper.deleteById(id) > 0;
    }

    @Override
    public OaWorkflowConfig requireEnabled(Long id, String businessType) {
        OaFormWorkflowOption option = optionMapper.selectOne(Wrappers.<OaFormWorkflowOption>lambdaQuery()
            .eq(OaFormWorkflowOption::getId, id)
            .eq(OaFormWorkflowOption::getStatus, ENABLED));
        if (option == null) throw new ServiceException("审批方式不存在或已停用");
        OaFormWorkflow form = null;
        if (StringUtils.isNotBlank(businessType)) {
            OaBusinessWorkflowBinding binding = bindingMapper.selectOne(Wrappers.<OaBusinessWorkflowBinding>lambdaQuery()
                .eq(OaBusinessWorkflowBinding::getBusinessType, StringUtils.trim(businessType))
                .eq(OaBusinessWorkflowBinding::getStatus, ENABLED));
            if (binding == null) throw new ServiceException("当前业务尚未绑定泛微表单");
            form = findForm(binding.getFormId(), true);
            if (form == null) throw new ServiceException("当前业务绑定的泛微表单不存在或已停用");
            validateBinding(StringUtils.trim(businessType), form.getId(), option.getId());
        }
        return toProjection(form, option);
    }

    private void validateBinding(String businessType, Long formId, Long optionId) {
        OaBusinessWorkflowBinding binding = bindingMapper.selectOne(Wrappers.<OaBusinessWorkflowBinding>lambdaQuery()
            .eq(OaBusinessWorkflowBinding::getBusinessType, businessType)
            .eq(OaBusinessWorkflowBinding::getStatus, ENABLED));
        if (binding == null || !formId.equals(binding.getFormId())) throw new ServiceException("当前业务尚未绑定该泛微表单");
        if (bindingOptionMapper.selectCount(Wrappers.<OaBusinessWorkflowOption>lambdaQuery()
            .eq(OaBusinessWorkflowOption::getBindingId, binding.getId())
            .eq(OaBusinessWorkflowOption::getOptionId, optionId)) == 0) {
            throw new ServiceException("当前审批方式未被该业务启用");
        }
    }

    private void validateForm(OaFormWorkflowBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getWorkflowId()) || StringUtils.isBlank(bo.getFormName())) {
            throw new ServiceException("泛微表单名称和 workflowId 不能为空");
        }
        validateFieldSchema(bo.getFieldSchemaJson());
        if (StringUtils.isNotBlank(bo.getFieldMappingJson()) && !JsonUtils.isJsonObject(bo.getFieldMappingJson())) {
            throw new ServiceException("公用字段映射必须是 JSON 对象");
        }
        if (StringUtils.isNotBlank(bo.getSpecificFieldMappingJson())
            && !JsonUtils.isJsonObject(bo.getSpecificFieldMappingJson())) {
            throw new ServiceException("表单专属字段映射必须是 JSON 对象");
        }
    }

    /** 校验管理员维护的动态字段定义，避免申请页渲染出无法提交的字段。 */
    private void validateFieldSchema(String json) {
        if (StringUtils.isBlank(json)) {
            throw new ServiceException("请配置至少一个表单字段");
        }
        if (!JsonUtils.isJsonObject(json)) {
            throw new ServiceException("表单字段定义必须是 JSON 对象");
        }
        Map<String, Object> schema = JsonUtils.parseMap(json);
        Object rawFields = schema.get("fields");
        if (!(rawFields instanceof Iterable<?> fields)) {
            throw new ServiceException("表单字段定义缺少 fields 数组");
        }
        Set<String> keys = new HashSet<>();
        Set<String> oaCodes = new HashSet<>();
        int count = 0;
        for (Object rawField : fields) {
            if (!(rawField instanceof Map<?, ?> field)) {
                throw new ServiceException("表单字段定义中存在无效字段");
            }
            String key = text(field.get("key"));
            String label = text(field.get("label"));
            String oaFieldCode = text(field.get("oaFieldCode"));
            String controlType = text(field.get("controlType")).toUpperCase();
            if (StringUtils.isBlank(key) || StringUtils.isBlank(label)
                || StringUtils.isBlank(oaFieldCode) || StringUtils.isBlank(controlType)) {
                throw new ServiceException("每个表单字段都必须填写字段标识、显示名称、泛微字段编码和控件类型");
            }
            if (!keys.add(key)) {
                throw new ServiceException("表单字段标识不能重复：" + key);
            }
            if (!oaCodes.add(oaFieldCode)) {
                throw new ServiceException("泛微字段编码不能重复：" + oaFieldCode);
            }
            if (("SELECT".equals(controlType) || "RADIO".equals(controlType))
                && !hasValidOptions(field.get("options"))) {
                throw new ServiceException("选择类字段必须配置选项：" + label);
            }
            count++;
        }
        if (count == 0) {
            throw new ServiceException("请配置至少一个表单字段");
        }
    }

    private boolean hasValidOptions(Object rawOptions) {
        if (!(rawOptions instanceof Iterable<?> options)) {
            return false;
        }
        int count = 0;
        Set<String> values = new HashSet<>();
        for (Object rawOption : options) {
            if (!(rawOption instanceof Map<?, ?> option)) {
                return false;
            }
            String label = text(option.get("label"));
            String oaValue = text(option.get("oaValue"));
            if (StringUtils.isBlank(label) || StringUtils.isBlank(oaValue) || !values.add(oaValue)) {
                return false;
            }
            count++;
        }
        return count > 0;
    }

    private String text(Object value) {
        return value == null ? "" : StringUtils.trim(String.valueOf(value));
    }

    private void validateOption(OaWorkflowOptionBo bo, Long currentId) {
        if (bo == null || StringUtils.isBlank(bo.getOptionCode()) || StringUtils.isBlank(bo.getOptionName())) {
            throw new ServiceException("审批方式编码和名称不能为空");
        }
        validateParticipantMapping(bo.getParticipantMappingJson());
        var query = Wrappers.<OaFormWorkflowOption>lambdaQuery()
            .eq(OaFormWorkflowOption::getOptionCode, StringUtils.trim(bo.getOptionCode()));
        if (currentId != null) query.ne(OaFormWorkflowOption::getId, currentId);
        if (optionMapper.selectCount(query) > 0) throw new ServiceException("审批方式编码不能重复");
    }

    private void validateParticipantMapping(String json) {
        if (StringUtils.isBlank(json) || !JsonUtils.isJsonObject(json)) {
            throw new ServiceException("请配置审批节点字段映射");
        }
        Map<String, Object> mapping = JsonUtils.parseMap(json);
        Object stages = mapping.get("stages");
        if (!(stages instanceof Iterable<?> values)) {
            throw new ServiceException("审批节点字段映射至少需要一个节点");
        }
        boolean valid = false;
        for (Object value : values) {
            if (!(value instanceof Map<?, ?> stage)) continue;
            Object rawCode = stage.get("code");
            Object rawField = stage.containsKey("fieldCode") ? stage.get("fieldCode") : stage.get("field");
            String code = String.valueOf(rawCode == null ? "" : rawCode).trim();
            String field = String.valueOf(rawField == null ? "" : rawField).trim();
            if (StringUtils.isNotBlank(code) && StringUtils.isNotBlank(field)) {
                valid = true;
                break;
            }
        }
        if (!valid) throw new ServiceException("审批节点字段映射至少需要一个有效节点");
    }

    private OaFormWorkflow requireForm(Long id, boolean enabledOnly) {
        OaFormWorkflow form = formMapper.selectOne(Wrappers.<OaFormWorkflow>lambdaQuery()
            .eq(OaFormWorkflow::getId, id)
            .eq(enabledOnly, OaFormWorkflow::getStatus, ENABLED));
        if (form == null) throw new ServiceException("泛微表单配置不存在");
        return form;
    }

    private OaFormWorkflow findForm(Long id, boolean enabledOnly) {
        if (id == null) return null;
        return formMapper.selectOne(Wrappers.<OaFormWorkflow>lambdaQuery()
            .eq(OaFormWorkflow::getId, id)
            .eq(enabledOnly, OaFormWorkflow::getStatus, ENABLED));
    }

    private void copyForm(OaFormWorkflowBo bo, OaFormWorkflow form) {
        form.setWorkflowId(StringUtils.trim(bo.getWorkflowId()));
        form.setFormName(StringUtils.trim(bo.getFormName()));
        form.setRequestNameTemplate(StringUtils.trim(bo.getRequestNameTemplate()));
        form.setFieldMappingJson(StringUtils.trim(bo.getFieldMappingJson()));
        form.setSpecificFieldMappingJson(StringUtils.trim(bo.getSpecificFieldMappingJson()));
        form.setFieldSchemaJson(StringUtils.trim(bo.getFieldSchemaJson()));
        form.setStatus(DISABLED.equalsIgnoreCase(bo.getStatus()) ? DISABLED : ENABLED);
        form.setRemark(StringUtils.trim(bo.getRemark()));
    }

    private void copyOption(OaWorkflowOptionBo bo, OaFormWorkflowOption option) {
        option.setOptionCode(StringUtils.trim(bo.getOptionCode()));
        option.setOptionName(StringUtils.trim(bo.getOptionName()));
        // 该列仅为数据库历史字段；新模型不使用本地流程模式，但写入非空默认值避免旧表约束影响新增。
        option.setProcessType(StringUtils.defaultIfBlank(bo.getProcessType(), "CUSTOM"));
        option.setParticipantMappingJson(StringUtils.trim(bo.getParticipantMappingJson()));
        option.setSortNo(bo.getSortNo());
        option.setStatus(DISABLED.equalsIgnoreCase(bo.getStatus()) ? DISABLED : ENABLED);
        option.setRemark(StringUtils.trim(bo.getRemark()));
    }

    private OaWorkflowConfig toProjection(OaFormWorkflow form, OaFormWorkflowOption option) {
        OaWorkflowConfig result = new OaWorkflowConfig();
        result.setId(option.getId());
        if (form != null) {
            result.setFormId(form.getId());
            result.setWorkflowId(form.getWorkflowId());
            result.setFormName(form.getFormName());
            result.setRequestNameTemplate(form.getRequestNameTemplate());
            result.setFieldMappingJson(form.getFieldMappingJson());
            result.setSpecificFieldMappingJson(form.getSpecificFieldMappingJson());
            result.setFieldSchemaJson(form.getFieldSchemaJson());
            result.setStatus(form.getStatus());
        }
        result.setWorkflowName(option.getOptionName());
        result.setApprovalCode(option.getOptionCode());
        result.setApprovalName(option.getOptionName());
        result.setProcessType(option.getProcessType());
        result.setParticipantMappingJson(option.getParticipantMappingJson());
        if (form == null) result.setStatus(option.getStatus());
        result.setRemark(option.getRemark());
        return result;
    }

    private OaWorkflowConfigVo toVo(OaWorkflowConfig projection, boolean isDefault) {
        OaWorkflowConfigVo vo = new OaWorkflowConfigVo();
        vo.setId(projection.getId());
        vo.setFormId(projection.getFormId());
        vo.setWorkflowId(projection.getWorkflowId());
        vo.setWorkflowName(projection.getWorkflowName());
        vo.setFormName(projection.getFormName());
        vo.setApprovalCode(projection.getApprovalCode());
        vo.setApprovalName(projection.getApprovalName());
        vo.setProcessType(projection.getProcessType());
        vo.setParticipantMappingJson(projection.getParticipantMappingJson());
        vo.setRequestNameTemplate(projection.getRequestNameTemplate());
        vo.setFieldMappingJson(projection.getFieldMappingJson());
        vo.setSpecificFieldMappingJson(projection.getSpecificFieldMappingJson());
        vo.setFieldSchemaJson(projection.getFieldSchemaJson());
        vo.setStatus(projection.getStatus());
        vo.setRemark(projection.getRemark());
        vo.setIsDefault(isDefault);
        return vo;
    }

    private OaWorkflowConfigVo toVo(OaFormWorkflow form, OaFormWorkflowOption option, boolean isDefault) {
        return toVo(toProjection(form, option), isDefault);
    }

    private OaFormWorkflowVo toFormVo(OaFormWorkflow form, List<OaFormWorkflowOption> options) {
        OaFormWorkflowVo vo = new OaFormWorkflowVo();
        vo.setId(form.getId());
        vo.setWorkflowId(form.getWorkflowId());
        vo.setFormName(form.getFormName());
        vo.setRequestNameTemplate(form.getRequestNameTemplate());
        vo.setFieldMappingJson(form.getFieldMappingJson());
        vo.setSpecificFieldMappingJson(form.getSpecificFieldMappingJson());
        vo.setFieldSchemaJson(form.getFieldSchemaJson());
        vo.setStatus(form.getStatus());
        vo.setRemark(form.getRemark());
        vo.setCreateTime(form.getCreateTime());
        vo.setUpdateTime(form.getUpdateTime());
        vo.setOptions(options.stream().map(this::toOptionVo).toList());
        return vo;
    }

    private OaWorkflowOptionVo toOptionVo(OaFormWorkflowOption option) {
        OaWorkflowOptionVo vo = new OaWorkflowOptionVo();
        vo.setId(option.getId());
        vo.setOptionCode(option.getOptionCode());
        vo.setOptionName(option.getOptionName());
        vo.setProcessType(option.getProcessType());
        vo.setParticipantMappingJson(option.getParticipantMappingJson());
        vo.setSortNo(option.getSortNo());
        vo.setStatus(option.getStatus());
        vo.setRemark(option.getRemark());
        vo.setCreateTime(option.getCreateTime());
        vo.setUpdateTime(option.getUpdateTime());
        return vo;
    }
}
