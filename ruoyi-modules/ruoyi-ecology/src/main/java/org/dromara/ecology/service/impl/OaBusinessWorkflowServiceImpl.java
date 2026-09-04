package org.dromara.ecology.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.ecology.domain.OaBusinessWorkflowBinding;
import org.dromara.ecology.domain.OaBusinessWorkflowOption;
import org.dromara.ecology.domain.OaFormWorkflow;
import org.dromara.ecology.domain.OaFormWorkflowOption;
import org.dromara.ecology.domain.OaBusinessType;
import org.dromara.ecology.domain.bo.OaBusinessWorkflowBindingBo;
import org.dromara.ecology.domain.vo.OaBusinessWorkflowBindingVo;
import org.dromara.ecology.mapper.OaBusinessTypeMapper;
import org.dromara.ecology.mapper.OaBusinessWorkflowBindingMapper;
import org.dromara.ecology.mapper.OaBusinessWorkflowOptionMapper;
import org.dromara.ecology.mapper.OaFormWorkflowMapper;
import org.dromara.ecology.mapper.OaFormWorkflowOptionMapper;
import org.dromara.ecology.service.IOaBusinessWorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OaBusinessWorkflowServiceImpl implements IOaBusinessWorkflowService {

    private static final String ENABLED = "ENABLED";

    private final OaBusinessTypeMapper businessTypeMapper;
    private final OaBusinessWorkflowBindingMapper bindingMapper;
    private final OaBusinessWorkflowOptionMapper bindingOptionMapper;
    private final OaFormWorkflowMapper formMapper;
    private final OaFormWorkflowOptionMapper optionMapper;

    @Override
    public List<OaBusinessWorkflowBindingVo> queryList() {
        return bindingMapper.selectList(Wrappers.<OaBusinessWorkflowBinding>lambdaQuery()
                .eq(OaBusinessWorkflowBinding::getStatus, ENABLED)
                .orderByAsc(OaBusinessWorkflowBinding::getBusinessType))
            .stream().map(this::toVo).toList();
    }

    @Override
    public OaBusinessWorkflowBindingVo queryByBusinessType(String businessType) {
        OaBusinessWorkflowBinding binding = findBinding(businessType);
        return binding == null ? empty(StringUtils.trim(businessType)) : toVo(binding);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean save(String businessType, OaBusinessWorkflowBindingBo bo) {
        String normalizedType = StringUtils.trim(businessType);
        if (StringUtils.isBlank(normalizedType) || bo == null) throw new ServiceException("业务类型和流程绑定不能为空");
        OaBusinessType business = businessTypeMapper.selectOne(Wrappers.<OaBusinessType>lambdaQuery()
            .eq(OaBusinessType::getBusinessType, normalizedType));
        if (business == null) throw new ServiceException("业务类型不存在");
        OaFormWorkflow form = formMapper.selectOne(Wrappers.<OaFormWorkflow>lambdaQuery()
            .eq(OaFormWorkflow::getId, bo.getFormId()).eq(OaFormWorkflow::getStatus, ENABLED));
        if (form == null) throw new ServiceException("泛微表单不存在或已停用");
        List<Long> optionIds = bo.getOptionIds().stream().distinct().toList();
        List<OaFormWorkflowOption> options = optionMapper.selectList(Wrappers.<OaFormWorkflowOption>lambdaQuery()
            .in(OaFormWorkflowOption::getId, optionIds)
            .eq(OaFormWorkflowOption::getStatus, ENABLED));
        if (options.size() != optionIds.size()) throw new ServiceException("只能选择已启用的通用审批方式");
        if (!optionIds.contains(bo.getDefaultOptionId())) throw new ServiceException("默认审批方式必须包含在已选审批方式中");

        OaBusinessWorkflowBinding binding = findBinding(normalizedType);
        if (binding == null) {
            binding = new OaBusinessWorkflowBinding();
            binding.setBusinessType(normalizedType);
            binding.setFormId(form.getId());
            binding.setDefaultOptionId(bo.getDefaultOptionId());
            binding.setStatus(ENABLED);
            bindingMapper.insert(binding);
        } else {
            binding.setFormId(form.getId());
            binding.setDefaultOptionId(bo.getDefaultOptionId());
            binding.setStatus(ENABLED);
            bindingMapper.updateById(binding);
        }
        bindingOptionMapper.delete(Wrappers.<OaBusinessWorkflowOption>lambdaQuery()
            .eq(OaBusinessWorkflowOption::getBindingId, binding.getId()));
        for (int i = 0; i < optionIds.size(); i++) {
            OaBusinessWorkflowOption item = new OaBusinessWorkflowOption();
            item.setBindingId(binding.getId());
            item.setOptionId(optionIds.get(i));
            item.setSortNo(i);
            bindingOptionMapper.insert(item);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String businessType) {
        OaBusinessWorkflowBinding binding = findBinding(businessType);
        if (binding == null) return true;
        bindingOptionMapper.delete(Wrappers.<OaBusinessWorkflowOption>lambdaQuery()
            .eq(OaBusinessWorkflowOption::getBindingId, binding.getId()));
        return bindingMapper.deleteById(binding.getId()) > 0;
    }

    private OaBusinessWorkflowBinding findBinding(String businessType) {
        return bindingMapper.selectOne(Wrappers.<OaBusinessWorkflowBinding>lambdaQuery()
            .eq(OaBusinessWorkflowBinding::getBusinessType, StringUtils.trim(businessType))
            .eq(OaBusinessWorkflowBinding::getStatus, ENABLED));
    }

    private OaBusinessWorkflowBindingVo empty(String businessType) {
        OaBusinessWorkflowBindingVo vo = new OaBusinessWorkflowBindingVo();
        vo.setBusinessType(businessType);
        vo.setOptionIds(List.of());
        return vo;
    }

    private OaBusinessWorkflowBindingVo toVo(OaBusinessWorkflowBinding binding) {
        OaBusinessWorkflowBindingVo vo = empty(binding.getBusinessType());
        vo.setFormId(binding.getFormId());
        vo.setDefaultOptionId(binding.getDefaultOptionId());
        vo.setOptionIds(bindingOptionMapper.selectList(Wrappers.<OaBusinessWorkflowOption>lambdaQuery()
                .eq(OaBusinessWorkflowOption::getBindingId, binding.getId())
                .orderByAsc(OaBusinessWorkflowOption::getSortNo))
            .stream().map(OaBusinessWorkflowOption::getOptionId).toList());
        return vo;
    }
}
