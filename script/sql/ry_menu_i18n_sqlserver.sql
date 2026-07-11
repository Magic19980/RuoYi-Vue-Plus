-- ----------------------------
-- 菜单国际化表（SQL Server）
-- ----------------------------
create table sys_menu_i18n
(
    id          bigint          not null identity(1,1),
    menu_id     bigint          not null,
    locale      varchar(10)     not null,
    menu_name   varchar(50)     not null,
    constraint pk_sys_menu_i18n primary key (id),
    constraint uq_menu_locale unique (menu_id, locale)
);

create index idx_menu_i18n_locale on sys_menu_i18n (locale);

-- 添加表注释
execute sp_addextendedproperty 'MS_Description', '菜单国际化表', 'schema', 'dbo', 'table', 'sys_menu_i18n';
execute sp_addextendedproperty 'MS_Description', '主键', 'schema', 'dbo', 'table', 'sys_menu_i18n', 'column', 'id';
execute sp_addextendedproperty 'MS_Description', '菜单ID', 'schema', 'dbo', 'table', 'sys_menu_i18n', 'column', 'menu_id';
execute sp_addextendedproperty 'MS_Description', '语言区域（如 en_US, id_ID）', 'schema', 'dbo', 'table', 'sys_menu_i18n', 'column', 'locale';
execute sp_addextendedproperty 'MS_Description', '菜单名称', 'schema', 'dbo', 'table', 'sys_menu_i18n', 'column', 'menu_name';

-- ----------------------------
-- 菜单国际化数据（英文 en_US）
-- ----------------------------
-- 一级菜单
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000001, 'en_US', 'System Management');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000002, 'en_US', 'System Monitor');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000003, 'en_US', 'System Tools');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000004, 'en_US', 'PLUS Official Site');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000005, 'en_US', 'Test Menu');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000008, 'en_US', 'AI Chat');
-- 二级菜单
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000100, 'en_US', 'User Management');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000101, 'en_US', 'Role Management');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000102, 'en_US', 'Menu Management');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000103, 'en_US', 'Department Management');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000104, 'en_US', 'Position Management');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000105, 'en_US', 'Dictionary Management');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000106, 'en_US', 'Parameter Settings');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000107, 'en_US', 'Notice');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000108, 'en_US', 'Log Management');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000109, 'en_US', 'Online Users');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000113, 'en_US', 'Cache Monitor');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000115, 'en_US', 'Code Generation');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000117, 'en_US', 'Admin Monitor');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000118, 'en_US', 'File Management');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000120, 'en_US', 'Job Center');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000121, 'en_US', 'AI Console');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000123, 'en_US', 'Client Management');
-- 三级菜单
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000500, 'en_US', 'Operation Log');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000501, 'en_US', 'Login Log');

-- ----------------------------
-- 菜单国际化数据（印尼语 id_ID）
-- ----------------------------
-- 一级菜单
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000001, 'id_ID', 'Manajemen Sistem');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000002, 'id_ID', 'Monitor Sistem');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000003, 'id_ID', 'Alat Sistem');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000004, 'id_ID', 'Situs Resmi PLUS');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000005, 'id_ID', 'Menu Uji');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000008, 'id_ID', 'Obrolan AI');
-- 二级菜单
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000100, 'id_ID', 'Manajemen Pengguna');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000101, 'id_ID', 'Manajemen Peran');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000102, 'id_ID', 'Manajemen Menu');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000103, 'id_ID', 'Manajemen Departemen');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000104, 'id_ID', 'Manajemen Jabatan');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000105, 'id_ID', 'Manajemen Kamus');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000106, 'id_ID', 'Pengaturan Parameter');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000107, 'id_ID', 'Pemberitahuan');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000108, 'id_ID', 'Manajemen Log');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000109, 'id_ID', 'Pengguna Online');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000113, 'id_ID', 'Monitor Cache');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000115, 'id_ID', 'Pembuat Kode');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000117, 'id_ID', 'Monitor Admin');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000118, 'id_ID', 'Manajemen Berkas');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000120, 'id_ID', 'Pusat Pekerjaan');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000121, 'id_ID', 'Konsol AI');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000123, 'id_ID', 'Manajemen Klien');
-- 三级菜单
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000500, 'id_ID', 'Log Operasi');
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES (1761400000000000501, 'id_ID', 'Log Masuk');
