package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 运维工作记录对象 dm_operation_record。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_operation_record")
public class OperationRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    /** 绑定的科室项目主数据ID。 */
    private Long projectId;

    private String requestPerson;

    private String customerUnit;

    private String requestRoleType;

    private LocalDateTime requestTime;

    private String handler;

    private LocalDateTime processTime;

    private LocalDateTime completionTime;

    private Integer responseMinutes;

    private Integer processingMinutes;

    private String lunchBreak;

    private String processStatus;

    private String processMethod;

    private String submitter;

    private String systemName;

    private String faultType;

    private String businessDescription;

    private String solution;

    private String remark;

    private String sourceType;

    private String sourceFileName;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
