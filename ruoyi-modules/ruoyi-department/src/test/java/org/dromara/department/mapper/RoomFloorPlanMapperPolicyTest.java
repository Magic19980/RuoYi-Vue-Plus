package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.dromara.department.domain.bo.RoomFloorPlanQueryBo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class RoomFloorPlanMapperPolicyTest {

    @Test
    void ordinaryFloorPlanPageOnlyExposesPublishedPlans() throws NoSuchMethodException {
        Method method = RoomFloorPlanMapper.class.getDeclaredMethod(
            "selectPageList", Page.class, RoomFloorPlanQueryBo.class,
            Long.class, Long.class, boolean.class, boolean.class
        );
        Select select = method.getAnnotation(Select.class);
        String sql = String.join(" ", select.value());
        int ordinaryBranch = sql.indexOf("<otherwise>");

        assertTrue(ordinaryBranch >= 0, "普通用户平面图查询必须存在独立权限分支");
        assertTrue(sql.indexOf("p.status = 'ENABLED'", ordinaryBranch) >= ordinaryBranch,
            "普通用户只能看到已发布平面图");
    }
}
