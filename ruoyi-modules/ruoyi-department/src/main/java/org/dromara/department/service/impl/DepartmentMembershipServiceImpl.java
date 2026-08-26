package org.dromara.department.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.department.api.DepartmentMembershipService;
import org.dromara.department.api.domain.DepartmentMembershipDTO;
import org.dromara.department.mapper.PersonProfileMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 科室人员服务关系查询实现。
 */
@RequiredArgsConstructor
@Service
public class DepartmentMembershipServiceImpl implements DepartmentMembershipService {

    private final PersonProfileMapper personProfileMapper;

    @Override
    public List<DepartmentMembershipDTO> selectActiveMemberships(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return personProfileMapper.selectActiveMembershipsByUserIds(userIds);
    }
}
