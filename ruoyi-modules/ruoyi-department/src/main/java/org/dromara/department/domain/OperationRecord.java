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
 * 运维工作记录实体，对应 {@code dm_operation_record} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_operation_record")
public class OperationRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 运维工作记录主键。 */
    private Long id;

    /** 业务科室主键。 */
    private Long deptId;

    /** 绑定的科室项目主数据ID。 */
    private Long projectId;

    /** 请求人员。 */
    private String requestPerson;

    /** 客户单位。 */
    private String customerUnit;

    /** 请求角色类型。 */
    private String requestRoleType;

    /** 请求时间。 */
    private LocalDateTime requestTime;

    /** 处理人员。 */
    private String handler;

    /** 开始处理时间。 */
    private LocalDateTime processTime;

    /** 完成时间。 */
    private LocalDateTime completionTime;

    /** 响应时长，单位为分钟。 */
    private Integer responseMinutes;

    /** 处理时长，单位为分钟。 */
    private Integer processingMinutes;

    /** 是否跨越午休时段。 */
    private String lunchBreak;

    /** 处理状态。 */
    private String processStatus;

    /** 处理方式。 */
    private String processMethod;

    /** 记录提交人。 */
    private String submitter;

    /** 相关系统名称。 */
    private String systemName;

    /** 故障类型。 */
    private String faultType;

    /** 业务描述。 */
    private String businessDescription;

    /** 解决方案。 */
    private String solution;

    /** 记录备注。 */
    private String remark;

    /** 数据来源。 */
    private String sourceType;

    /** 来源文件名称。 */
    private String sourceFileName;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
