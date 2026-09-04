package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaImportDeptAlias;

/** 通用导入部门别名数据层。 */
@Mapper
public interface OaImportDeptAliasMapper extends BaseMapperPlus<OaImportDeptAlias, OaImportDeptAlias> {

    default OaImportDeptAlias selectByNormalizedName(String businessType, String normalizedName) {
        return selectOne(Wrappers.<OaImportDeptAlias>lambdaQuery()
            .eq(OaImportDeptAlias::getBusinessType, businessType)
            .eq(OaImportDeptAlias::getNormalizedName, normalizedName)
            .eq(OaImportDeptAlias::getStatus, "0"));
    }
}
