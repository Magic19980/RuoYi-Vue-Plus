package org.dromara.ecology.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.encrypt.utils.EncryptUtils;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.redis.utils.CacheUtils;
import org.dromara.ecology.client.EcologyClient;
import org.dromara.ecology.config.EcologyProperties;
import org.dromara.ecology.domain.OaSyncBatch;
import org.dromara.ecology.domain.OaSyncDetail;
import org.dromara.ecology.domain.bo.OaHrmUserPasswordBo;
import org.dromara.ecology.domain.vo.OaDepartmentDirectoryVo;
import org.dromara.ecology.domain.vo.OaJobTitleDirectoryVo;
import org.dromara.ecology.domain.vo.OaHrmUserPasswordVo;
import org.dromara.ecology.domain.vo.OaOrganizationTreeVo;
import org.dromara.ecology.domain.vo.OaSubCompanyDirectoryVo;
import org.dromara.ecology.domain.vo.OaSyncBatchVo;
import org.dromara.ecology.domain.vo.OaSyncDetailVo;
import org.dromara.ecology.domain.vo.OaSyncResultVo;
import org.dromara.ecology.domain.vo.OaUserDirectoryVo;
import org.dromara.ecology.mapper.OaSyncBatchMapper;
import org.dromara.ecology.mapper.OaSyncDetailMapper;
import org.dromara.ecology.service.IOaHrmSyncService;
import org.dromara.system.domain.SysDept;
import org.dromara.system.domain.SysPost;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.SysUserPost;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.bo.SysConfigBo;
import org.dromara.system.domain.vo.SysConfigVo;
import org.dromara.system.mapper.SysDeptMapper;
import org.dromara.system.mapper.SysPostMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.SysUserPostMapper;
import org.dromara.system.service.ISysUserService;
import org.dromara.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/** 泛微 HRM 组织与人员同步服务。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OaHrmSyncServiceImpl implements IOaHrmSyncService {

    private static final String ORGANIZATION = "ORGANIZATION";
    private static final String USER = "USER";
    private static final String SUCCESS = "SUCCESS";
    private static final String PENDING = "PENDING";
    private static final String FAILED = "FAILED";
    private static final String CONFLICT = "CONFLICT";
    private static final String HRM_DEFAULT_PASSWORD_HASH_KEY = "ecology.hrm.defaultPasswordHash";
    private static final String HRM_DEFAULT_PASSWORD_ENCRYPTED_KEY = "ecology.hrm.defaultPasswordEncrypted";
    private static final int OA_PHONE_MAX_LENGTH = 64;
    private static final DateTimeFormatter OA_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EcologyClient ecologyClient;
    private final EcologyProperties properties;
    private final OaSyncBatchMapper syncBatchMapper;
    private final OaSyncDetailMapper syncDetailMapper;
    private final SysDeptMapper sysDeptMapper;
    private final SysPostMapper sysPostMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserPostMapper sysUserPostMapper;
    private final ISysUserService sysUserService;
    private final ISysConfigService sysConfigService;

    /** 兼容未配置独立 HRM 加密密钥的旧部署，优先级低于专用密钥和泛微 server-secret。 */
    @Value("${sa-token.jwt-secret-key:}")
    private String jwtSecretKey;

    private final ReentrantLock syncLock = new ReentrantLock();

    @Override
    @Lock4j(name = "ecology:hrm-sync", expire = 7200000, acquireTimeout = 1000)
    public OaSyncResultVo syncOrganization(boolean full) {
        lockSync();
        // 组织树必须拿到完整的上下级关系；即使调用方传入增量，也按全量读取后重建本地树。
        OaSyncBatch batch = startBatch(ORGANIZATION, true);
        try {
            List<OaSubCompanyDirectoryVo> subCompanies = syncSubCompanies(batch);
            List<OaDepartmentDirectoryVo> departments = syncDepartments(batch);
            syncJobTitles(batch);
            syncLocalDepartments(subCompanies, departments);
            finishBatch(batch);
            return toResult(batch);
        } catch (Exception ex) {
            failBatch(batch, ex);
            throw asServiceException("泛微组织同步失败", ex);
        } finally {
            syncLock.unlock();
        }
    }

    @Override
    @Lock4j(name = "ecology:hrm-sync", expire = 7200000, acquireTimeout = 1000)
    public OaSyncResultVo syncUsers(boolean full) {
        lockSync();
        OaSyncBatch batch = startBatch(USER, full);
        try {
            syncUsersPage(batch);
            finishBatch(batch);
            return toResult(batch);
        } catch (Exception ex) {
            failBatch(batch, ex);
            throw asServiceException("泛微人员同步失败", ex);
        } finally {
            syncLock.unlock();
        }
    }

    @Override
    public OaHrmUserPasswordVo queryUserPasswordStatus() {
        OaHrmUserPasswordVo result = new OaHrmUserPasswordVo();
        String storedEncrypted = sysConfigService.selectConfigByKey(HRM_DEFAULT_PASSWORD_ENCRYPTED_KEY);
        if (StringUtils.isNotBlank(storedEncrypted)) {
            result.setConfigured(true);
            result.setSource("PAGE");
            result.setPassword(decryptHrmPassword(storedEncrypted));
        } else if (StringUtils.isNotBlank(sysConfigService.selectConfigByKey(HRM_DEFAULT_PASSWORD_HASH_KEY))) {
            // 兼容旧版 BCrypt 配置。BCrypt 是单向摘要，旧密码无法恢复，重新保存后即可回显。
            result.setConfigured(true);
            result.setSource("PAGE");
        } else if (StringUtils.isNotBlank(properties.getHrmDefaultPassword())) {
            result.setConfigured(true);
            result.setSource("ENV");
            result.setPassword(properties.getHrmDefaultPassword());
        } else {
            result.setConfigured(false);
            result.setSource("NONE");
        }
        return result;
    }

    @Override
    public void updateUserPassword(OaHrmUserPasswordBo bo) {
        String encryptedPassword = EncryptUtils.encryptByAes(bo.getPassword(), resolveHrmPasswordEncryptionKey());
        SysConfigVo existing = queryHrmPasswordConfig();
        SysConfigBo config = new SysConfigBo();
        config.setConfigId(existing == null ? null : existing.getConfigId());
        config.setConfigName("泛微 HRM 人员初始密码");
        config.setConfigKey(HRM_DEFAULT_PASSWORD_ENCRYPTED_KEY);
        config.setConfigValue(encryptedPassword);
        config.setConfigType("N");
        config.setRemark("供泛微 HRM 同步新人员创建系统用户使用，密码以 AES 密文保存，仅授权管理员可回显");
        if (existing == null) {
            sysConfigService.insertConfig(config);
        } else {
            sysConfigService.updateConfig(config);
        }
    }

    @Override
    public List<OaOrganizationTreeVo> queryOrganizationTree(boolean includeDisabled, String subcompanyId) {
        String normalizedSubcompanyId = StringUtils.trim(subcompanyId);
        List<SysDept> localDepartments = DataPermissionHelper.ignore(() -> sysDeptMapper.selectList(
            Wrappers.<SysDept>lambdaQuery()
                .in(SysDept::getOaSourceType, List.of("SUBCOMPANY", "DEPARTMENT"))
                .eq(SysDept::getDelFlag, SystemConstants.NORMAL)
                .eq(!includeDisabled, SysDept::getStatus, SystemConstants.NORMAL)));
        Map<Long, SysDept> departmentsById = localDepartments.stream()
            .filter(dept -> dept.getDeptId() != null)
            .collect(Collectors.toMap(SysDept::getDeptId, dept -> dept, (left, right) -> left));
        Map<Long, OaOrganizationTreeVo> nodesById = new HashMap<>();
        Map<String, OaOrganizationTreeVo> nodes = new LinkedHashMap<>();
        for (SysDept source : localDepartments) {
            boolean subcompany = "SUBCOMPANY".equals(source.getOaSourceType());
            if (StringUtils.isNotBlank(normalizedSubcompanyId)
                && !subcompany && !normalizedSubcompanyId.equals(source.getOaSubcompanyId())) {
                continue;
            }
            if (StringUtils.isNotBlank(normalizedSubcompanyId)
                && subcompany && !normalizedSubcompanyId.equals(source.getOaSourceId())) {
                continue;
            }
            String sourceId = StringUtils.trim(source.getOaSourceId());
            if (StringUtils.isBlank(sourceId)) {
                continue;
            }
            OaOrganizationTreeVo node = new OaOrganizationTreeVo();
            node.setNodeKey(subcompany ? subcompanyNodeKey(sourceId)
                : departmentNodeKey(source.getOaSubcompanyId(), sourceId));
            node.setNodeType(source.getOaSourceType());
            node.setOaId(sourceId);
            node.setName(source.getDeptName());
            node.setShortName(source.getDeptName());
            node.setFullName(source.getDeptName());
            node.setSubcompanyId(subcompany ? sourceId : source.getOaSubcompanyId());
            node.setStatus(SystemConstants.NORMAL.equals(source.getStatus()) ? "ENABLED" : "DISABLED");
            node.setTreeStatus("VALID");
            node.setShowOrder(source.getOrderNum() == null ? null : BigDecimal.valueOf(source.getOrderNum()));
            node.setLocalDeptId(source.getDeptId());
            node.setLocalDeptName(source.getDeptName());
            nodes.put(node.getNodeKey(), node);
            nodesById.put(source.getDeptId(), node);
        }
        for (SysDept source : localDepartments) {
            OaOrganizationTreeVo node = nodesById.get(source.getDeptId());
            if (node == null) {
                continue;
            }
            SysDept parent = departmentsById.get(source.getParentId());
            OaOrganizationTreeVo parentNode = parent == null ? null : nodesById.get(parent.getDeptId());
            node.setParentNodeKey(parentNode == null ? null : parentNode.getNodeKey());
            node.setParentOaId(parentNode == null ? null : parentNode.getOaId());
        }

        validateTreeLinks(nodes);
        List<OaOrganizationTreeVo> roots = new ArrayList<>();
        for (OaOrganizationTreeVo node : nodes.values()) {
            node.setChildren(new ArrayList<>());
        }
        for (OaOrganizationTreeVo node : nodes.values()) {
            OaOrganizationTreeVo parent = StringUtils.isBlank(node.getParentNodeKey())
                ? null : nodes.get(node.getParentNodeKey());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        sortTree(roots);
        populateTreePath(roots, null, 0);
        return roots;
    }

    private void validateTreeLinks(Map<String, OaOrganizationTreeVo> nodes) {
        for (OaOrganizationTreeVo node : nodes.values()) {
            String parentKey = node.getParentNodeKey();
            if (StringUtils.isBlank(parentKey)) {
                continue;
            }
            if (parentKey.equals(node.getNodeKey()) || !nodes.containsKey(parentKey)) {
                node.setTreeStatus(parentKey.equals(node.getNodeKey()) ? "CYCLE" : "ORPHAN");
                node.setParentNodeKey(null);
            }
        }
        for (OaOrganizationTreeVo node : nodes.values()) {
            Set<String> visited = new HashSet<>();
            OaOrganizationTreeVo current = node;
            while (current != null && StringUtils.isNotBlank(current.getParentNodeKey())) {
                if (!visited.add(current.getNodeKey())) {
                    node.setTreeStatus("CYCLE");
                    node.setParentNodeKey(null);
                    break;
                }
                current = nodes.get(current.getParentNodeKey());
            }
        }
    }

    private void sortTree(List<OaOrganizationTreeVo> nodes) {
        nodes.sort(this::compareTreeNodes);
        for (OaOrganizationTreeVo node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private int compareTreeNodes(OaOrganizationTreeVo left, OaOrganizationTreeVo right) {
        int result = compareOrder(left.getShowOrder(), right.getShowOrder());
        if (result != 0) {
            return result;
        }
        String leftName = left.getName() == null ? "" : left.getName();
        String rightName = right.getName() == null ? "" : right.getName();
        result = leftName.compareToIgnoreCase(rightName);
        return result != 0 ? result : left.getNodeKey().compareTo(right.getNodeKey());
    }

    private int compareOrder(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }

    private void populateTreePath(List<OaOrganizationTreeVo> nodes, String parentPath, int level) {
        for (OaOrganizationTreeVo node : nodes) {
            String path = StringUtils.isBlank(parentPath) ? node.getName() : parentPath + " / " + node.getName();
            node.setLevel(level);
            node.setPath(path);
            populateTreePath(node.getChildren(), path, level + 1);
        }
    }

    private String subcompanyNodeKey(String oaId) {
        return "SUBCOMPANY:" + StringUtils.trim(oaId);
    }

    private String departmentNodeKey(String subcompanyId, String departmentId) {
        return "DEPARTMENT:" + StringUtils.trim(subcompanyId) + ":" + StringUtils.trim(departmentId);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return StringUtils.trim(value);
            }
        }
        return null;
    }

    private boolean isRootId(String value) {
        return StringUtils.isBlank(value) || "0".equals(StringUtils.trim(value));
    }

    private boolean isCanceled(String value) {
        return "1".equals(StringUtils.trim(value));
    }

    @Override
    public PageResult<OaSyncBatchVo> queryBatches(String syncType, PageQuery pageQuery) {
        Page<OaSyncBatchVo> page = syncBatchMapper.selectVoPage(
            pageQuery == null ? new Page<>(1, 20) : pageQuery.build(),
            Wrappers.<OaSyncBatch>lambdaQuery()
                .eq(StringUtils.isNotBlank(syncType), OaSyncBatch::getSyncType, syncType)
                .orderByDesc(OaSyncBatch::getStartedAt));
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public PageResult<OaSyncDetailVo> queryDetails(Long batchId, String detailStatus, PageQuery pageQuery) {
        Page<OaSyncDetailVo> page = syncDetailMapper.selectVoPage(
            pageQuery == null ? new Page<>(1, 20) : pageQuery.build(),
            Wrappers.<OaSyncDetail>lambdaQuery()
                .eq(batchId != null, OaSyncDetail::getBatchId, batchId)
                .eq(StringUtils.isNotBlank(detailStatus), OaSyncDetail::getDetailStatus, detailStatus)
                .orderByDesc(OaSyncDetail::getCreateTime)
                .orderByDesc(OaSyncDetail::getId));
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    private List<OaSubCompanyDirectoryVo> syncSubCompanies(OaSyncBatch batch) {
        List<OaSubCompanyDirectoryVo> validRows = new ArrayList<>();
        int page = 1;
        long fetched = 0;
        int pageSize = syncPageSize();
        while (true) {
            PageResult<OaSubCompanyDirectoryVo> result = ecologyClient.queryOaSubCompanyList(
                page, pageSize, batch.getWatermark());
            Collection<OaSubCompanyDirectoryVo> rows = result.getRows();
            if (rows == null || rows.isEmpty()) {
                break;
            }
            for (OaSubCompanyDirectoryVo source : rows) {
                increment(batch, "totalCount");
                try {
                    if (StringUtils.isBlank(source.getOaId())) {
                        throw new ServiceException("泛微分部 ID 为空");
                    }
                    increment(batch, "successCount");
                    validRows.add(source);
                } catch (Exception ex) {
                    increment(batch, "failedCount");
                    writeDetail(batch, "SUBCOMPANY", source.getOaId(), source.getOaId(), null,
                        "UPDATE", FAILED, message(ex));
                }
            }
            fetched += rows.size();
            if (!hasNext(result.getTotal(), fetched, rows.size(), pageSize)) {
                break;
            }
            throttleBeforeNextPage();
            page++;
        }
        return validRows;
    }

    private List<OaDepartmentDirectoryVo> syncDepartments(OaSyncBatch batch) {
        List<OaDepartmentDirectoryVo> validRows = new ArrayList<>();
        int page = 1;
        long fetched = 0;
        int pageSize = syncPageSize();
        while (true) {
            PageResult<OaDepartmentDirectoryVo> result = ecologyClient.queryOaDepartmentList(
                page, pageSize, batch.getWatermark());
            Collection<OaDepartmentDirectoryVo> rows = result.getRows();
            if (rows == null || rows.isEmpty()) {
                break;
            }
            for (OaDepartmentDirectoryVo source : rows) {
                increment(batch, "totalCount");
                try {
                    if (StringUtils.isBlank(source.getOaId())) {
                        throw new ServiceException("泛微部门 ID 为空");
                    }
                    validRows.add(source);
                    increment(batch, "successCount");
                } catch (Exception ex) {
                    increment(batch, "failedCount");
                    writeDetail(batch, "DEPARTMENT", source.getOaId(),
                        departmentKey(source.getOaSubCompanyId(), source.getOaId()), null,
                        "UPDATE", FAILED, message(ex));
                }
            }
            fetched += rows.size();
            if (!hasNext(result.getTotal(), fetched, rows.size(), pageSize)) {
                break;
            }
            throttleBeforeNextPage();
            page++;
        }
        return validRows;
    }

    /** 将本次完整的泛微组织响应直接投影到 sys_dept，并重建本地层级。 */
    private void syncLocalDepartments(List<OaSubCompanyDirectoryVo> subCompanies,
                                      List<OaDepartmentDirectoryVo> departments) {
        if (subCompanies.isEmpty() && departments.isEmpty()) {
            throw new ServiceException("泛微组织同步未返回任何分部或部门，已停止本地部门接管，请检查泛微接口配置");
        }

        List<SysDept> existingDepartments = DataPermissionHelper.ignore(() -> sysDeptMapper.selectList(
            Wrappers.<SysDept>lambdaQuery().eq(SysDept::getDelFlag, SystemConstants.NORMAL)));
        Map<String, SysDept> localBySource = new HashMap<>();
        for (SysDept local : existingDepartments) {
            String sourceKey = localSourceKey(local.getOaSourceType(), local.getOaSourceId(), local.getOaSubcompanyId());
            if (sourceKey != null) {
                localBySource.put(sourceKey, local);
            }
        }

        Map<String, SysDept> projections = new LinkedHashMap<>();
        Map<String, String> parentSourceKeys = new HashMap<>();
        Set<String> sourceKeys = new HashSet<>();

        for (OaSubCompanyDirectoryVo source : subCompanies) {
            String sourceId = StringUtils.trim(source.getOaId());
            if (StringUtils.isBlank(sourceId)) {
                continue;
            }
            String sourceKey = localSourceKey("SUBCOMPANY", sourceId, null);
            SysDept local = localBySource.get(sourceKey);
            if (local == null) {
                local = new SysDept();
                local.setDeptId(IdGeneratorUtil.nextLongId());
            }
            local.setOaSourceType("SUBCOMPANY");
            local.setOaSourceId(sourceId);
            local.setOaSubcompanyId(null);
            local.setDeptName(firstNonBlank(source.getOaName(), source.getOaFullName(), sourceId));
            local.setStatus(isCanceled(source.getCanceled()) ? SystemConstants.DISABLE : SystemConstants.NORMAL);
            local.setOrderNum(toOrderNum(decimal(source.getShowOrder())));
            local.setDelFlag(SystemConstants.NORMAL);
            projections.put(sourceKey, local);
            sourceKeys.add(sourceKey);
            parentSourceKeys.put(sourceKey, isRootId(source.getOaParentId())
                ? null : localSourceKey("SUBCOMPANY", source.getOaParentId(), null));
        }

        for (OaDepartmentDirectoryVo source : departments) {
            String sourceId = StringUtils.trim(source.getOaId());
            if (StringUtils.isBlank(sourceId)) {
                continue;
            }
            String subcompanyId = normalizeSubcompanyId(source.getOaSubCompanyId());
            String sourceKey = localSourceKey("DEPARTMENT", sourceId, subcompanyId);
            SysDept local = localBySource.get(sourceKey);
            if (local == null) {
                local = new SysDept();
                local.setDeptId(IdGeneratorUtil.nextLongId());
            }
            local.setOaSourceType("DEPARTMENT");
            local.setOaSourceId(sourceId);
            local.setOaSubcompanyId(subcompanyId);
            local.setDeptName(firstNonBlank(source.getOaName(), source.getOaMark(), sourceId));
            local.setStatus(isCanceled(source.getCanceled()) ? SystemConstants.DISABLE : SystemConstants.NORMAL);
            local.setOrderNum(toOrderNum(decimal(source.getShowOrder())));
            local.setDelFlag(SystemConstants.NORMAL);
            projections.put(sourceKey, local);
            sourceKeys.add(sourceKey);
            if (isRootId(source.getOaParentId())) {
                parentSourceKeys.put(sourceKey, "0".equals(subcompanyId)
                    ? null : localSourceKey("SUBCOMPANY", subcompanyId, null));
            } else {
                parentSourceKeys.put(sourceKey,
                    localSourceKey("DEPARTMENT", source.getOaParentId(), subcompanyId));
            }
        }

        if (projections.isEmpty()) {
            throw new ServiceException("泛微组织响应中没有有效的组织节点，已停止本地部门接管");
        }

        Map<String, Long> sourceToLocalId = projections.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getDeptId(), (left, right) -> left));
        Map<String, String> normalizedParentKeys = new HashMap<>();
        for (String sourceKey : sourceKeys) {
            String parentKey = parentSourceKeys.get(sourceKey);
            normalizedParentKeys.put(sourceKey,
                StringUtils.isBlank(parentKey) || hasSourceCycle(sourceKey, parentSourceKeys)
                    ? null : parentKey);
        }

        Map<Long, SysDept> allById = new HashMap<>();
        for (SysDept local : existingDepartments) {
            allById.put(local.getDeptId(), local);
        }
        for (SysDept local : projections.values()) {
            allById.put(local.getDeptId(), local);
        }

        Map<Long, String> ancestorsCache = new HashMap<>();
        Set<Long> existingIds = existingDepartments.stream()
            .map(SysDept::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet());
        for (Map.Entry<String, SysDept> entry : projections.entrySet()) {
            SysDept local = entry.getValue();
            Long parentId = sourceToLocalId.get(normalizedParentKeys.get(entry.getKey()));
            // 泛微顶级分部/部门直接作为本地顶级节点，避免依赖旧系统的示例根部门。
            local.setParentId(parentId == null ? 0L : parentId);
            local.setAncestors(resolveAncestors(local.getDeptId(), allById, ancestorsCache, new HashSet<>()));
            DataPermissionHelper.ignore(() -> {
                if (existingIds.contains(local.getDeptId())) {
                    sysDeptMapper.updateById(local);
                } else {
                    sysDeptMapper.insert(local);
                }
            });
        }

        // 全量同步后，当前响应之外的部门（包括历史手工维护部门）不再作为主数据使用。
        // 通过逻辑删除保留历史引用，避免业务表中的 dept_id 变成物理悬空数据。
        Set<Long> projectedDepartmentIds = new HashSet<>(sourceToLocalId.values());
        List<Long> staleDepartmentIds = existingDepartments.stream()
            .map(SysDept::getDeptId)
            .filter(Objects::nonNull)
            .filter(deptId -> !projectedDepartmentIds.contains(deptId))
            .toList();
        if (!staleDepartmentIds.isEmpty()) {
            DataPermissionHelper.ignore(() -> sysDeptMapper.deleteByIds(staleDepartmentIds));
            log.info("已逻辑删除不在泛微组织响应中的本地部门，count={}", staleDepartmentIds.size());
        }

        evictDepartmentCaches(allById.keySet());
        log.info("泛微组织已接管本地部门，subCompanies={}, departments={}",
            subCompanies.size(), departments.size());
    }

    private String resolveAncestors(Long deptId, Map<Long, SysDept> allById,
                                    Map<Long, String> cache, Set<Long> visiting) {
        if (Objects.equals(deptId, SystemConstants.DEFAULT_DEPT_ID)) {
            return SystemConstants.ROOT_DEPT_ANCESTORS;
        }
        String cached = cache.get(deptId);
        if (cached != null) {
            return cached;
        }
        if (deptId == null || !visiting.add(deptId)) {
            return SystemConstants.ROOT_DEPT_ANCESTORS;
        }
        SysDept current = allById.get(deptId);
        SysDept parent = current == null ? null : allById.get(current.getParentId());
        String ancestors;
        if (parent == null) {
            ancestors = SystemConstants.ROOT_DEPT_ANCESTORS;
        } else {
            ancestors = resolveAncestors(parent.getDeptId(), allById, cache, visiting)
                + StringUtils.SEPARATOR + parent.getDeptId();
        }
        visiting.remove(deptId);
        cache.put(deptId, ancestors);
        return ancestors;
    }

    private boolean hasSourceCycle(String sourceKey, Map<String, String> parentSourceKeys) {
        Set<String> visited = new HashSet<>();
        String current = sourceKey;
        while (StringUtils.isNotBlank(current)) {
            if (!visited.add(current)) {
                return true;
            }
            current = parentSourceKeys.get(current);
            if (StringUtils.isNotBlank(current) && !parentSourceKeys.containsKey(current)) {
                return false;
            }
        }
        return false;
    }

    private String localSourceKey(String sourceType, String sourceId, String subcompanyId) {
        if (StringUtils.isBlank(sourceType) || StringUtils.isBlank(sourceId)) {
            return null;
        }
        String normalizedSourceId = StringUtils.trim(sourceId);
        if ("DEPARTMENT".equals(sourceType)) {
            return sourceType + ":" + normalizeSubcompanyId(subcompanyId) + ":" + normalizedSourceId;
        }
        return sourceType + ":" + normalizedSourceId;
    }

    private String normalizeSubcompanyId(String subcompanyId) {
        return StringUtils.isBlank(subcompanyId) ? "0" : StringUtils.trim(subcompanyId);
    }

    private int toOrderNum(BigDecimal order) {
        if (order == null) {
            return 0;
        }
        long value = order.longValue();
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(value, Integer.MIN_VALUE);
    }

    private void evictDepartmentCaches(Collection<Long> deptIds) {
        for (Long deptId : deptIds) {
            if (deptId != null) {
                CacheUtils.evict(CacheNames.SYS_DEPT, deptId);
            }
        }
        CacheUtils.clear(CacheNames.SYS_DEPT_AND_CHILD);
    }

    private void syncJobTitles(OaSyncBatch batch) {
        Set<String> sourceIds = new HashSet<>();
        int page = 1;
        long fetched = 0;
        int pageSize = syncPageSize();
        while (true) {
            PageResult<OaJobTitleDirectoryVo> result = ecologyClient.queryOaJobTitleList(
                page, pageSize, batch.getWatermark());
            Collection<OaJobTitleDirectoryVo> rows = result.getRows();
            if (rows == null || rows.isEmpty()) {
                break;
            }
            for (OaJobTitleDirectoryVo source : rows) {
                increment(batch, "totalCount");
                try {
                    Action action = saveJobTitle(source);
                    sourceIds.add(StringUtils.trim(source.getOaId()));
                    increment(batch, "successCount");
                    incrementAction(batch, action);
                } catch (Exception ex) {
                    increment(batch, "failedCount");
                    writeDetail(batch, "JOBTITLE", source.getOaId(), source.getOaId(), null,
                        "UPDATE", FAILED, message(ex));
                }
            }
            fetched += rows.size();
            if (!hasNext(result.getTotal(), fetched, rows.size(), pageSize)) {
                break;
            }
            throttleBeforeNextPage();
            page++;
        }
        if ("FULL".equals(batch.getSyncMode()) && value(batch.getFailedCount()) == 0
            && !sourceIds.isEmpty()) {
            disableMissingOaPosts(sourceIds, batch);
            cleanupLegacyPosts();
        }
    }

    private void syncUsersPage(OaSyncBatch batch) {
        Map<String, Long> departmentMappings = loadDepartmentMappings();
        Map<String, Long> postMappings = loadOaPostMappings();
        Set<String> sourceIds = new HashSet<>();

        int page = 1;
        long fetched = 0;
        int pageSize = syncPageSize();
        while (true) {
            PageResult<OaUserDirectoryVo> result = ecologyClient.queryOaUserList(
                null, page, pageSize, batch.getWatermark());
            Collection<OaUserDirectoryVo> rows = result.getRows();
            if (rows == null || rows.isEmpty()) {
                break;
            }
            for (OaUserDirectoryVo source : rows) {
                increment(batch, "totalCount");
                if (StringUtils.isNotBlank(source.getOaUserId())) {
                    sourceIds.add(StringUtils.trim(source.getOaUserId()));
                }
                SyncOutcome outcome = syncOneUser(source, departmentMappings, postMappings, batch);
                applyOutcome(batch, outcome);
            }
            fetched += rows.size();
            if (!hasNext(result.getTotal(), fetched, rows.size(), pageSize)) {
                break;
            }
            throttleBeforeNextPage();
            page++;
        }
        if ("FULL".equals(batch.getSyncMode()) && value(batch.getFailedCount()) == 0
            && value(batch.getPendingCount()) == 0 && !sourceIds.isEmpty()) {
            disableMissingOaUsers(sourceIds, batch);
            cleanupLegacyUsers(batch);
        }
    }

    /** 分页请求之间主动留出间隔，降低泛微接口瞬时并发和数据库查询峰值。 */
    private void throttleBeforeNextPage() {
        long interval = Math.max(0L, Math.min(properties.getHrmSyncPageIntervalMillis(), 60000L));
        if (interval == 0L) {
            return;
        }
        try {
            Thread.sleep(interval);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new ServiceException("泛微 HRM 同步被中断");
        }
    }

    private Map<String, Long> loadDepartmentMappings() {
        List<SysDept> departments = DataPermissionHelper.ignore(() -> sysDeptMapper.selectList(
            Wrappers.<SysDept>lambdaQuery()
                .eq(SysDept::getOaSourceType, "DEPARTMENT")
                .eq(SysDept::getStatus, SystemConstants.NORMAL)
                .eq(SysDept::getDelFlag, SystemConstants.NORMAL)
                .isNotNull(SysDept::getOaSourceId)));
        return departments
            .stream()
            .collect(Collectors.toMap(department -> departmentKey(department.getOaSubcompanyId(), department.getOaSourceId()),
                SysDept::getDeptId, (left, right) -> left));
    }

    private Map<String, Long> loadOaPostMappings() {
        List<SysPost> posts = DataPermissionHelper.ignore(() -> sysPostMapper.selectList(
            Wrappers.<SysPost>lambdaQuery()
                .eq(SysPost::getOaSourceType, "JOBTITLE")
                .eq(SysPost::getStatus, SystemConstants.NORMAL)
                .eq(SysPost::getDelFlag, SystemConstants.NORMAL)
                .isNotNull(SysPost::getOaSourceId)));
        return posts.stream()
            .filter(post -> StringUtils.isNotBlank(post.getOaSourceId()) && post.getPostId() != null)
            .collect(Collectors.toMap(post -> StringUtils.trim(post.getOaSourceId()), SysPost::getPostId,
                (left, right) -> left));
    }

    private SyncOutcome syncOneUser(OaUserDirectoryVo source, Map<String, Long> departmentMappings,
                                    Map<String, Long> postMappings, OaSyncBatch batch) {
        String sourceKey = departmentKey(source.getSubCompanyId(), source.getDepartmentId());
        if (StringUtils.isBlank(source.getOaUserId())) {
            return pending(batch, source, sourceKey, "泛微人员 ID 为空");
        }
        if (StringUtils.isBlank(source.getOaWorkcode())) {
            return pending(batch, source, sourceKey, "泛微人员工号为空");
        }
        if (properties.isHrmSkipSecondaryAccounts() && "1".equals(StringUtils.trim(source.getAccountType()))) {
            return pending(batch, source, sourceKey, "泛微次账号已按配置跳过");
        }
        String oaStatus = StringUtils.trim(source.getStatus());
        if (StringUtils.isNotBlank(oaStatus) && !isKnownOaStatus(oaStatus)) {
            return pending(batch, source, sourceKey, "未知泛微人员状态：" + oaStatus);
        }
        Long localDeptId = departmentMappings.get(sourceKey);
        if (localDeptId == null) {
            return pending(batch, source, sourceKey, "泛微部门未完成本地映射：" + sourceKey);
        }

        UserResolution resolution = resolveUser(source);
        if (resolution.conflictMessage() != null) {
            writeDetail(batch, "USER", source.getOaUserId(), sourceKey, null,
                "SKIP", CONFLICT, resolution.conflictMessage());
            return SyncOutcome.conflict(resolution.conflictMessage());
        }

        SysUser user = resolution.user();
        String action;
        try {
            if (user == null) {
                user = createLocalUser(source, localDeptId, postMappings);
                action = "CREATE";
            } else {
                if (user.isSuperAdmin()) {
                    return pending(batch, source, sourceKey, "不允许通过 HRM 同步修改超级管理员");
                }
                updateLocalUser(user, source, localDeptId, postMappings);
                action = isInactiveOaStatus(oaStatus) ? "DISABLE" : "UPDATE";
            }
            return SyncOutcome.success(action, user.getUserId());
        } catch (Exception ex) {
            writeDetail(batch, "USER", source.getOaUserId(), sourceKey,
                user == null ? null : user.getUserId(), "UPDATE", FAILED, message(ex));
            return SyncOutcome.failed(message(ex));
        }
    }

    private UserResolution resolveUser(OaUserDirectoryVo source) {
        List<SysUser> byOaUserId = DataPermissionHelper.ignore(() -> sysUserMapper.selectList(Wrappers.<SysUser>lambdaQuery()
            .eq(SysUser::getOaSourceType, "USER")
            .eq(SysUser::getOaSourceId, source.getOaUserId())));
        List<SysUser> byEmployeeNo = DataPermissionHelper.ignore(() -> sysUserMapper.selectList(
            Wrappers.<SysUser>lambdaQuery().eq(SysUser::getEmployeeNo, source.getOaWorkcode())));

        Set<Long> userIds = new HashSet<>();
        byOaUserId.stream().map(SysUser::getUserId).filter(Objects::nonNull).forEach(userIds::add);
        byEmployeeNo.stream().map(SysUser::getUserId).filter(java.util.Objects::nonNull).forEach(userIds::add);
        if (userIds.size() > 1) {
            return UserResolution.conflict("泛微人员 ID、工号分别匹配到了多个本地用户");
        }
        if (byEmployeeNo.size() > 1) {
            return UserResolution.conflict("本地存在重复工号：" + source.getOaWorkcode());
        }
        Long userId = userIds.stream().findFirst().orElse(null);
        if (userId == null) {
            return UserResolution.empty();
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return UserResolution.conflict("泛微源 ID 指向的本地用户不存在：" + userId);
        }
        if (StringUtils.isNotBlank(user.getOaSourceId())
            && !source.getOaUserId().equals(user.getOaSourceId())) {
            return UserResolution.conflict("本地用户已经归属其他泛微人员：" + user.getOaSourceId());
        }
        return UserResolution.of(user);
    }

    private SysUser createLocalUser(OaUserDirectoryVo source, Long localDeptId, Map<String, Long> postMappings) {
        String userName = StringUtils.trim(source.getOaWorkcode());
        String nickName = StringUtils.trim(source.getOaUserName());
        if (userName.length() > 30) {
            throw new ServiceException("泛微工号超过本地账号长度限制：" + userName);
        }
        if (StringUtils.isBlank(nickName)) {
            throw new ServiceException("泛微姓名为空，不能创建本地用户");
        }
        SysUserBo bo = buildUserBo(null, userName, nickName, source, localDeptId, postMappings);
        bo.setPassword(resolveHrmDefaultPasswordHash());
        boolean unique = DataPermissionHelper.ignore(() -> sysUserService.checkUserNameUnique(bo));
        if (!unique) {
            throw new ServiceException("本地登录账号已存在：" + userName);
        }
        DataPermissionHelper.ignore(() -> sysUserService.insertUser(bo));
        SysUser user = sysUserMapper.selectById(bo.getUserId());
        if (user == null) {
            throw new ServiceException("创建本地用户后无法读取用户记录");
        }
        return user;
    }

    /**
     * 获取泛微新人员的初始密码密文。
     *
     * <p>页面配置保存的已经是 BCrypt 密文，可以直接写入新用户；环境变量保留为部署级备用配置。</p>
     */
    private String resolveHrmDefaultPasswordHash() {
        String storedEncrypted = sysConfigService.selectConfigByKey(HRM_DEFAULT_PASSWORD_ENCRYPTED_KEY);
        if (StringUtils.isNotBlank(storedEncrypted)) {
            return BCrypt.hashpw(decryptHrmPassword(storedEncrypted));
        }
        String storedHash = sysConfigService.selectConfigByKey(HRM_DEFAULT_PASSWORD_HASH_KEY);
        if (StringUtils.isNotBlank(storedHash)) {
            return storedHash;
        }
        String environmentPassword = properties.getHrmDefaultPassword();
        if (StringUtils.isBlank(environmentPassword)) {
            throw new ServiceException("未配置人员初始密码，请在泛微 HRM 同步页面设置");
        }
        return BCrypt.hashpw(environmentPassword);
    }

    /** 解密页面保存的泛微人员初始密码，仅在授权接口和新用户创建流程中使用。 */
    private String decryptHrmPassword(String encryptedPassword) {
        try {
            return EncryptUtils.decryptByAes(encryptedPassword, resolveHrmPasswordEncryptionKey());
        } catch (Exception ex) {
            log.error("泛微 HRM 初始密码解密失败，请检查 ECOLOGY_HRM_PASSWORD_ENCRYPTION_KEY 配置", ex);
            throw new ServiceException("人员初始密码无法解密，请检查加密密钥配置");
        }
    }

    /** 生成 AES 所需的固定长度密钥，不在数据库中保存原始密钥。 */
    private String resolveHrmPasswordEncryptionKey() {
        String rawKey = properties.getHrmPasswordEncryptionKey();
        if (StringUtils.isBlank(rawKey)) {
            rawKey = properties.getServerSecret();
        }
        if (StringUtils.isBlank(rawKey)) {
            rawKey = jwtSecretKey;
        }
        if (StringUtils.isBlank(rawKey)) {
            throw new ServiceException("未配置人员初始密码加密密钥，请设置 ECOLOGY_HRM_PASSWORD_ENCRYPTION_KEY");
        }
        return DigestUtil.sha256Hex(rawKey).substring(0, 32);
    }

    /** 查询页面保存的泛微人员初始密码配置，用于更新现有配置记录。 */
    private SysConfigVo queryHrmPasswordConfig() {
        SysConfigBo query = new SysConfigBo();
        query.setConfigKey(HRM_DEFAULT_PASSWORD_ENCRYPTED_KEY);
        SysConfigVo encryptedConfig = sysConfigService.selectConfigList(query).stream()
            .filter(item -> HRM_DEFAULT_PASSWORD_ENCRYPTED_KEY.equals(item.getConfigKey()))
            .findFirst()
            .orElse(null);
        if (encryptedConfig != null) {
            return encryptedConfig;
        }
        query.setConfigKey(HRM_DEFAULT_PASSWORD_HASH_KEY);
        return sysConfigService.selectConfigList(query).stream()
            .filter(item -> HRM_DEFAULT_PASSWORD_HASH_KEY.equals(item.getConfigKey()))
            .findFirst()
            .orElse(null);
    }

    private void updateLocalUser(SysUser user, OaUserDirectoryVo source, Long localDeptId,
                                 Map<String, Long> postMappings) {
        String nickName = StringUtils.isBlank(source.getOaUserName()) ? user.getNickName() : source.getOaUserName();
        SysUserBo bo = buildUserBo(user.getUserId(), user.getUserName(), nickName, source, localDeptId, postMappings);
        bo.setEmployeeNo(StringUtils.isBlank(source.getOaWorkcode()) ? user.getEmployeeNo() : source.getOaWorkcode());
        bo.setEmail(StringUtils.isBlank(source.getEmail()) ? user.getEmail() : source.getEmail());
        String phoneNumber = resolvePhoneNumber(source);
        bo.setPhoneNumber(StringUtils.isBlank(phoneNumber) ? user.getPhoneNumber() : phoneNumber);
        bo.setGender(StringUtils.isBlank(source.getSex()) ? user.getGender() : toGender(source.getSex()));
        String localStatus = toLocalStatus(source.getStatus());
        bo.setStatus(localStatus == null ? user.getStatus() : localStatus);
        DataPermissionHelper.ignore(() -> sysUserService.updateUser(bo));
    }

    private SysUserBo buildUserBo(Long userId, String userName, String nickName,
                                  OaUserDirectoryVo source, Long localDeptId,
                                  Map<String, Long> postMappings) {
        SysUserBo bo = new SysUserBo();
        bo.setUserId(userId);
        bo.setUserName(userName);
        bo.setNickName(nickName);
        bo.setEmployeeNo(source.getOaWorkcode());
        bo.setDeptId(localDeptId);
        bo.setEmail(source.getEmail());
        bo.setPhoneNumber(resolvePhoneNumber(source));
        bo.setGender(toGender(source.getSex()));
        bo.setStatus(toLocalStatus(source.getStatus()));
        bo.setOaSourceType("USER");
        bo.setOaSourceId(source.getOaUserId());
        Long postId = StringUtils.isBlank(source.getJobTitle()) ? null
            : postMappings.get(StringUtils.trim(source.getJobTitle()));
        if (postId != null) {
            bo.setPostIds(new Long[]{postId});
        }
        return bo;
    }

    /**
     * 泛微手机号可能包含国家码、分机号或座机号码，不能按 11 位手机号截断。
     * 仅在手机号为空时回退到电话字段；超出本地字段上限时让当前人员同步失败，保留完整源数据并给出可定位的错误。
     */
    private String resolvePhoneNumber(OaUserDirectoryVo source) {
        String phoneNumber = firstNonBlank(source.getMobile(), source.getTelephone());
        if (StringUtils.isNotBlank(phoneNumber)) {
            int length = phoneNumber.codePointCount(0, phoneNumber.length());
            if (length > OA_PHONE_MAX_LENGTH) {
                throw new ServiceException("泛微人员手机号/电话超过本地字段长度限制（最多"
                    + OA_PHONE_MAX_LENGTH + "个字符，实际" + length + "个字符）");
            }
        }
        return phoneNumber;
    }

    private Action saveJobTitle(OaJobTitleDirectoryVo source) {
        if (StringUtils.isBlank(source.getOaId())) {
            throw new ServiceException("泛微岗位 ID 为空");
        }
        String sourceId = StringUtils.trim(source.getOaId());
        if (sourceId.length() > 64) {
            throw new ServiceException("泛微岗位 ID 超过本地字段长度限制：" + sourceId);
        }
        SysPost entity = DataPermissionHelper.ignore(() -> sysPostMapper.selectOne(
            Wrappers.<SysPost>lambdaQuery()
                .eq(SysPost::getOaSourceType, "JOBTITLE")
                .eq(SysPost::getOaSourceId, sourceId)
                .last("limit 1")));
        Action action = entity == null ? Action.CREATE : Action.UPDATE;
        if (entity == null) {
            entity = new SysPost();
            entity.setPostId(IdGeneratorUtil.nextLongId());
        }
        entity.setDeptId(SystemConstants.DEFAULT_DEPT_ID);
        entity.setPostCode(buildPostCode(sourceId));
        entity.setPostName(limitText(firstNonBlank(source.getOaName(), source.getOaMark(), sourceId), 50));
        entity.setPostSort(0);
        entity.setStatus(SystemConstants.NORMAL);
        entity.setDelFlag(SystemConstants.NORMAL);
        entity.setOaSourceType("JOBTITLE");
        entity.setOaSourceId(sourceId);
        entity.setRemark(limitText(firstNonBlank(source.getOaRemark(), "泛微 HRM 自动同步"), 500));
        SysPost target = entity;
        if (action == Action.CREATE) {
            DataPermissionHelper.ignore(() -> sysPostMapper.insert(target));
        } else {
            DataPermissionHelper.ignore(() -> sysPostMapper.updateById(target));
        }
        return action;
    }

    /** 全量岗位同步成功后，停用已经从泛微目录中消失的岗位。 */
    private void disableMissingOaPosts(Set<String> sourceIds, OaSyncBatch batch) {
        if (sourceIds.isEmpty()) {
            return;
        }
        List<SysPost> stalePosts = DataPermissionHelper.ignore(() -> sysPostMapper.selectList(
            Wrappers.<SysPost>lambdaQuery()
                .eq(SysPost::getOaSourceType, "JOBTITLE")
                .eq(SysPost::getDelFlag, SystemConstants.NORMAL)
                .notIn(SysPost::getOaSourceId, sourceIds)));
        for (SysPost post : stalePosts) {
            if (!SystemConstants.DISABLE.equals(post.getStatus())) {
                post.setStatus(SystemConstants.DISABLE);
                DataPermissionHelper.ignore(() -> sysPostMapper.updateById(post));
                increment(batch, "disabledCount");
                writeDetail(batch, "JOBTITLE", post.getOaSourceId(), post.getOaSourceId(), post.getPostId(),
                    "DISABLE", SUCCESS, "岗位未出现在本次泛微全量目录中，已停用");
            }
        }
    }

    /**
     * 全量岗位同步成功后清理历史手工岗位。
     *
     * <p>岗位关联先解除，再对岗位做逻辑删除，避免用户岗位关联表留下无效的本地岗位。</p>
     */
    private void cleanupLegacyPosts() {
        List<SysPost> legacyPosts = DataPermissionHelper.ignore(() -> sysPostMapper.selectList(
            Wrappers.<SysPost>lambdaQuery()
                .eq(SysPost::getDelFlag, SystemConstants.NORMAL)
                .and(wrapper -> wrapper.isNull(SysPost::getOaSourceType)
                    .or().eq(SysPost::getOaSourceType, ""))));
        List<Long> postIds = legacyPosts.stream()
            .map(SysPost::getPostId)
            .filter(Objects::nonNull)
            .toList();
        if (postIds.isEmpty()) {
            return;
        }
        DataPermissionHelper.ignore(() -> sysUserPostMapper.lambda()
            .in(SysUserPost::getPostId, postIds).delete());
        DataPermissionHelper.ignore(() -> sysPostMapper.deleteByIds(postIds));
        log.info("已逻辑删除历史手工岗位，count={}", postIds.size());
    }

    /** 全量人员同步成功后，停用已经从泛微目录中消失的本地账号。 */
    private void disableMissingOaUsers(Set<String> sourceIds, OaSyncBatch batch) {
        if (sourceIds.isEmpty()) {
            return;
        }
        List<SysUser> staleUsers = DataPermissionHelper.ignore(() -> sysUserMapper.selectList(
            Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getOaSourceType, "USER")
                .eq(SysUser::getDelFlag, SystemConstants.NORMAL)
                .notIn(SysUser::getOaSourceId, sourceIds)));
        for (SysUser user : staleUsers) {
            if (user.isSuperAdmin()) {
                continue;
            }
            if (!SystemConstants.DISABLE.equals(user.getStatus())) {
                user.setStatus(SystemConstants.DISABLE);
                DataPermissionHelper.ignore(() -> sysUserMapper.updateById(user));
                increment(batch, "disabledCount");
                writeDetail(batch, "USER", user.getOaSourceId(), user.getOaSourceId(), user.getUserId(),
                    "DISABLE", SUCCESS, "人员未出现在本次泛微全量目录中，已停用");
            }
        }
    }

    /**
     * 全量人员同步成功后清理历史手工用户。
     *
     * <p>超级管理员始终保留；存在科室服务关系的历史用户不能直接删除时仅停用，
     * 防止破坏现有业务关系，并通过同步明细提示后续处理。</p>
     */
    private void cleanupLegacyUsers(OaSyncBatch batch) {
        List<SysUser> legacyUsers = DataPermissionHelper.ignore(() -> sysUserMapper.selectList(
            Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getDelFlag, SystemConstants.NORMAL)
                .and(wrapper -> wrapper.isNull(SysUser::getOaSourceType)
                    .or().eq(SysUser::getOaSourceType, ""))));
        for (SysUser user : legacyUsers) {
            if (user.isSuperAdmin() || user.getUserId() == null) {
                continue;
            }
            try {
                sysUserService.deleteUserById(user.getUserId());
                log.info("已逻辑删除历史手工用户，userId={}, userName={}", user.getUserId(), user.getUserName());
            } catch (Exception ex) {
                // 仍被业务服务关系引用的用户不能删除，至少停用，避免继续作为有效主数据使用。
                user.setStatus(SystemConstants.DISABLE);
                DataPermissionHelper.ignore(() -> sysUserMapper.updateById(user));
                increment(batch, "pendingCount");
                writeDetail(batch, USER, user.getUserName(), user.getUserName(), user.getUserId(),
                    "DISABLE", PENDING, "历史本地用户仍存在科室服务关系，已停用，未执行删除：" + message(ex));
                log.warn("历史手工用户存在服务关系，已停用未删除，userId={}, userName={}",
                    user.getUserId(), user.getUserName(), ex);
            }
        }
    }

    private OaSyncBatch startBatch(String syncType, boolean full) {
        OaSyncBatch previous = syncBatchMapper.selectLastSuccess(syncType);
        boolean fullMode = full || previous == null || previous.getFinishedAt() == null;
        OaSyncBatch batch = new OaSyncBatch();
        batch.setSyncType(syncType);
        batch.setSyncMode(fullMode ? "FULL" : "INCREMENTAL");
        batch.setStatus("RUNNING");
        batch.setStartedAt(LocalDateTime.now());
        batch.setWatermark(fullMode ? null : formatWatermark(previous.getFinishedAt()));
        batch.setTotalCount(0);
        batch.setSuccessCount(0);
        batch.setCreatedCount(0);
        batch.setUpdatedCount(0);
        batch.setDisabledCount(0);
        batch.setPendingCount(0);
        batch.setFailedCount(0);
        batch.setVersion(1L);
        syncBatchMapper.insert(batch);
        return batch;
    }

    private void finishBatch(OaSyncBatch batch) {
        batch.setFinishedAt(LocalDateTime.now());
        boolean pendingUser = USER.equals(batch.getSyncType())
            && batch.getPendingCount() != null && batch.getPendingCount() > 0;
        batch.setStatus(batch.getFailedCount() != null && batch.getFailedCount() > 0 || pendingUser
            ? "PARTIAL" : SUCCESS);
        batch.setMessage("同步完成");
        syncBatchMapper.updateById(batch);
    }

    private void failBatch(OaSyncBatch batch, Exception ex) {
        batch.setStatus("FAILED");
        batch.setFinishedAt(LocalDateTime.now());
        batch.setMessage(message(ex));
        try {
            syncBatchMapper.updateById(batch);
        } catch (Exception updateEx) {
            log.error("更新泛微 HRM 失败批次失败，batchId={}", batch.getId(), updateEx);
        }
    }

    private void applyOutcome(OaSyncBatch batch, SyncOutcome outcome) {
        if (outcome.success()) {
            increment(batch, "successCount");
            if ("CREATE".equals(outcome.action())) {
                increment(batch, "createdCount");
            } else if ("DISABLE".equals(outcome.action())) {
                increment(batch, "disabledCount");
            } else {
                increment(batch, "updatedCount");
            }
        } else if (PENDING.equals(outcome.detailStatus())) {
            increment(batch, "pendingCount");
        } else {
            increment(batch, "failedCount");
        }
    }

    private SyncOutcome pending(OaSyncBatch batch, OaUserDirectoryVo source,
                                String sourceKey, String message) {
        writeDetail(batch, "USER", source.getOaUserId(), sourceKey, null,
            "SKIP", PENDING, message);
        return SyncOutcome.pending(message);
    }

    private void writeDetail(OaSyncBatch batch, String entityType, String sourceId, String sourceKey,
                             Long localId, String action, String detailStatus, String message) {
        try {
            OaSyncDetail detail = new OaSyncDetail();
            detail.setBatchId(batch.getId());
            detail.setEntityType(entityType);
            detail.setSourceId(sourceId);
            detail.setSourceKey(sourceKey);
            detail.setLocalId(localId);
            detail.setAction(action);
            detail.setDetailStatus(detailStatus);
            detail.setMessage(limit(message));
            syncDetailMapper.insert(detail);
        } catch (Exception ex) {
            log.warn("写入泛微 HRM 同步明细失败，batchId={}, sourceId={}", batch.getId(), sourceId, ex);
        }
    }

    private void increment(OaSyncBatch batch, String field) {
        switch (field) {
            case "totalCount" -> batch.setTotalCount(value(batch.getTotalCount()) + 1);
            case "successCount" -> batch.setSuccessCount(value(batch.getSuccessCount()) + 1);
            case "createdCount" -> batch.setCreatedCount(value(batch.getCreatedCount()) + 1);
            case "updatedCount" -> batch.setUpdatedCount(value(batch.getUpdatedCount()) + 1);
            case "disabledCount" -> batch.setDisabledCount(value(batch.getDisabledCount()) + 1);
            case "pendingCount" -> batch.setPendingCount(value(batch.getPendingCount()) + 1);
            case "failedCount" -> batch.setFailedCount(value(batch.getFailedCount()) + 1);
            default -> throw new IllegalArgumentException("未知同步计数字段：" + field);
        }
    }

    private void incrementAction(OaSyncBatch batch, Action action) {
        if (action == Action.CREATE) {
            increment(batch, "createdCount");
        } else {
            increment(batch, "updatedCount");
        }
    }

    private OaSyncResultVo toResult(OaSyncBatch batch) {
        OaSyncResultVo result = new OaSyncResultVo();
        result.setBatchId(batch.getId());
        result.setSyncType(batch.getSyncType());
        result.setSyncMode(batch.getSyncMode());
        result.setStatus(batch.getStatus());
        result.setWatermark(batch.getWatermark());
        result.setStartedAt(batch.getStartedAt());
        result.setFinishedAt(batch.getFinishedAt());
        result.setTotalCount(batch.getTotalCount());
        result.setSuccessCount(batch.getSuccessCount());
        result.setCreatedCount(batch.getCreatedCount());
        result.setUpdatedCount(batch.getUpdatedCount());
        result.setDisabledCount(batch.getDisabledCount());
        result.setPendingCount(batch.getPendingCount());
        result.setFailedCount(batch.getFailedCount());
        result.setMessage(batch.getMessage());
        return result;
    }

    private String departmentKey(String subcompanyId, String departmentId) {
        return (StringUtils.isBlank(subcompanyId) ? "0" : StringUtils.trim(subcompanyId))
            + ":" + (StringUtils.isBlank(departmentId) ? "0" : StringUtils.trim(departmentId));
    }

    private String formatWatermark(LocalDateTime finishedAt) {
        return finishedAt.minusSeconds(Math.max(0, properties.getHrmSyncOverlapSeconds())).format(OA_TIME);
    }

    private boolean hasNext(long total, long fetched, int currentSize, int pageSize) {
        if (currentSize <= 0) {
            return false;
        }
        return total > 0 ? fetched < total : currentSize >= pageSize;
    }

    private int syncPageSize() {
        return Math.max(20, Math.min(properties.getHrmSyncPageSize(), 1000));
    }

    private BigDecimal decimal(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(StringUtils.trim(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String toGender(String sex) {
        if (StringUtils.isBlank(sex)) {
            return "2";
        }
        String value = StringUtils.trim(sex);
        if ("男".equals(value) || "M".equalsIgnoreCase(value) || "male".equalsIgnoreCase(value)) {
            return "0";
        }
        if ("女".equals(value) || "F".equalsIgnoreCase(value) || "female".equalsIgnoreCase(value)) {
            return "1";
        }
        return "2";
    }

    private String toLocalStatus(String oaStatus) {
        if (StringUtils.isBlank(oaStatus)) {
            return "0";
        }
        return isInactiveOaStatus(oaStatus) ? "1" : "0";
    }

    private boolean isKnownOaStatus(String status) {
        return switch (status) {
            case "0", "1", "2", "3", "4", "5", "6", "7" -> true;
            default -> false;
        };
    }

    private boolean isInactiveOaStatus(String status) {
        return "4".equals(status) || "5".equals(status) || "6".equals(status) || "7".equals(status);
    }

    private void lockSync() {
        if (!syncLock.tryLock()) {
            throw new ServiceException("已有泛微 HRM 同步任务正在执行，请稍后再试");
        }
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private String message(Exception ex) {
        return limit(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
    }

    private String limit(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 2000 ? text.substring(0, 2000) : text;
    }

    private String limitText(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        int textLength = text.codePointCount(0, text.length());
        if (textLength <= maxLength) {
            return text;
        }
        return text.substring(0, text.offsetByCodePoints(0, maxLength));
    }

    /** 本地岗位编码上限为 64 位，超长泛微 ID 用稳定摘要避免截断碰撞。 */
    private String buildPostCode(String sourceId) {
        String prefix = "OA-";
        if (sourceId.length() <= 64 - prefix.length()) {
            return prefix + sourceId;
        }
        return prefix + sourceId.substring(0, 52) + "-" + DigestUtil.md5Hex(sourceId).substring(0, 8);
    }

    private ServiceException asServiceException(String prefix, Exception ex) {
        if (ex instanceof ServiceException serviceException) {
            return serviceException;
        }
        return new ServiceException(prefix + "：" + message(ex));
    }

    private enum Action {
        CREATE,
        UPDATE
    }

    private record UserResolution(SysUser user, String conflictMessage) {

        static UserResolution empty() {
            return new UserResolution(null, null);
        }

        static UserResolution of(SysUser user) {
            return new UserResolution(user, null);
        }

        static UserResolution conflict(String message) {
            return new UserResolution(null, message);
        }
    }

    private record SyncOutcome(boolean success, String action, Long localId,
                               String detailStatus, String message) {

        static SyncOutcome success(String action, Long localId) {
            return new SyncOutcome(true, action, localId, SUCCESS, null);
        }

        static SyncOutcome pending(String message) {
            return new SyncOutcome(false, "SKIP", null, PENDING, message);
        }

        static SyncOutcome conflict(String message) {
            return new SyncOutcome(false, "SKIP", null, CONFLICT, message);
        }

        static SyncOutcome failed(String message) {
            return new SyncOutcome(false, "UPDATE", null, FAILED, message);
        }
    }
}
