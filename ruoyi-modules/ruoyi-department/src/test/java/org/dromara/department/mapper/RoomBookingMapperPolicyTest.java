package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.dromara.department.domain.bo.RoomBookingQueryBo;
import org.dromara.department.domain.vo.RoomBookingVo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class RoomBookingMapperPolicyTest {

    @Test
    void bookableRoomOptionsExcludeMatchedBlacklistsButViewOptionsRemainSeparate() throws NoSuchMethodException {
        Method bookableMethod = RoomResourceMapper.class.getDeclaredMethod(
            "selectOptions", Long.class, Long.class, boolean.class
        );
        Method viewMethod = RoomResourceMapper.class.getDeclaredMethod(
            "selectViewOptions", Long.class, Long.class, boolean.class, boolean.class
        );
        String bookableSql = String.join(" ", bookableMethod.getAnnotation(Select.class).value());
        String viewSql = String.join(" ", viewMethod.getAnnotation(Select.class).value());

        assertTrue(bookableSql.contains("dm_room_booking_blacklist bl"),
            "可预约房间查询必须检查预约黑名单");
        assertTrue(bookableSql.contains("bl.enabled = 1"),
            "仅启用中的黑名单应阻止预约");
        assertTrue(bookableSql.contains("bl.subject_type = 'ALL'"),
            "可预约房间查询必须支持全公司黑名单");
        assertTrue(bookableSql.contains("sys_user_role ubr"),
            "可预约房间查询必须支持角色黑名单");
        assertTrue(!viewSql.contains("dm_room_booking_blacklist bl"),
            "黑名单只禁止预约，不应从查看房间列表中移除房间");
    }

    @Test
    void approvalOnlyVisibilityIsLimitedToPendingOccurrences() throws NoSuchMethodException {
        for (String methodName : List.of("selectCalendar", "selectPageList")) {
            Method method = "selectCalendar".equals(methodName)
                ? RoomBookingMapper.class.getDeclaredMethod(methodName, RoomBookingQueryBo.class, Long.class, Long.class, boolean.class, boolean.class)
                : RoomBookingMapper.class.getDeclaredMethod(methodName, Page.class, RoomBookingQueryBo.class, Long.class, Long.class, boolean.class, boolean.class);
            Select select = method.getAnnotation(Select.class);
            String sql = String.join(" ", select.value());
            int visibilityStart = sql.indexOf("and (#{canManageAll} or (#{canApprove}");
            int visibilityEnd = sql.indexOf("order by", visibilityStart);

            assertTrue(visibilityStart >= 0 && visibilityEnd > visibilityStart,
                methodName + " 必须存在可见范围查询条件");
            String visibilitySql = sql.substring(visibilityStart, visibilityEnd);
            int approvalGuard = visibilitySql.indexOf("or (o.status = 'PENDING' and (");

            assertTrue(visibilitySql.contains("dm_room_booking_attendee ba0"),
                methodName + " 必须允许预约参与者读取被邀请的预约");
            assertTrue(approvalGuard >= 0, methodName + " 的审批授权必须限制为待审批实例");
            assertTrue(visibilitySql.indexOf("a2.permission_type = 'APPROVE'", approvalGuard) > approvalGuard,
                methodName + " 的审批 ACL 必须位于待审批限制内");
            assertTrue(visibilitySql.indexOf("dm_room_approval_rule ar ", approvalGuard) > approvalGuard,
                methodName + " 的审批链授权必须位于待审批限制内");
        }
    }
}
