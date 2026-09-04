package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 泛微 HRM 同步明细。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_sync_detail")
public class OaSyncDetail extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long batchId;
    private String entityType;
    private String sourceId;
    private String sourceKey;
    private Long localId;
    private String action;
    private String detailStatus;
    private String message;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
