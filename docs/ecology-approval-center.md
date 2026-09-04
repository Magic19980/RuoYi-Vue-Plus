# 泛微通用审批中心

## 边界

- 泛微中心只承接其他业务的外部 OA 审批。
- SCORE、5WHY 继续使用 `ruoyi-department` 中的本地审核人、审核任务和状态字段，不改走泛微。
- `ruoyi-workflow` 的 Warm-Flow 本地引擎仍保留给本地工作流场景，和泛微中心互不替代。

## 启用配置

在环境变量或配置中心注入以下配置，不要把真实密钥提交到代码库：

```yaml
ecology:
  enabled: ${ECOLOGY_ENABLED:false}
  address: ${ECOLOGY_ADDRESS}
  # 以下三项来自 System-boxplusn 的 ECologyUtils，需从代码迁移到配置中心/环境变量
  app-id: ${ECOLOGY_APP_ID}
  server-public-key: ${ECOLOGY_SERVER_PUBLIC_KEY}
  server-secret: ${ECOLOGY_SERVER_SECRET}
  # HRM REST 人员目录接口，对应 System-boxplusn 的 getOaUserList
  # 该值来自 System-boxplusn 的 OaServiceImpl.applyId
  hrm-apply-id: ${ECOLOGY_HRM_APPLY_ID}
  hrm-user-list-path: /api/hrm/resful/getHrmUserInfoWithPage
  # 现场接口如果支持按姓名或 keyword 查询，可改为 lastname/keyword
  hrm-user-search-field: workcode
  hrm-skip-secondary-accounts: true
  hrm-sync-enabled: false
  # 全量同步分页之间的间隔，单位毫秒；建议保留 200~500
  hrm-sync-page-interval-millis: 300
  hrm-request-retry-count: 2
  # 仅对网络异常、408/425/429 和 5xx 重试，并采用指数退避
  hrm-request-retry-initial-delay-millis: 1000
  hrm-request-retry-max-delay-millis: 30000
  request-status-path: /api/workflow/paService/getWorkflowRequest
  attachment-write-empty-fields: true
  request-link-template: https://oa.example.com/spa/workflow/static4form/index.html#/main/workflowengine/requestview/requestview?requestid={requestId}
  callback-enabled: ${ECOLOGY_CALLBACK_ENABLED:false}
  callback-secret: ${ECOLOGY_CALLBACK_SECRET:}
  reconcile-enabled: ${ECOLOGY_RECONCILE_ENABLED:false}
  reconcile-batch-size: ${ECOLOGY_RECONCILE_BATCH_SIZE:50}
```

默认 `enabled=false`，因此未配置泛微环境时系统仍可启动；网络异常会进入 `UNKNOWN`，避免误重提可能已在泛微侧受理的请求。

当前科室系统采用新配置结构 `ecology.*`，其值按 System-boxplusn 旧配置迁移：`oa.address` 对应 `ecology.address`；旧代码中的 `ECologyUtils.APPID/SERVER_PUBLIC_KEY/SERVER_SECRET` 对应 `ecology.app-id/server-public-key/server-secret`；`OaServiceImpl.applyId` 对应 `ecology.hrm-apply-id`。泛微表单的 `workflowId` 和字段编码由“泛微工作中心 / 流程配置 / 表单配置”维护，业务页面只选择已配置的表单和审批方式，不再把流程 ID 写在业务代码中。

## 数据准备顺序

1. 新环境执行最新的 `script/sql/department_management.sql`；已有环境按版本顺序执行 `script/sql/migration/` 中尚未执行的脚本，最后执行 `20260903_ecology_workflow_form_config.sql` 和 `20260903_ecology_workflow_option_mapping.sql`。
2. 在“泛微工作中心 / 流程配置 / 表单配置”新增一个可复用表单：填写泛微表单名称、真实 `workflowId` 和公用字段映射；只有该表单特有的业务字段才填写到“表单专属字段映射”。
3. 切换到“审批方式配置”，维护全局通用的泛微审批方式真实值（例如 `splcfs=0`）和展示名称，再按实际规则配置审批节点编码与人员字段（例如 `ycq1`、`hq2`）。审批方式不绑定某一个表单。
4. 在“泛微工作中心 / 业务配置”维护业务类型，选择一个表单、多选允许的通用审批方式并设置默认项；在“审批方案管理”中为具体审批方式维护审批人和抄送人。
5. 用户发起申请或导入业务时只选择业务类型、审批方式和审批策略。系统会动态取得表单的 `workflowId`、审批方式真实值和节点字段，生成泛微请求，并保存 `requestId`。
6. 审批人选择使用已同步到 `sys_user` 的泛微人员；系统以 `sys_user.oa_source_id` 作为泛微 `userid`，工号只作为辅助核对字段，不再维护独立的用户绑定关系。

## HRM 组织与人员同步

HRM 同步页面为“泛微审批中心 / 泛微 HRM 同步”，用于把泛微组织目录和人员资料同步到科室管理系统。它不改变 SCORE、5WHY 的本地审批边界。

推荐按以下顺序执行：

1. 执行 `script/sql/migration/20260829_ecology_hrm_sync.sql`；新环境执行 `department_management.sql` 时已经包含同一组 HRM 审计表和菜单。若基础系统脚本不是当前版本，再执行 `script/sql/migration/20260829_ecology_hrm_sync_upgrade.sql`，补齐 `sys_dept` 泛微来源字段；最后执行 `script/sql/migration/20260830_ecology_hrm_master_data_cleanup.sql`，补齐 `sys_user`、`sys_post` 来源字段并清理旧中转表。
2. 确认 `hrm-apply-id`、HRM 四个接口路径和 `ECOLOGY_ENABLED=true`；HRM REST 使用 `key = MD5(hrmApplyId + 当前毫秒时间戳).toUpperCase()`，请求头为 `key`、`ts`。
3. 点击“同步并接管组织”。系统按泛微分部、部门的上级 ID 重建本地 `sys_dept` 层级，并为有效泛微部门自动维护 `dm_department` 业务科室配置；旧本地部门保留记录但自动停用。泛微来源类型和来源 ID 会写入 `sys_dept`，后续同步按来源 ID 幂等更新。
4. 点击“同步人员”。泛微岗位直接写入本地 `sys_post`，泛微人员直接写入本地 `sys_user`；人员的泛微 ID 写入 `oa_source_id`，工号写入 `employee_no`，姓名、邮箱、手机、性别、状态和主部门同步到本地。首次创建用户必须配置 `hrm-default-password`，密码只使用本地配置，不从泛微读取。
5. 泛微是部门、岗位、人员的唯一主数据源。全量同步成功后，泛微已不存在的本地泛微岗位/用户会停用，不物理删除，避免历史业务数据失去关联；同步失败或来源 ID 冲突会进入同步明细，不执行可能造成数据破坏的停用操作。

系统用户主部门变更会触发现有科室人员档案同步；如果旧科室仍存在有效人员服务关系，系统会拒绝本次主部门变更并在同步明细中记录失败，需先结束旧关系再重试。定时同步由 `hrm-sync-enabled` 控制，首次上线建议保持关闭，完成一次组织接管和人员同步核对后再开启。

新环境直接执行完整初始化脚本即可。已执行过旧版 HRM 脚本的环境，依次执行 `20260829_ecology_hrm_sync_upgrade.sql` 和 `20260830_ecology_hrm_master_data_cleanup.sql`；清理脚本会删除旧的部门快照、岗位快照、人员快照、部门映射和用户绑定表，并移除对应菜单授权。

## 字段映射示例

```json
{
  "titleField": "wjmc",
  "contentField": "spxq",
  "applicantField": "tbr",
  "applicantDateField": "tbrq",
  "urgencyField": "jjcd",
  "formFields": {
    "amount": "je",
    "reason": "yy"
  },
  "imageAttachmentField": "tp",
  "fileAttachmentField": "fj"
}
```

公用字段映射中可以包含固定字段和 `formFields`。表单专属字段映射建议只写当前表单特有的 `formFields`，提交时系统自动与公用映射合并，专属配置覆盖同名键。审批方式单独保存类似下面的节点映射；`optionCode` 是写入 `splcfs` 的真实值，`fieldCode` 是审批节点对应的泛微人员字段：

```json
{
  "modeField": "splcfs",
  "copyField": "csr",
  "stages": [
    { "code": "STAGE_1", "name": "依次签", "fieldCode": "ycq1", "mode": "SEQUENTIAL", "required": true },
    { "code": "STAGE_2", "name": "会签", "fieldCode": "hq2", "mode": "COUNTERSIGN", "required": true }
  ]
}
```

表单、审批方式和业务绑定都是数据配置：表单配置维护 `workflowId`、公用字段和表单专属字段，审批方式配置维护通用真实值和节点字段，业务配置只负责选择表单及允许使用的审批方式。提交时按“业务绑定的表单 + 选择的通用审批方式”动态取得 `workflowId`、审批方式真实值和字段映射。
审批人统一使用已同步到 `sys_user` 的本地用户，提交时读取其 `oa_source_id` 作为泛微用户 ID；不再维护独立的用户绑定关系。附件使用本地 OSS 的 `ossId`，提交时生成 `[{filePath,fileName}]` 结构。

## 业务模块复用

业务模块可以复用 `IOaApplicationService` 保存申请并调用提交流程；建议业务单据自身保留业务数据，以 `businessType + applicationId + processId + oaRequestId` 建立关联。后续可按业务需要增加专用适配器，把业务对象转换成 `formDataJson`，而不复制泛微鉴权和状态处理逻辑。

## 当前实现范围

当前版本已覆盖申请草稿、业务单据关联、流程配置、泛微 HRM 人员查询、组织/部门/岗位/人员直同步、审批人快照、OSS 附件关系、泛微发起、requestId 关联、状态同步、未知状态保护、回调入口/去重和自动对账。

人员查询沿用 System-boxplusn 的 HRM REST 鉴权：`key = MD5(hrmApplyId + 当前毫秒时间戳).toUpperCase()`，时间戳放在 `ts` 请求头中。人员接口的实际搜索字段和分页字段以现场 Ecology 版本为准，当前兼容发送 `workcode`、`curpage`、`pagesize`、`page`、`pageNo`、`pageSize`，并兼容文档的 `dataList/totalSize` 和现场返回的 `rows/total`。

已经存在流程实例的流程配置执行“删除”时会自动转为停用，避免历史实例失去 workflowId 和配置追溯；没有实例的配置才会删除。

回调入口为 `POST /ecology/callback`，当前验签边界采用 HMAC-SHA256 的通用约定；`System-boxplusn` 没有回调实现，接入现场 Ecology 前必须按厂商实际签名算法、请求头和 JSON 字段调整 verifier。

附件沿用 System-boxplusn 的实现：文件先通过科室系统现有上传/OSS能力保存，提交泛微时将可访问 URL 和原始文件名组装成 `tp`（图片）/`fj`（普通附件）的 `[{filePath,fileName}]` 数组；默认即使为空也提交 `tp/fj` 空数组。当前没有调用泛微知识管理上传接口，现场必须确认泛微服务器能够访问该 URL。

自动对账只会查询已有 `requestId` 的流程，查询路径由 `request-status-path` 配置，默认兼容 System-boxplusn 的 `getWorkflowRequest`；提交超时且没有 `requestId` 会进入 `UNKNOWN`，需要人工在泛微侧核对后处理。HRM 单页请求只对可恢复错误进行有限次重试，并使用指数退避；分页之间默认间隔 300ms，单页连续失败后会终止本次同步，不继续请求后续分页。异常明细可按批次查看，失败批次修复配置后重新执行全量同步，不再依赖人员快照或人工绑定表。多实例部署时还应为定时任务增加分布式锁。
