package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 平台问题与建议负责人选项。
 */
@Data
public class PlatformFeedbackUserOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String userName;

    private Long deptId;

    private String deptName;
}
