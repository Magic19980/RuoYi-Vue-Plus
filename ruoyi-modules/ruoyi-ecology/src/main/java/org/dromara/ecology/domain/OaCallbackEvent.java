package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/** 泛微回调接收、验签和幂等处理记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_callback_event")
public class OaCallbackEvent extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 泛微事件 ID；没有事件 ID 时使用请求体摘要。 */
    private String eventKey;
    private String oaRequestId;
    private Long processId;
    private String eventStatus;
    private String rawBody;
    private String errorMessage;
    private LocalDateTime processedAt;
}
