package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** SCORE 提案分类配置视图。 */
@Data
public class ScoreCategoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String categoryName;
    private Integer categoryLevel;
    private Integer sortNum;
    private String status;
    private String remark;
    private Long proposalCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<ScoreCategoryVo> children = new ArrayList<>();
}
