package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 人员档案绑定用的系统用户选项。
 */
@Data
public class PersonUserOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long deptId;

    private String userName;

    private String nickName;

    private String deptName;
}
