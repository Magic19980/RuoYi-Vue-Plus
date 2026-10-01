-- 文档模板中心回滚脚本
-- 适用数据库：MySQL 8+
--
-- 作用：
-- 1. 删除“文档模板中心”菜单及其所有子菜单；
-- 2. 删除角色与上述菜单的授权关系；
-- 3. 删除文档模板中心的绑定、版本和主表。
--
-- 注意：DROP TABLE 会永久删除模板中心数据及上传文件的关联记录，执行前请确认已完成备份。
-- 本脚本不删除“通用导入模板”、泛微业务配置、资料库或其他业务模板。

/* 菜单回滚：按根菜单递归处理所有后代菜单。 */
START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_document_template_center_menu_ids;

CREATE TEMPORARY TABLE tmp_document_template_center_menu_ids AS
WITH RECURSIVE menu_tree AS (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_id = 1761400000000003160
       OR menu_name = '文档模板中心'
       OR component = 'department/documentTemplate/index'

    UNION ALL

    SELECT child.menu_id
    FROM sys_menu child
    INNER JOIN menu_tree parent ON child.parent_id = parent.menu_id
)
SELECT DISTINCT menu_id
FROM menu_tree;

ALTER TABLE tmp_document_template_center_menu_ids
    ADD PRIMARY KEY (menu_id);

DELETE role_menu
FROM sys_role_menu role_menu
INNER JOIN tmp_document_template_center_menu_ids menu_ids
        ON menu_ids.menu_id = role_menu.menu_id;

DELETE menu
FROM sys_menu menu
INNER JOIN tmp_document_template_center_menu_ids menu_ids
        ON menu_ids.menu_id = menu.menu_id;

DROP TEMPORARY TABLE IF EXISTS tmp_document_template_center_menu_ids;

COMMIT;

/* 表回滚：先删除依赖表，再删除主表。 */
DROP TABLE IF EXISTS dm_document_template_binding;
DROP TABLE IF EXISTS dm_document_template_version;
DROP TABLE IF EXISTS dm_document_template;
