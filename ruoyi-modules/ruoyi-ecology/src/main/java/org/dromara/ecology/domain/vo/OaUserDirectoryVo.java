package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微人员目录项。数据来源为 Ecology HRM REST 接口。 */
@Data
public class OaUserDirectoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String oaUserId;
    private String oaUserName;
    private String oaWorkcode;
    private String oaLoginId;
    private String sex;
    private String mobile;
    private String telephone;
    private String email;
    private String departmentId;
    private String departmentName;
    private String departmentCode;
    private String subCompanyId;
    private String subCompanyName;
    private String jobTitle;
    private String managerId;
    private String assistantId;
    private String accountType;
    private String belongTo;
    private String modified;
    private String status;
}
