package org.dromara.ecology.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaApplication;
import org.dromara.ecology.domain.vo.OaApplicationVo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

/** 泛微通用申请数据层。 */
@Mapper
public interface OaApplicationMapper extends BaseMapperPlus<OaApplication, OaApplicationVo> {

    default OaApplication selectByIdForUpdate(Long id) {
        return selectOne(Wrappers.<OaApplication>lambdaQuery()
            .eq(OaApplication::getId, id)
            .last("limit 1 for update"));
    }
}
