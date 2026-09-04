package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaApplicationDept;

import java.util.List;

/** OA 申请部门明细数据层。 */
@Mapper
public interface OaApplicationDeptMapper extends BaseMapperPlus<OaApplicationDept, OaApplicationDept> {

    default List<OaApplicationDept> selectByApplicationId(Long applicationId) {
        return selectList(Wrappers.<OaApplicationDept>lambdaQuery()
            .eq(OaApplicationDept::getApplicationId, applicationId)
            .orderByAsc(OaApplicationDept::getSortNo)
            .orderByAsc(OaApplicationDept::getId));
    }

    default void deleteByApplicationId(Long applicationId) {
        delete(Wrappers.<OaApplicationDept>lambdaUpdate()
            .eq(OaApplicationDept::getApplicationId, applicationId));
    }
}
