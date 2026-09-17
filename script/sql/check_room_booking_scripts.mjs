import { readFile } from 'node:fs/promises';

const sqlDirectory = new URL('./', import.meta.url);
const paths = {
  init: new URL('./department_management.sql', sqlDirectory),
  upgrade: new URL('./upgrade_room_booking_phase3.sql', sqlDirectory),
  rollback: new URL('./rollback_room_booking_phase3.sql', sqlDirectory),
};

const [init, upgrade, rollback] = await Promise.all(
  Object.values(paths).map(path => readFile(path, 'utf8')),
);

const permissions = [
  'department:room:list',
  'department:room:query',
  'department:room:book',
  'department:room:cancel',
  'department:room:add',
  'department:room:edit',
  'department:room:remove',
  'department:room:approve',
  'department:room:export',
  'department:room:manage',
  'department:room:manageAll',
];

const phase3Tables = [
  'dm_room_floor_plan',
  'dm_room_floor_plan_version',
  'dm_room_approval_rule',
  'dm_room_quota_policy',
  'dm_room_amenity',
  'dm_room_amenity_relation',
  'dm_room_booking_blacklist',
  'dm_room_booking_recurrence_exception',
  'dm_room_quota_usage',
];

const phase3Alterations = [
  ['预约幂等列', 'add column request_key', 'drop column request_key'],
  ['预约幂等唯一索引', 'add unique key uk_dm_room_booking_request', 'drop index uk_dm_room_booking_request'],
  ['房间平面图字段', 'add column floor_plan_id', 'drop column floor_plan_id'],
  ['房间楼栋字段', 'add column building_name', 'drop column building_name'],
  ['房间楼层字段', 'add column floor_name', 'drop column floor_name'],
  ['房间区域字段', 'add column area_name', 'drop column area_name'],
  ['房间合并组字段', 'add column merge_group', 'drop column merge_group'],
  ['房间展示图字段', 'add column display_image', 'drop column display_image'],
  ['房间照片字段', 'add column photo_urls', 'drop column photo_urls'],
  ['房间使用说明字段', 'add column usage_guide', 'drop column usage_guide'],
  ['房间负责人字段', 'add column manager_user_id', 'drop column manager_user_id'],
  ['房间展示屏字段', 'add column display_screen', 'drop column display_screen'],
  ['房间跨日字段', 'add column allow_cross_day', 'drop column allow_cross_day'],
  ['维护计划类型字段', 'add column recurrence_type', 'drop column recurrence_type'],
  ['维护计划间隔字段', 'add column recurrence_interval', 'drop column recurrence_interval'],
  ['维护计划次数字段', 'add column recurrence_count', 'drop column recurrence_count'],
  ['维护计划截止日期字段', 'add column recurrence_until', 'drop column recurrence_until'],
  ['审批步骤字段', 'add column approval_step', 'drop column approval_step'],
  ['审批截止时间字段', 'add column approval_due_at', 'drop column approval_due_at'],
  ['审批查询索引', 'add key idx_dm_room_booking_occurrence_approval', 'drop index idx_dm_room_booking_occurrence_approval'],
  ['配额单价字段', 'add column unit_price', 'drop column unit_price'],
  ['配额退款字段', 'add column refund_rate', 'drop column refund_rate'],
  ['房间平面图查询索引', 'add key idx_dm_room_resource_floor_plan', 'drop index idx_dm_room_resource_floor_plan'],
];

const failures = [];
for (const permission of permissions) {
  if (!init.includes(permission)) failures.push(`初始化脚本缺少权限：${permission}`);
  if (!upgrade.includes(permission)) failures.push(`升级脚本缺少权限：${permission}`);
}
for (const table of phase3Tables) {
  if (!init.includes(`create table if not exists ${table}`)) failures.push(`初始化脚本缺少表：${table}`);
  if (!upgrade.includes(`create table if not exists ${table}`)) failures.push(`升级脚本缺少表：${table}`);
  if (!rollback.includes(`drop table if exists ${table}`)) failures.push(`回滚脚本缺少表删除：${table}`);
}
for (const [label, upgradeToken, rollbackToken] of phase3Alterations) {
  if (!upgrade.includes(upgradeToken)) failures.push(`升级脚本缺少结构变更：${label}（${upgradeToken}）`);
  if (!rollback.includes(rollbackToken)) failures.push(`回滚脚本缺少结构撤销：${label}（${rollbackToken}）`);
}
const rollbackCleansMenuRange = rollback.includes(
  'delete from sys_menu where menu_id between 1761400000000003160 and 1761400000000003170',
);
for (const menuId of ['3160', '3161', '3162', '3163', '3164', '3165', '3166', '3167', '3168', '3169', '3170']) {
  const fullId = `176140000000000${menuId}`;
  if (!upgrade.includes(fullId)) failures.push(`升级脚本缺少菜单：${fullId}`);
  if (!rollbackCleansMenuRange && !rollback.includes(fullId)) failures.push(`回滚脚本缺少菜单清理：${fullId}`);
}
if (!upgrade.includes('insert ignore into sys_role_menu')) failures.push('升级脚本缺少角色授权');
if (!rollback.includes('delete from sys_role_menu')) failures.push('回滚脚本缺少角色授权清理');

if (failures.length > 0) {
  console.error('[room-booking-sql] 校验失败');
  failures.forEach(failure => console.error(`- ${failure}`));
  process.exitCode = 1;
} else {
  console.log(`[room-booking-sql] 初始化/升级/回滚脚本校验通过：${phase3Tables.length} 张 Phase 3 表，${permissions.length} 个权限`);
}
