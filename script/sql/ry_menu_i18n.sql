-- ----------------------------
-- 菜单国际化表
-- ----------------------------
create table sys_menu_i18n
(
    id          bigint          not null auto_increment  comment '主键',
    menu_id     bigint          not null                 comment '菜单ID',
    locale      varchar(10)     not null                 comment '语言区域（如 en_US, id_ID）',
    menu_name   varchar(50)     not null                 comment '菜单名称',
    primary key (id),
    unique key uniq_menu_locale (menu_id, locale),
    key idx_locale (locale)
) engine=innodb auto_increment=1 comment = '菜单国际化表';

-- ----------------------------
-- 菜单国际化数据（英文 en_US）
-- ----------------------------
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES
-- 一级菜单
(1761400000000000001, 'en_US', 'System Management'),
(1761400000000000002, 'en_US', 'System Monitor'),
(1761400000000000003, 'en_US', 'System Tools'),
(1761400000000000004, 'en_US', 'PLUS Official Site'),
(1761400000000000005, 'en_US', 'Test Menu'),
(1761400000000000008, 'en_US', 'AI Chat'),
-- 二级菜单
(1761400000000000100, 'en_US', 'User Management'),
(1761400000000000101, 'en_US', 'Role Management'),
(1761400000000000102, 'en_US', 'Menu Management'),
(1761400000000000103, 'en_US', 'Department Management'),
(1761400000000000104, 'en_US', 'Position Management'),
(1761400000000000105, 'en_US', 'Dictionary Management'),
(1761400000000000106, 'en_US', 'Parameter Settings'),
(1761400000000000107, 'en_US', 'Notice'),
(1761400000000000108, 'en_US', 'Log Management'),
(1761400000000000109, 'en_US', 'Online Users'),
(1761400000000000113, 'en_US', 'Cache Monitor'),
(1761400000000000115, 'en_US', 'Code Generation'),
(1761400000000000117, 'en_US', 'Admin Monitor'),
(1761400000000000118, 'en_US', 'File Management'),
(1761400000000000120, 'en_US', 'Job Center'),
(1761400000000000121, 'en_US', 'AI Console'),
(1761400000000000123, 'en_US', 'Client Management'),
-- 三级菜单
(1761400000000000500, 'en_US', 'Operation Log'),
(1761400000000000501, 'en_US', 'Login Log');

-- ----------------------------
-- 菜单国际化数据（印尼语 id_ID）
-- ----------------------------
INSERT INTO sys_menu_i18n (menu_id, locale, menu_name) VALUES
-- 一级菜单
(1761400000000000001, 'id_ID', 'Manajemen Sistem'),
(1761400000000000002, 'id_ID', 'Monitor Sistem'),
(1761400000000000003, 'id_ID', 'Alat Sistem'),
(1761400000000000004, 'id_ID', 'Situs Resmi PLUS'),
(1761400000000000005, 'id_ID', 'Menu Uji'),
(1761400000000000008, 'id_ID', 'Obrolan AI'),
-- 二级菜单
(1761400000000000100, 'id_ID', 'Manajemen Pengguna'),
(1761400000000000101, 'id_ID', 'Manajemen Peran'),
(1761400000000000102, 'id_ID', 'Manajemen Menu'),
(1761400000000000103, 'id_ID', 'Manajemen Departemen'),
(1761400000000000104, 'id_ID', 'Manajemen Jabatan'),
(1761400000000000105, 'id_ID', 'Manajemen Kamus'),
(1761400000000000106, 'id_ID', 'Pengaturan Parameter'),
(1761400000000000107, 'id_ID', 'Pemberitahuan'),
(1761400000000000108, 'id_ID', 'Manajemen Log'),
(1761400000000000109, 'id_ID', 'Pengguna Online'),
(1761400000000000113, 'id_ID', 'Monitor Cache'),
(1761400000000000115, 'id_ID', 'Pembuat Kode'),
(1761400000000000117, 'id_ID', 'Monitor Admin'),
(1761400000000000118, 'id_ID', 'Manajemen Berkas'),
(1761400000000000120, 'id_ID', 'Pusat Pekerjaan'),
(1761400000000000121, 'id_ID', 'Konsol AI'),
(1761400000000000123, 'id_ID', 'Manajemen Klien'),
-- 三级菜单
(1761400000000000500, 'id_ID', 'Log Operasi'),
(1761400000000000501, 'id_ID', 'Log Masuk');
