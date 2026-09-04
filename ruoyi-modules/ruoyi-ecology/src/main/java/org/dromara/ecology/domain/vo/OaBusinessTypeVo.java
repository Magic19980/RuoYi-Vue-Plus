package org.dromara.ecology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ecology.domain.OaBusinessType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 泛微审批业务类型配置视图。 */
@Data
@AutoMapper(target = OaBusinessType.class)
public class OaBusinessTypeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String businessType;

    private String businessName;
    private String status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
