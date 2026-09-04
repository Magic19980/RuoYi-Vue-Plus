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

/**
 * SCORE 提案全局分类配置实体，对应 {@code dm_score_category} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_score_category")
public class ScoreCategory extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 分类主键。 */
    private Long id;

    /** 父分类ID，0表示提案大类。 */
    private Long parentId;

    /** 分类名称。 */
    private String categoryName;

    /** 1-大类，2-小类。 */
    private Integer categoryLevel;

    /** 同级分类显示顺序。 */
    private Integer sortNum;

    /** 分类状态。 */
    private String status;

    /** 分类备注。 */
    private String remark;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
