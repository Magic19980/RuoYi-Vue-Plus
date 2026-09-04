package org.dromara.ecology.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ecology.domain.OaApplicationAttachment;
import org.dromara.ecology.domain.vo.OaAttachmentVo;

import java.util.List;

/** 泛微申请附件数据层。 */
@Mapper
public interface OaApplicationAttachmentMapper extends BaseMapperPlus<OaApplicationAttachment, OaAttachmentVo> {

    default List<OaApplicationAttachment> selectByApplicationId(Long applicationId) {
        return selectList(Wrappers.<OaApplicationAttachment>lambdaQuery()
            .eq(OaApplicationAttachment::getApplicationId, applicationId)
            .orderByAsc(OaApplicationAttachment::getSortNo)
            .orderByAsc(OaApplicationAttachment::getId));
    }

    default int deleteByApplicationId(Long applicationId) {
        return delete(Wrappers.<OaApplicationAttachment>lambdaUpdate()
            .eq(OaApplicationAttachment::getApplicationId, applicationId));
    }
}
