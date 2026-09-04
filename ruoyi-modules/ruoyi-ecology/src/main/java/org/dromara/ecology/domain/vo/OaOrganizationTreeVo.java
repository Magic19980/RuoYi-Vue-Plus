package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 泛微 HRM 分部与部门组织树节点。 */
@Data
public class OaOrganizationTreeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 节点唯一键，分部和部门使用不同命名空间。 */
    private String nodeKey;
    private String nodeType;
    private String oaId;
    private String oaCode;
    private String name;
    private String shortName;
    private String fullName;
    private String subcompanyId;
    private String parentOaId;
    private String parentNodeKey;
    private String path;
    private Integer level;
    private String status;
    private String treeStatus;
    private BigDecimal showOrder;

    /** 直接投影到本地 sys_dept 的部门信息。 */
    private Long localDeptId;
    private String localDeptName;

    private List<OaOrganizationTreeVo> children = new ArrayList<>();
}
