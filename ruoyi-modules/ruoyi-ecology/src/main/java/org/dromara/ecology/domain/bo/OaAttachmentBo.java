package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微申请附件参数。文件先上传到本地 OSS，再由审批中心适配到泛微。 */
@Data
public class OaAttachmentBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "附件 OSS ID 不能为空")
    private Long ossId;

    /** FILE 或 IMAGE，分别映射到泛微 fj/tp 字段。 */
    @Size(max = 20, message = "附件类型不能超过20个字符")
    private String attachmentType = "FILE";

    private Integer sortNo;
}
