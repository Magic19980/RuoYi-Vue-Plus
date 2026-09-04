package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 泛微申请与本地 OSS 附件的关系及外部适配结果。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_application_attachment")
public class OaApplicationAttachment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long applicationId;
    private Long processId;
    private Long ossId;
    private String attachmentType;
    private String fileName;
    private String fileUrl;
    private Integer sortNo;
    private String uploadStatus;
    private String oaFileId;
    private String oaFilePath;
    private String failReason;

    @TableLogic
    private String delFlag;
}
