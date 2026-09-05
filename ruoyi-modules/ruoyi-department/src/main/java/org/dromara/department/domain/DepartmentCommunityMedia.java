package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 协作社区媒体附件实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_department_community_media")
public class DepartmentCommunityMedia extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long postId;

    private Long ossId;

    private String mediaType;

    private String originalName;

    private String fileSuffix;

    private String contentType;

    private Long fileSize;

    private Integer sortNum;

    @TableLogic
    private String delFlag;
}
