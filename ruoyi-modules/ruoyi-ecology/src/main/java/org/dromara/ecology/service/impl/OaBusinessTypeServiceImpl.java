package org.dromara.ecology.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.ecology.domain.OaBusinessType;
import org.dromara.ecology.domain.bo.OaBusinessTypeBo;
import org.dromara.ecology.domain.vo.OaBusinessTypeVo;
import org.dromara.ecology.mapper.OaBusinessTypeMapper;
import org.dromara.ecology.service.IOaBusinessTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 泛微审批业务类型配置服务实现。 */
@Service
@RequiredArgsConstructor
public class OaBusinessTypeServiceImpl implements IOaBusinessTypeService {

    private static final String ENABLED = "ENABLED";
    private static final String DISABLED = "DISABLED";

    private final OaBusinessTypeMapper mapper;

    @Override
    public List<OaBusinessTypeVo> queryList(String keyword, boolean enabledOnly) {
        String normalizedKeyword = StringUtils.trim(keyword);
        return mapper.selectVoList(Wrappers.<OaBusinessType>lambdaQuery()
            .eq(enabledOnly, OaBusinessType::getStatus, ENABLED)
            .and(StringUtils.isNotBlank(normalizedKeyword), query -> query
                .like(OaBusinessType::getBusinessType, normalizedKeyword)
                .or().like(OaBusinessType::getBusinessName, normalizedKeyword))
            .orderByAsc(OaBusinessType::getBusinessName)
            .orderByAsc(OaBusinessType::getBusinessType)
            .orderByAsc(OaBusinessType::getId));
    }

    @Override
    public OaBusinessTypeVo queryById(Long id) {
        OaBusinessTypeVo result = mapper.selectVoById(id);
        if (result == null) {
            throw new ServiceException("业务类型不存在");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(OaBusinessTypeBo bo) {
        String businessType = normalizeType(bo);
        if (mapper.selectCount(Wrappers.<OaBusinessType>lambdaQuery()
            .eq(OaBusinessType::getBusinessType, businessType)) > 0) {
            throw new ServiceException("业务类型标识已存在，请直接编辑原配置");
        }
        OaBusinessType entity = new OaBusinessType();
        entity.setBusinessType(businessType);
        copy(bo, entity);
        return mapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(OaBusinessTypeBo bo) {
        if (bo == null || bo.getId() == null) {
            throw new ServiceException("业务类型配置不能为空");
        }
        OaBusinessType entity = mapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("业务类型不存在");
        }
        String businessType = normalizeType(bo);
        if (!StringUtils.equals(StringUtils.trim(entity.getBusinessType()), businessType)) {
            throw new ServiceException("业务类型标识创建后不能修改，请停用旧类型后新增配置");
        }
        copy(bo, entity);
        return mapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disableById(Long id) {
        OaBusinessType entity = mapper.selectById(id);
        if (entity == null) {
            return false;
        }
        entity.setStatus(DISABLED);
        return mapper.updateById(entity) > 0;
    }

    @Override
    public String requireEnabled(String businessType) {
        return requireEnabledConfig(businessType).getBusinessType();
    }

    @Override
    public OaBusinessTypeVo requireEnabledConfig(String businessType) {
        String normalized = StringUtils.trim(businessType);
        if (StringUtils.isBlank(normalized)) {
            throw new ServiceException("请选择业务类型");
        }
        OaBusinessTypeVo result = mapper.selectVoList(Wrappers.<OaBusinessType>lambdaQuery()
            .eq(OaBusinessType::getBusinessType, normalized)
            .eq(OaBusinessType::getStatus, ENABLED))
            .stream().findFirst().orElse(null);
        if (result == null) {
            throw new ServiceException("业务类型不存在或已停用，请先在业务类型配置中维护");
        }
        return result;
    }

    private String normalizeType(OaBusinessTypeBo bo) {
        if (bo == null) {
            throw new ServiceException("业务类型配置不能为空");
        }
        String value = StringUtils.trim(bo.getBusinessType());
        if (StringUtils.isBlank(value)) {
            throw new ServiceException("业务类型标识不能为空");
        }
        return value;
    }

    private void copy(OaBusinessTypeBo bo, OaBusinessType entity) {
        entity.setBusinessName(StringUtils.trim(bo.getBusinessName()));
        entity.setStatus(DISABLED.equalsIgnoreCase(bo.getStatus()) ? DISABLED : ENABLED);
        entity.setRemark(StringUtils.trim(bo.getRemark()));
    }

}
