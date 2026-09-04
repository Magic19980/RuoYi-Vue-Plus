-- 科室管理平台初始化脚本
-- 当前脚本只描述全新开发环境的最终结构，不承担历史表兼容和数据回填。

-- 业务科室配置：dept_id 直接引用 sys_dept.dept_id，避免系统部门与业务数据出现两套科室主键。
create table if not exists dm_department (
    dept_id              bigint(20)      not null comment '系统部门ID（sys_dept.dept_id）',
    status               varchar(20)     not null default 'ENABLED' comment '科室状态（ENABLED启用 DISABLED停用）',
    manager_user_id      bigint(20)      default null comment '科室负责人用户ID',
    sort_num             int(11)         not null default 0 comment '排序号',
    remark               varchar(500)    default null comment '备注',
    version              bigint(20)      default 0 comment '版本号',
    create_dept          bigint(20)      default null comment '创建部门',
    create_by            bigint(20)      default null comment '创建者',
    create_time          datetime        default null comment '创建时间',
    update_by            bigint(20)      default null comment '更新者',
    update_time          datetime        default null comment '更新时间',
    del_flag             char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (dept_id),
    key idx_dm_department_status (status, del_flag),
    key idx_dm_department_manager (manager_user_id)
) engine=innodb comment='业务科室配置';

create table if not exists dm_person_profile (
    id                  bigint(20)      not null comment '主键',
    user_id             bigint(20)      not null comment '系统用户ID',
    employee_no         varchar(64)     default null comment '工号',
    remark              varchar(500)    default null comment '备注',
    join_date           date            not null comment '加入目标科室日期',
    leave_date          date            default null comment '离开生效日期（不含当日）',
    member_type         varchar(20)     not null default 'FULL' comment '成员类型：FULL正式/TEMP临时',
    member_status       varchar(20)     not null default 'ACTIVE' comment '服务状态：ACTIVE有效/ENDED结束',
    member_source       varchar(20)     not null default 'MANUAL' comment '关系来源：MANUAL人工纳入 AUTO_MAIN主部门自动同步',
    ended_at            datetime        default null comment '结束服务时间',
    ended_by            bigint(20)      default null comment '结束服务操作人',
    end_reason          varchar(500)    default null comment '结束服务原因',
    version             bigint(20)      default 0 comment '版本号',
    create_dept         bigint(20)      default null comment '纳入日报的目标科室/创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by           bigint(20)      default null comment '更新者',
    update_time         datetime        default null comment '更新时间',
    del_flag            char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_person_profile_create_dept (create_dept),
    key idx_dm_person_profile_service_period (create_dept, member_status, join_date, leave_date),
    key idx_dm_person_profile_service_active (create_dept, member_status, del_flag, join_date, leave_date),
    key idx_dm_person_profile_user_dept_period (user_id, create_dept, del_flag, join_date, leave_date)
) engine=innodb comment='科室人员扩展信息';

-- 人员档案的 create_dept 是“纳入的目标科室”，sys_user.dept_id 仍表示系统主部门。

create table if not exists dm_person_profile_event (
    id                  bigint(20)      not null comment '主键',
    profile_id          bigint(20)      not null comment '人员档案ID',
    user_id             bigint(20)      not null comment '用户ID',
    dept_id             bigint(20)      not null comment '服务科室ID',
    event_type          varchar(20)     not null comment '事件类型：JOIN REJOIN LEAVE CHANGE',
    effective_date      date            not null comment '事件生效日期',
    member_type         varchar(20)     default null comment '事件后的成员类型',
    reason              varchar(500)    default null comment '变更原因',
    operator_id         bigint(20)      default null comment '操作人',
    create_dept         bigint(20)      default null comment '创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by           bigint(20)      default null comment '更新者',
    update_time         datetime        default null comment '更新时间',
    del_flag            char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_person_profile_event_profile (profile_id, effective_date),
    key idx_dm_person_profile_event_user_dept (user_id, dept_id, effective_date)
) engine=innodb comment='人员服务关系变更历史';

create table if not exists dm_daily_report (
    id                 bigint(20)      not null comment '主键',
    report_date        date            not null comment '日报日期',
    user_id            bigint(20)      not null comment '填报人ID',
    dept_id            bigint(20)      not null comment '填报部门ID',
    today_work         text            comment '今日工作',
    tomorrow_plan      text            comment '明日计划',
    coordination_note  varchar(2000)   default null comment '待协调事项/备注',
    status              varchar(20)    not null default 'SUBMITTED' comment '状态（SUBMITTED已填写）',
    source_type         varchar(20)     not null default 'WEB' comment '来源（WEB网页 IMPORT导入 LEAVE休假自动生成）',
    leave_id            bigint(20)      default null comment '关联休假ID，人工修改后解除关联',
    version             bigint(20)      default 0 comment '版本号',
    create_dept         bigint(20)      default null comment '创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by           bigint(20)      default null comment '更新者',
    update_time         datetime        default null comment '更新时间',
    del_flag            char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    unique key uk_dm_daily_report_date_user_dept (report_date, user_id, dept_id, del_flag),
    key idx_dm_daily_report_dept_date (dept_id, report_date),
    key idx_dm_daily_report_status (status),
    key idx_dm_daily_report_leave (leave_id),
    key idx_dm_daily_report_task_stat (dept_id, user_id, del_flag, report_date)
) engine=innodb comment='科室每日工作日报';

create table if not exists dm_daily_calendar_override (
    id                  bigint(20)      not null comment '主键',
    dept_id             bigint(20)      not null comment '科室ID',
    user_id             bigint(20)      default null comment '目标人员ID；为空表示全体成员生效',
    calendar_date       date            not null comment '例外日期',
    day_type            varchar(50)     not null comment '日期例外类型字典值（dm_date_exception）',
    need_report         tinyint(1)      not null default 0 comment '是否需要填写日报（1是 0否）',
    remark              varchar(500)    default null comment '日期说明',
    version             bigint(20)      default 0 comment '版本号',
    create_dept         bigint(20)      default null comment '创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by            bigint(20)      default null comment '更新者',
    update_time          datetime        default null comment '更新时间',
    del_flag             char(1)        default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    unique key uk_dm_daily_calendar_override_user_date (dept_id, calendar_date, user_id),
    key idx_dm_daily_calendar_override_range (dept_id, calendar_date)
) engine=innodb comment='科室日报日期例外规则';
-- user_id 为空表示全体成员生效；day_type 使用 dm_date_exception 字典，是否生成日报由 need_report 决定。

create table if not exists dm_daily_leave (
    id                  bigint(20)      not null comment '主键',
    dept_id             bigint(20)      not null comment '科室ID',
    user_id             bigint(20)      not null comment '人员ID',
    start_date          date            not null comment '休假开始日期',
    end_date            date            not null comment '休假结束日期',
    leave_type          varchar(50)     not null default '休假' comment '休假类型字典值（dm_leave_type）',
    reason              varchar(500)    default null comment '休假说明',
    status              varchar(20)     not null default 'ENABLED' comment '状态（ENABLED生效 CANCELLED取消）',
    version             bigint(20)      default 0 comment '版本号',
    create_dept         bigint(20)      default null comment '创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by           bigint(20)      default null comment '更新者',
    update_time          datetime        default null comment '更新时间',
    del_flag             char(1)        default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_daily_leave_user_range (user_id, start_date, end_date),
    key idx_dm_daily_leave_dept_range (dept_id, start_date, end_date),
    key idx_dm_daily_leave_dept_active_range (dept_id, status, del_flag, start_date, end_date)
) engine=innodb comment='科室人员休假安排';

create table if not exists dm_daily_report_attachment (
    id                 bigint(20)      not null comment '主键',
    report_id          bigint(20)      not null comment '日报ID',
    oss_id             bigint(20)      not null comment 'OSS文件ID',
    original_name      varchar(255)    default null comment '原始文件名',
    file_type          varchar(100)    default null comment '文件类型',
    sort_num            int(4)         default 0 comment '排序号',
    create_dept         bigint(20)      default null comment '创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by           bigint(20)      default null comment '更新者',
    update_time          datetime        default null comment '更新时间',
    del_flag             char(1)        default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_daily_report_attachment_report (report_id)
) engine=innodb comment='日报附件归档';

-- 科室资料分类：每个科室独立维护，支持任意层级；parent_id=0表示顶级分类。
create table if not exists dm_department_document_category (
    id                 bigint(20)      not null comment '主键',
    dept_id            bigint(20)      not null comment '所属科室ID',
    parent_id          bigint(20)      not null default 0 comment '父分类ID，0表示顶级分类',
    category_name      varchar(100)    not null comment '分类名称',
    sort_num           int(11)         not null default 0 comment '排序号',
    status             varchar(20)     not null default 'ENABLED' comment '状态（ENABLED启用 DISABLED停用）',
    remark             varchar(500)    default null comment '备注',
    create_dept        bigint(20)      default null comment '创建部门',
    create_by          bigint(20)      default null comment '创建者',
    create_time        datetime        default null comment '创建时间',
    update_by          bigint(20)      default null comment '更新者',
    update_time        datetime        default null comment '更新时间',
    del_flag           char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    unique key uk_dm_department_document_category_parent_name (dept_id, parent_id, category_name, del_flag),
    key idx_dm_department_document_category_dept (dept_id, parent_id, status)
) engine=innodb comment='科室资料分类配置';

-- 科室资料主表与版本表：文件内容复用 sys_oss，业务表只保存科室、项目、分类和版本关系。
create table if not exists dm_department_document (
    id                    bigint(20)      not null comment '主键',
    dept_id               bigint(20)      not null comment '所属科室ID',
    project_id            bigint(20)      default null comment '关联项目ID',
    category_id           bigint(20)      not null comment '资料分类ID',
    title                 varchar(200)    not null comment '资料标题',
    description           varchar(1000)   default null comment '资料说明',
    tags                  varchar(500)    default null comment '资料标签，逗号分隔',
    visibility            varchar(20)     not null default 'DEPT' comment '可见范围（DEPT科室 PRIVATE私有）',
    status                varchar(20)     not null default 'PUBLISHED' comment '状态（DRAFT草稿 PUBLISHED已发布 ARCHIVED已归档）',
    expire_date           date            default null comment '失效日期',
    current_version_id    bigint(20)      default null comment '当前版本ID',
    version_no            int(11)         not null default 1 comment '当前版本号',
    current_oss_id        bigint(20)      not null comment '当前文件OSS ID',
    current_file_name     varchar(255)    default null comment '当前文件存储名',
    current_original_name varchar(255)    not null comment '当前文件原名',
    current_file_suffix   varchar(20)     default null comment '当前文件后缀',
    current_file_size     bigint(20)      default null comment '当前文件大小（字节）',
    current_content_type  varchar(100)    default null comment '当前文件媒体类型',
    create_dept           bigint(20)      default null comment '创建部门',
    create_by             bigint(20)      default null comment '创建者',
    create_time           datetime        default null comment '创建时间',
    update_by             bigint(20)      default null comment '更新者',
    update_time           datetime        default null comment '更新时间',
    del_flag              char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_department_document_dept (dept_id, update_time),
    key idx_dm_department_document_project (project_id),
    key idx_dm_department_document_category (category_id),
    key idx_dm_department_document_oss (current_oss_id)
) engine=innodb comment='科室资料主表';

create table if not exists dm_department_document_version (
    id                    bigint(20)      not null comment '主键',
    document_id           bigint(20)      not null comment '资料ID',
    version_no            int(11)         not null comment '版本号',
    oss_id                bigint(20)      not null comment 'OSS文件ID',
    original_name         varchar(255)    not null comment '原始文件名',
    file_suffix           varchar(20)     default null comment '文件后缀',
    file_size             bigint(20)      default null comment '文件大小（字节）',
    content_type          varchar(100)    default null comment '文件媒体类型',
    version_note          varchar(500)    default null comment '版本说明',
    create_dept           bigint(20)      default null comment '创建部门',
    create_by             bigint(20)      default null comment '创建者',
    create_time           datetime        default null comment '创建时间',
    update_by             bigint(20)      default null comment '更新者',
    update_time           datetime        default null comment '更新时间',
    del_flag              char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    unique key uk_dm_department_document_version (document_id, version_no),
    key idx_dm_department_document_version_oss (oss_id)
) engine=innodb comment='科室资料版本表';

create table if not exists dm_weekly_report (
    id                  bigint(20)      not null comment '主键',
    week_start          date            not null comment '周开始日期（按周一归一）',
    week_end            date            not null comment '周结束日期（按周日归一）',
    title               varchar(200)    not null comment '周报标题',
    report_count        int(11)         not null default 0 comment '日报条数',
    required_user_count int(11)         not null default 0 comment '应填人数',
    filled_user_count   int(11)         not null default 0 comment '已填人数',
    missing_user_count  int(11)         not null default 0 comment '缺报人数',
    status              varchar(20)     not null default 'GENERATED' comment '状态（GENERATED已生成）',
    snapshot_json       longtext        comment '周报数据快照JSON',
    version             bigint(20)      default 0 comment '版本号',
    create_dept         bigint(20)      default null comment '创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by           bigint(20)      default null comment '更新者',
    update_time         datetime        default null comment '更新时间',
    del_flag            char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    unique key uk_dm_weekly_report_range (create_dept, week_start, week_end),
    key idx_dm_weekly_report_dept (create_dept, week_start)
) engine=innodb comment='科室周报数据快照';

create table if not exists dm_work_order_import_batch (
    id                    bigint(20)      not null comment '导入批次主键',
    source_file_name      varchar(255)    not null comment '原始文件名',
    oss_id                bigint(20)      default null comment '原始PDF OSS ID',
    source_period_start   date            default null comment 'PDF推断的业务周期开始',
    source_period_end     date            default null comment 'PDF推断的业务周期结束',
    page_count            int(11)         not null default 0 comment 'PDF页数',
    record_count          int(11)         not null default 0 comment '识别到的记录数',
    parsed_record_count   int(11)         not null default 0 comment '已生成候选记录数',
    pending_record_count  int(11)         not null default 0 comment '待人工确认记录数',
    status                varchar(20)     not null default 'PARSED' comment '状态（PARSED已解析 FAILED失败）',
    error_message         varchar(2000)   default null comment '错误信息',
    create_dept           bigint(20)      default null comment '创建部门',
    create_by             bigint(20)      default null comment '创建者',
    create_time           datetime        default null comment '创建时间',
    update_by             bigint(20)      default null comment '更新者',
    update_time           datetime        default null comment '更新时间',
    del_flag              char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_work_order_batch_time (create_time)
) engine=innodb comment='工单PDF导入批次';

create table if not exists dm_work_order (
    id                    bigint(20)      not null comment '主键',
    dept_id               bigint(20)      not null comment '业务所属部门ID',
    ticket_no             varchar(100)    default null comment '工单编号，PDF没有编号时由系统生成',
    occur_date            date            default null comment '工单发生年月，按每月1号保存，周报统计必填',
    source_period_start   date            default null comment '来源文件业务周期开始',
    source_period_end     date            default null comment '来源文件业务周期结束',
    request_dept          varchar(100)    default null comment '申请部门',
    settlement_unit       varchar(150)    default null comment '结算单位/服务对象',
    project_owner         varchar(100)    default null comment '项目负责人',
    system_name           varchar(150)    not null comment '项目名称',
    install_department    varchar(150)    default null comment '安装车间',
    install_team          varchar(150)    default null comment '安装班组',
    work_category         varchar(100)    default null comment '工作类别',
    fault_type            varchar(100)    default null comment '故障类型',
    title                 varchar(255)    default null comment '项目特征',
    work_content          text            comment '工作内容',
    unit                  varchar(30)     default null comment '计量单位',
    quantity              decimal(12,2)   not null default 1 comment '工程量/工单量',
    responsible_person    varchar(100)    default null comment '责任人',
    handler               varchar(100)    default null comment '处理人',
    resolution_minutes    int(11)         default null comment '处理时长（分钟）',
    feedback_channel      varchar(50)     default null comment '反馈渠道',
    source_type           varchar(20)     not null default 'MANUAL' comment '来源（PDF/MANUAL）',
    source_batch_id       bigint(20)      default null comment 'PDF导入批次ID',
    source_file_name      varchar(255)    default null comment '来源文件名',
    source_page           int(11)         default null comment '来源PDF页码',
    parse_confidence      decimal(5,2)    default null comment '解析置信度（0-100）',
    parse_message         varchar(500)    default null comment '解析提示',
    remark                varchar(1000)   default null comment '备注',
    version               bigint(20)      default 0 comment '版本号',
    create_dept           bigint(20)      default null comment '创建部门',
    create_by             bigint(20)      default null comment '创建者',
    create_time           datetime        default null comment '创建时间',
    update_by             bigint(20)      default null comment '更新者',
    update_time           datetime        default null comment '更新时间',
    del_flag              char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_work_order_dept_date (dept_id, occur_date),
    key idx_dm_work_order_system (system_name),
    key idx_dm_work_order_batch (source_batch_id)
) engine=innodb comment='科室人工单台账';

create table if not exists dm_work_order_detail (
    id                    bigint(20)      not null comment '明细主键',
    work_order_id         bigint(20)      not null comment '人工单主记录ID',
    source_page           int(11)         default null comment '来源PDF页码',
    sequence_no           int(11)         not null comment 'PDF表格序号',
    request_dept          varchar(100)    default null comment '申请部门',
    settlement_unit       varchar(150)    default null comment '结算单位',
    project_owner         varchar(100)    default null comment '项目负责人',
    project_name          varchar(150)    default null comment '项目名称（单价表对应名称）',
    project_feature       varchar(255)    default null comment '项目特征（实用物资）',
    unit                  varchar(30)     default null comment '计量单位',
    engineering_quantity varchar(50)     default null comment '工程量',
    chinese_labor         varchar(50)     default null comment '中国人工',
    indonesia_labor       varchar(50)     default null comment '印尼人工',
    install_department    varchar(150)    default null comment '安装车间',
    install_team          varchar(150)    default null comment '安装班组',
    work_content          text            comment '工作内容',
    quantity              decimal(12,2)   not null default 1 comment '用于汇总的工单量',
    parse_message         varchar(500)   default null comment '解析提示',
    create_dept           bigint(20)      default null comment '创建部门',
    create_by             bigint(20)      default null comment '创建者',
    create_time           datetime        default null comment '创建时间',
    update_by             bigint(20)      default null comment '更新者',
    update_time           datetime        default null comment '更新时间',
    del_flag              char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    unique key uk_dm_work_order_detail_sequence (work_order_id, source_page, sequence_no),
    key idx_dm_work_order_detail_parent (work_order_id)
) engine=innodb comment='人工单PDF原始统计明细';

-- 科室负责的项目主数据。运维工作记录通过 project_id 绑定到这里，项目名称不再依赖自由文本。
create table if not exists dm_department_project (
    id                    bigint(20)      not null comment '主键',
    dept_id               bigint(20)      not null comment '所属部门ID',
    project_code          varchar(50)     default null comment '项目编码',
    project_name          varchar(150)    not null comment '项目名称',
    project_type          varchar(50)     default null comment '项目类型',
    responsible_person    varchar(100)    default null comment '负责人',
    status                varchar(20)     not null default 'ENABLED' comment '状态（ENABLED启用 DISABLED停用）',
    sort_num              int(11)         not null default 0 comment '排序号',
    remark                varchar(1000)   default null comment '备注',
    version               bigint(20)      default 0 comment '版本号',
    create_dept           bigint(20)      default null comment '创建部门',
    create_by             bigint(20)      default null comment '创建者',
    create_time           datetime        default null comment '创建时间',
    update_by             bigint(20)      default null comment '更新者',
    update_time           datetime        default null comment '更新时间',
    del_flag              char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    unique key uk_dm_department_project_name (dept_id, project_name, del_flag),
    key idx_dm_department_project_dept (dept_id, status, sort_num)
) engine=innodb comment='科室负责项目主数据';

-- 运维台账：来源于《物流系统科日常管理表》的“工作记录”表。
-- 与人工单台账（dm_work_order）分开，周报中的运维指标只读取本表及系统在线率字段。
create table if not exists dm_operation_record (
    id                    bigint(20)      not null comment '主键',
    dept_id               bigint(20)      not null comment '业务所属部门ID',
    project_id            bigint(20)      default null comment '绑定的科室项目ID',
    request_person        varchar(150)    default null comment '请求人',
    customer_unit         varchar(150)    default null comment '客户单位',
    request_role_type     varchar(100)    default null comment '请求岗位类型',
    request_time          datetime        default null comment '请求时间',
    handler               varchar(100)    default null comment '处理人',
    process_time          datetime        default null comment '处理时间',
    completion_time       datetime        default null comment '完成时间',
    response_minutes      int(11)         default null comment '响应耗时（分钟）',
    processing_minutes    int(11)         default null comment '处理耗时（分钟）',
    lunch_break           char(1)         not null default '0' comment '是否午休（1是 0否）',
    process_status        varchar(30)     not null default 'PROCESSING' comment '处理状态（PROCESSING处理中 COMPLETED已完成 CANCELLED已取消）',
    process_method        varchar(100)    default null comment '处理方式',
    submitter             varchar(100)    default null comment '提交人',
    system_name           varchar(150)    default null comment '系统/项目，用于运维结构统计',
    fault_type            varchar(100)    default null comment '故障类型，用于运维结构统计',
    business_description  text            comment '业务描述',
    solution              text            comment '解决方案',
    remark                varchar(1000)   default null comment '备注',
    source_type           varchar(20)     not null default 'MANUAL' comment '来源（MANUAL手动 EXCEL导入）',
    source_file_name      varchar(255)    default null comment '来源文件名',
    version               bigint(20)      default 0 comment '版本号',
    create_dept           bigint(20)      default null comment '创建部门',
    create_by             bigint(20)      default null comment '创建者',
    create_time           datetime        default null comment '创建时间',
    update_by             bigint(20)      default null comment '更新者',
    update_time           datetime        default null comment '更新时间',
    del_flag              char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_operation_dept_time (dept_id, request_time),
    key idx_dm_operation_status (process_status),
    key idx_dm_operation_system (system_name),
    key idx_dm_operation_project (project_id)
) engine=innodb comment='科室运维工作记录台账';

-- 系统在线率属于运维台账的系统运行指标，单独记录，避免重复写入每一条工作记录。
create table if not exists dm_operation_system (
    id                    bigint(20)      not null comment '主键',
    dept_id               bigint(20)      not null comment '业务所属部门ID',
    project_id            bigint(20)      default null comment '绑定的科室项目ID',
    stat_date             date            not null comment '统计日期',
    system_name           varchar(150)    not null comment '系统名称',
    responsible_person    varchar(100)    default null comment '负责人',
    server_name           varchar(150)    default null comment '服务器名称',
    server_ip              varchar(1000)   default null comment '服务器IP',
    online_days           decimal(10,2)   default null comment '系统在线时长（天）',
    downtime_minutes      int(11)         default null comment '系统停机时间（分钟）',
    online_rate           decimal(8,4)    default null comment '系统在线率（百分比）',
    remark                varchar(1000)   default null comment '备注',
    source_type           varchar(20)     not null default 'MANUAL' comment '来源（MANUAL手动 EXCEL导入）',
    source_file_name      varchar(255)    default null comment '来源文件名',
    version               bigint(20)      default 0 comment '版本号',
    create_dept           bigint(20)      default null comment '创建部门',
    create_by             bigint(20)      default null comment '创建者',
    create_time           datetime        default null comment '创建时间',
    update_by             bigint(20)      default null comment '更新者',
    update_time            datetime        default null comment '更新时间',
    del_flag              char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_operation_system_dept_date (dept_id, stat_date),
    key idx_dm_operation_system_name (system_name),
    key idx_dm_operation_system_project (project_id)
) engine=innodb comment='科室系统在线率台账';

create table if not exists dm_five_why (
    id                          bigint(20)      not null comment '主键',
    dept_id                     bigint(20)      not null comment '所属部门ID',
    company_dept                varchar(255)    default null comment '公司/部门/填写日期展示文本',
    employee_no                 varchar(64)     default null comment '分析人工号',
    analyst_user_id             bigint(20)      default null comment '分析人系统用户ID，用于任务统计',
    analyst_name                varchar(100)    not null comment '分析人姓名',
    analysis_date               date            not null comment '分析日期',
    problem_name                varchar(255)    not null comment '问题名称',
    problem_description         text            comment '问题描述（5W2H）',
    impact_scope                text            comment '影响范围',
    whys_json                   longtext        comment '5WHY问题与原因JSON',
    improvements_json           longtext        comment '改善措施与责任人JSON',
    before_oss_id               bigint(20)      default null comment '改善前图片OSS ID',
    after_oss_id                bigint(20)      default null comment '改善后图片OSS ID',
    effect_verification         text            comment '效果验证',
    standardization_plan        text            comment '标准化长效方案',
    standardization_execution   text            comment '标准化执行说明',
    review_status               varchar(30)     not null default 'DRAFT' comment '提案状态（DRAFT暂存 PENDING待审核 PENDING_CONFIRM待现场确认 APPROVED已通过 REJECTED未通过）',
    review_comment              varchar(1000)   default null comment '审核意见',
    reviewer_user_id            bigint(20)      default null comment '实际审核人系统用户ID',
    reviewed_at                 datetime        default null comment '审核完成时间',
    review_file_oss_id          bigint(20)      default null comment '提交审核时生成的Excel OSS ID',
    review_file_name            varchar(255)    default null comment '提交审核时生成的Excel文件名',
    revision_no                 int(11)         not null default 0 comment '提交版本号',
    submitted_at                datetime        default null comment '提交审核时间',
    submitted_by                bigint(20)      default null comment '提交审核操作人',
    confirm_comment             varchar(1000)   default null comment '现场确认意见',
    confirmer_user_id           bigint(20)      default null comment '现场确认人用户ID',
    confirmed_at                datetime        default null comment '现场确认时间',
    version                     bigint(20)      default 0 comment '版本号',
    create_dept                 bigint(20)      default null comment '创建部门',
    create_by                   bigint(20)      default null comment '创建者',
    create_time                 datetime        default null comment '创建时间',
    update_by                   bigint(20)      default null comment '更新者',
    update_time                 datetime        default null comment '更新时间',
    del_flag                    char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_five_why_dept_date (dept_id, analysis_date),
    key idx_dm_five_why_task_stat (dept_id, analyst_user_id, del_flag, analysis_date),
    key idx_dm_five_why_review (review_status)
) engine=innodb comment='科室5WHY分析记录';

-- SCORE 提案全局分类配置。所有科室共用同一套大类/小类，parent_id=0表示大类。
create table if not exists dm_score_category (
    id                  bigint(20)      not null comment '主键',
    parent_id           bigint(20)      not null default 0 comment '父分类ID，0表示提案大类',
    category_name       varchar(500)    not null comment '分类名称',
    category_level      tinyint(1)      not null default 1 comment '分类层级（1大类 2小类）',
    sort_num            int(11)         not null default 0 comment '排序号',
    status              varchar(20)     not null default 'ENABLED' comment '状态（ENABLED启用 DISABLED停用）',
    remark              varchar(500)    default null comment '备注',
    version             bigint(20)      default 0 comment '版本号',
    create_dept         bigint(20)      default null comment '创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by           bigint(20)      default null comment '更新人',
    update_time         datetime        default null comment '更新时间',
    del_flag            char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    unique key uk_dm_score_category_parent_name (parent_id, category_name, del_flag),
    key idx_dm_score_category_parent (parent_id, status, sort_num)
) engine=innodb comment='SCORE提案全局分类配置';

create table if not exists dm_score_proposal (
    id                          bigint(20)      not null comment '主键',
    dept_id                     bigint(20)      not null comment '所属部门ID',
    main_category_id            bigint(20)      default null comment '提案大类ID',
    sub_category_id             bigint(20)      default null comment '提案小类ID',
    proposer_user_id            bigint(20)      default null comment '提议人系统用户ID，用于任务统计',
    company_name                varchar(255)    default null comment '企业名称',
    team_member_user_ids        text            comment '企业参与人员用户ID JSON',
    employee_no                 varchar(64)     default null comment '提议人工号',
    proposer_name               varchar(100)    not null comment '提议者姓名',
    proposer_level              varchar(50)     default null comment '职位层级字典值（dm_score_job）',
    dept_name                   varchar(150)    default null comment '车间/部门',
    main_category               varchar(500)    default null comment '提案大类',
    sub_category                varchar(500)    default null comment '提案小类',
    problem_description         text            comment '问题描述',
    improvement_measure         text            comment '改进措施',
    implementer_supervisor      text            comment '实施人/监督人',
    implementer_user_ids        longtext        comment '实施人/监督人用户ID JSON快照',
    before_oss_id               bigint(20)      default null comment '改进前图片OSS ID',
    after_oss_id                bigint(20)      default null comment '改进后图片OSS ID',
    start_date                  date            default null comment '开始日期',
    planned_completion_date     date            default null comment '计划完成日期',
    actual_completion_date      date            default null comment '实际完成日期',
    completion_status            varchar(50)     default null comment '完成状态',
    remark                      varchar(1000)   default null comment '备注',
    review_status               varchar(30)     not null default 'DRAFT' comment '提案状态（DRAFT暂存 PENDING待审核 PENDING_CONFIRM待现场确认 APPROVED已通过 REJECTED未通过）',
    review_comment              varchar(1000)   default null comment '审核意见',
    reviewer_user_id            bigint(20)      default null comment '实际审核人系统用户ID',
    reviewed_at                 datetime        default null comment '审核完成时间',
    review_file_oss_id          bigint(20)      default null comment '提交审核时生成的Excel OSS ID',
    review_file_name            varchar(255)    default null comment '提交审核时生成的Excel文件名',
    revision_no                 int(11)         not null default 0 comment '提交版本号',
    submitted_at                datetime        default null comment '提交审核时间',
    submitted_by                bigint(20)      default null comment '提交审核操作人',
    confirm_comment              varchar(1000)   default null comment '现场确认意见',
    confirmer_user_id            bigint(20)      default null comment '现场确认人用户ID',
    confirmed_at                 datetime        default null comment '现场确认时间',
    version                     bigint(20)      default 0 comment '版本号',
    create_dept                 bigint(20)      default null comment '创建部门',
    create_by                   bigint(20)      default null comment '创建者',
    create_time                 datetime        default null comment '创建时间',
    update_by                   bigint(20)      default null comment '更新人',
    update_time                 datetime        default null comment '更新时间',
    del_flag                    char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_score_dept_date (dept_id, start_date),
    key idx_dm_score_review (review_status),
    key idx_dm_score_metric (dept_id, review_status, del_flag, confirmed_at),
    key idx_dm_score_status_stat (dept_id, del_flag, submitted_at, review_status),
    key idx_dm_score_category (main_category),
    key idx_dm_score_category_id (main_category_id, sub_category_id),
    key idx_dm_score_task_stat (dept_id, proposer_user_id, del_flag, submitted_at)
) engine=innodb comment='科室SCORE提案记录';

-- SCORE提案事件型任务：审核通过后由同一审核人继续执行现场确认。
create table if not exists dm_score_proposal_review_task (
    id                  bigint(20)      not null comment '主键',
    proposal_id         bigint(20)      not null comment 'SCORE提案ID',
    dept_id             bigint(20)      not null comment '科室ID',
    revision_no         int(11)         not null comment '提案版本号',
    stage               varchar(20)     not null comment '阶段（REVIEW审核/CONFIRM现场确认）',
    assignee_user_id    bigint(20)      not null comment '任务处理人',
    status              varchar(20)     not null default 'PENDING' comment '任务状态（PENDING待处理 COMPLETED已完成 CANCELLED已取消）',
    deadline            datetime        default null comment '处理截止时间',
    completed_by        bigint(20)      default null comment '完成操作人',
    completed_at        datetime        default null comment '完成时间',
    result              varchar(20)     default null comment '处理结果',
    comment             varchar(1000)   default null comment '处理意见',
    version             bigint(20)      default 0 comment '版本号',
    create_dept         bigint(20)      default null comment '创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by           bigint(20)      default null comment '更新者',
    update_time         datetime        default null comment '更新时间',
    del_flag            char(1)         not null default '0' comment '删除标志',
    primary key (id),
    unique key uk_dm_score_review_task (proposal_id, revision_no, stage, assignee_user_id, del_flag),
    key idx_dm_score_review_task_user (assignee_user_id, status, deadline),
    key idx_dm_score_review_task_proposal (proposal_id, revision_no, stage, status)
) engine=innodb comment='SCORE提案审核和现场确认任务';

-- 审核规则：每个科室、每种业务维护一名主审核人和一名备用审核人。
-- 审核时只允许配置中的审核人操作，避免拥有页面权限的普通成员越权审核。
create table if not exists dm_review_rule (
    id                          bigint(20)      not null comment '主键',
    dept_id                     bigint(20)      not null comment '科室ID',
    task_type                   varchar(30)     not null comment '业务类型（SCORE_PROPOSAL/FIVE_WHY）',
    reviewer_user_id            bigint(20)      not null comment '主审核人用户ID',
    backup_reviewer_user_id     bigint(20)      default null comment '备用审核人用户ID',
    effective_start             date            default null comment '生效开始日期',
    effective_end               date            default null comment '生效结束日期',
    status                      varchar(20)     not null default 'ENABLED' comment '状态（ENABLED启用 DISABLED停用）',
    remark                      varchar(500)    default null comment '备注',
    version                     bigint(20)      default 0 comment '版本号',
    create_dept                 bigint(20)      default null comment '创建部门',
    create_by                   bigint(20)      default null comment '创建者',
    create_time                 datetime        default null comment '创建时间',
    update_by                   bigint(20)      default null comment '更新者',
    update_time                 datetime        default null comment '更新时间',
    del_flag                    char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    unique key uk_dm_review_rule_dept_type (dept_id, task_type, del_flag),
    key idx_dm_review_rule_effective (dept_id, task_type, status, effective_start, effective_end)
) engine=innodb comment='科室业务审核人配置';

-- 周期任务规则：任务类型可扩展，当前内置 SCORE、5WHY、日报三类。
create table if not exists dm_department_task_rule (
    id                          bigint(20)      not null comment '主键',
    dept_id                     bigint(20)      not null comment '科室ID',
    task_name                   varchar(150)    not null comment '任务名称',
    task_type                   varchar(30)     not null comment '任务类型（SCORE_PROPOSAL/FIVE_WHY/DAILY_REPORT）',
    cycle_type                  varchar(20)     not null default 'MONTH' comment '周期（日报为DAY；其他任务为WEEK/MONTH/QUARTER）',
    required_count              int(11)         not null default 1 comment '周期内要求完成次数',
    deadline_day                int(11)         not null default 0 comment '周期内截止日序号，0表示周期最后一天',
    deadline_time               time            not null default '18:00:00' comment '截止时间',
    count_mode                  varchar(20)     not null default 'SUBMITTED' comment '统计口径（SUBMITTED提交 APPROVED审核通过）',
    remind_hours                int(11)         not null default 24 comment '截止前多少小时提醒',
    effective_start             date            default null comment '生效开始日期',
    effective_end               date            default null comment '生效结束日期',
    status                      varchar(20)     not null default 'ENABLED' comment '状态（ENABLED启用 DISABLED停用）',
    remark                      varchar(1000)   default null comment '备注',
    version                     bigint(20)      default 0 comment '版本号',
    create_dept                 bigint(20)      default null comment '创建部门',
    create_by                   bigint(20)      default null comment '创建者',
    create_time                 datetime        default null comment '创建时间',
    update_by                   bigint(20)      default null comment '更新者',
    update_time                 datetime        default null comment '更新时间',
    del_flag                    char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_task_rule_dept_status (dept_id, status, task_type),
    key idx_dm_task_rule_dept_active (dept_id, status, del_flag, task_type)
) engine=innodb comment='科室周期任务规则';

-- 任务分配：只有被分配到规则的成员才产生该任务要求。
create table if not exists dm_department_task_assignment (
    id                          bigint(20)      not null comment '主键',
    rule_id                     bigint(20)      not null comment '任务规则ID',
    dept_id                     bigint(20)      not null comment '科室ID',
    user_id                     bigint(20)      not null comment '被分配成员ID',
    effective_start             date            default null comment '分配生效开始日期',
    effective_end               date            default null comment '分配生效结束日期',
    work_days                   varchar(20)     default null comment '日报任务个人工作日，ISO星期编号逗号分隔',
    reminder_time               time            default null comment '日报任务个人提醒时间',
    status                      varchar(20)     not null default 'ENABLED' comment '状态（ENABLED启用 DISABLED停用）',
    remark                      varchar(500)    default null comment '备注',
    version                     bigint(20)      default 0 comment '版本号',
    create_dept                 bigint(20)      default null comment '创建部门',
    create_by                   bigint(20)      default null comment '创建者',
    create_time                 datetime        default null comment '创建时间',
    update_by                   bigint(20)      default null comment '更新者',
    update_time                 datetime        default null comment '更新时间',
    del_flag                    char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    unique key uk_dm_task_assignment_rule_user (rule_id, user_id, del_flag),
    key idx_dm_task_assignment_user (dept_id, user_id, status),
    key idx_dm_task_assignment_rule_status (rule_id, status, del_flag),
    key idx_dm_task_assignment_user_active (user_id, dept_id, status, del_flag, effective_start, effective_end),
    key idx_dm_task_assignment_dept_active (dept_id, status, del_flag, effective_start, effective_end, user_id)
) engine=innodb comment='科室周期任务成员分配';

-- 提醒去重记录，避免定时任务重复给同一成员发送相同周期提醒。
create table if not exists dm_department_task_reminder_log (
    id                          bigint(20)      not null comment '主键',
    rule_id                     bigint(20)      not null comment '任务规则ID',
    user_id                     bigint(20)      not null comment '接收人ID',
    period_start                date            not null comment '任务周期开始',
    reminder_type               varchar(20)     not null comment '提醒类型（BEFORE/OVERDUE）',
    create_time                 datetime        default null comment '发送时间',
    primary key (id),
    unique key uk_dm_task_reminder (rule_id, user_id, period_start, reminder_type)
) engine=innodb comment='科室周期任务提醒记录';

-- 任务周期实例：任务定义在具体成员和周期上的落地记录。
create table if not exists dm_department_task_instance (
    id                  bigint(20)      not null comment '主键',
    rule_id             bigint(20)      not null comment '任务定义ID',
    dept_id             bigint(20)      not null comment '科室ID',
    user_id             bigint(20)      not null comment '成员ID',
    period_start        date            not null comment '周期开始日期',
    period_end          date            not null comment '周期结束日期',
    deadline            datetime        not null comment '本周期截止时间',
    required_count      int(11)         not null default 1 comment '本周期要求次数',
    completed_count     int(11)         not null default 0 comment '本周期完成次数',
    status              varchar(20)     not null default 'NOT_STARTED' comment '实例状态',
    generated_at        datetime        not null default current_timestamp comment '实例生成时间',
    completed_at        datetime        default null comment '完成时间',
    version             bigint(20)      default 0 comment '版本号',
    create_dept         bigint(20)      default null comment '创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by           bigint(20)      default null comment '更新者',
    update_time         datetime        default null comment '更新时间',
    del_flag            char(1)         not null default '0' comment '删除标志',
    primary key (id),
    unique key uk_dm_task_instance_period (rule_id, user_id, period_start, del_flag),
    key idx_dm_task_instance_dept_period (dept_id, period_start, status)
) engine=innodb comment='科室任务周期实例';

-- 任务完成记录：保存实例对应的 SCORE、5WHY、日报来源记录，便于审计和扩展任务类型。
create table if not exists dm_department_task_completion (
    id                  bigint(20)      not null comment '主键',
    instance_id         bigint(20)      not null comment '周期实例ID',
    task_type           varchar(30)     not null comment '任务类型',
    source_id           bigint(20)      not null comment '来源业务记录ID',
    completed_at        datetime        not null default current_timestamp comment '完成时间',
    create_dept         bigint(20)      default null comment '创建部门',
    create_by           bigint(20)      default null comment '创建者',
    create_time         datetime        default null comment '创建时间',
    update_by           bigint(20)      default null comment '更新者',
    update_time         datetime        default null comment '更新时间',
    del_flag            char(1)         not null default '0' comment '删除标志',
    primary key (id),
    unique key uk_dm_task_completion_source (instance_id, task_type, source_id, del_flag),
    key idx_dm_task_completion_instance (instance_id, completed_at)
) engine=innodb comment='科室任务周期完成记录';

-- 菜单与按钮权限。日报不设置审核，也排除签字、固化清单和推广清单。
insert ignore into sys_menu values(1761400000000003000, '运营管理', 0, 6, 'department', null, '', 'N', 'Y', 'M', '0', '0', '', 'post', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '运营管理目录');
update sys_menu set menu_name = '运营管理', remark = '运营管理目录' where menu_id = 1761400000000003000;
insert ignore into sys_menu values(1761400000000003001, '日报管理', 1761400000000003000, 1, 'dailyReport', 'department/dailyReport/index', '', 'N', 'Y', 'C', '0', '0', 'department:dailyReport:list', 'documentation', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '科室日报管理');
insert ignore into sys_menu values(1761400000000003010, '日报查询', 1761400000000003001, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:dailyReport:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003011, '日报新增', 1761400000000003001, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:dailyReport:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003012, '日报修改', 1761400000000003001, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:dailyReport:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003013, '日报删除', 1761400000000003001, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:dailyReport:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003014, '日报导入', 1761400000000003001, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:dailyReport:import', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003015, '日报导出', 1761400000000003001, 6, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:dailyReport:export', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003017, '本部门查看', 1761400000000003001, 8, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:dailyReport:viewDept', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');

insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003000);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003001);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003010);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003011);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003012);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003013);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003014);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003015);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003017);

insert ignore into sys_menu values(1761400000000003002, '人员档案', 1761400000000003000, 2, 'person', 'department/person/index', '', 'N', 'Y', 'C', '0', '0', 'department:person:list', 'peoples', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '科室人员业务档案');
insert ignore into sys_menu values(1761400000000003020, '人员查询', 1761400000000003002, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:person:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003021, '人员新增', 1761400000000003002, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:person:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003022, '人员修改', 1761400000000003002, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:person:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003023, '人员删除', 1761400000000003002, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:person:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003024, '本部门查看', 1761400000000003002, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:person:viewDept', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003025, '人员导入', 1761400000000003002, 6, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:person:import', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003026, '人员导出', 1761400000000003002, 7, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:person:export', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003002);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003020);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003021);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003022);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003023);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003024);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003025);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003026);

insert ignore into sys_menu values(1761400000000003003, '周报管理', 1761400000000003000, 3, 'weeklyReport', 'department/weeklyReport/index', '', 'N', 'Y', 'C', '0', '0', 'department:weeklyReport:list', 'chart', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '科室周报汇总与PPT生成');
insert ignore into sys_menu values(1761400000000003030, '周报查询', 1761400000000003003, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:weeklyReport:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003031, '周报汇总', 1761400000000003003, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:weeklyReport:summary', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003032, '周报生成', 1761400000000003003, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:weeklyReport:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003033, '周报导出', 1761400000000003003, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:weeklyReport:export', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003034, '本部门查看', 1761400000000003003, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:weeklyReport:viewDept', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003035, '周报删除', 1761400000000003003, 6, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:weeklyReport:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003003);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003030);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003031);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003032);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003033);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003034);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003035);

insert ignore into sys_menu values(1761400000000003004, '人工单台账', 1761400000000003000, 4, 'workOrder', 'department/workOrder/index', '', 'N', 'Y', 'C', '0', '0', 'department:workOrder:list', 'clipboard', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, 'PDF导入与手动维护人工单台账');
insert ignore into sys_menu values(1761400000000003040, '人工单查询', 1761400000000003004, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:workOrder:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003041, '人工单新增', 1761400000000003004, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:workOrder:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003042, '人工单修改', 1761400000000003004, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:workOrder:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003043, '人工单删除', 1761400000000003004, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:workOrder:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003044, '人工单PDF导入', 1761400000000003004, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:workOrder:import', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003045, '人工单导出', 1761400000000003004, 6, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:workOrder:export', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003046, '本部门查看', 1761400000000003004, 7, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:workOrder:viewDept', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003004);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003040);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003041);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003042);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003043);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003044);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003045);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003046);

insert ignore into sys_menu values(1761400000000003005, '运维台账', 1761400000000003000, 5, 'operationLedger', 'department/operationLedger/index', '', 'N', 'Y', 'C', '0', '0', 'department:operationLedger:list', 'monitor', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '工作记录与系统在线率运维台账');
insert ignore into sys_menu values(1761400000000003070, '运维台账查询', 1761400000000003005, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:operationLedger:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003071, '运维台账新增', 1761400000000003005, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:operationLedger:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003072, '运维台账修改', 1761400000000003005, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:operationLedger:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003073, '运维台账删除', 1761400000000003005, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:operationLedger:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003074, '运维记录导入', 1761400000000003005, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:operationLedger:import', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003075, '运维台账导出', 1761400000000003005, 6, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:operationLedger:export', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003076, '本部门查看', 1761400000000003005, 7, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:operationLedger:viewDept', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003005);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003070);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003071);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003072);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003073);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003074);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003075);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003076);

insert ignore into sys_menu values(1761400000000003006, '项目管理', 1761400000000003000, 8, 'project', 'department/project/index', '', 'N', 'Y', 'C', '0', '0', 'department:project:list', 'component', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '科室负责项目主数据');
insert ignore into sys_menu values(1761400000000003080, '项目查询', 1761400000000003006, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:project:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003081, '项目新增', 1761400000000003006, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:project:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003082, '项目修改', 1761400000000003006, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:project:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003083, '项目删除', 1761400000000003006, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:project:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003084, '本部门查看', 1761400000000003006, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:project:viewDept', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003006);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003080);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003081);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003082);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003083);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003084);

insert ignore into sys_menu values(1761400000000003110, '资料管理', 1761400000000003000, 9, 'document', 'department/document/index', '', 'N', 'Y', 'C', '0', '0', 'department:document:list', 'documentation', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '科室资料、项目资料与版本管理');
insert ignore into sys_menu values(1761400000000003111, '资料查询', 1761400000000003110, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:document:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003112, '资料上传', 1761400000000003110, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:document:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003113, '资料修改', 1761400000000003110, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:document:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003114, '资料删除', 1761400000000003110, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:document:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003115, '资料恢复', 1761400000000003110, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:document:restore', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003116, '资料下载', 1761400000000003110, 6, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:document:download', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003110);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003111);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003112);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003113);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003114);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003115);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003116);
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003110 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003111 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003112 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003113 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003114 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003115 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003116 from sys_role_menu where menu_id = 1761400000000003000;

insert ignore into sys_menu values(1761400000000003122, '分类新增', 1761400000000003110, 7, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:documentCategory:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003123, '分类修改', 1761400000000003110, 8, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:documentCategory:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003124, '分类删除', 1761400000000003110, 9, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:documentCategory:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003122);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003123);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003124);
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003122 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003123 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003124 from sys_role_menu where menu_id = 1761400000000003000;

insert ignore into sys_menu values(1761400000000003050, '5WHY管理', 1761400000000003000, 6, 'fiveWhy', 'department/fiveWhy/index', '', 'N', 'Y', 'C', '0', '0', 'department:fiveWhy:list', 'question', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '5WHY分析记录与DOCX生成');
insert ignore into sys_menu values(1761400000000003051, '5WHY查询', 1761400000000003050, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:fiveWhy:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003052, '5WHY新增', 1761400000000003050, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:fiveWhy:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003053, '5WHY修改', 1761400000000003050, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:fiveWhy:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003054, '5WHY删除', 1761400000000003050, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:fiveWhy:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003055, '5WHY审核', 1761400000000003050, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:fiveWhy:review', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003056, '5WHY导出', 1761400000000003050, 6, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:fiveWhy:export', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003050);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003051);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003052);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003053);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003054);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003055);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003056);

insert ignore into sys_menu values(1761400000000003060, 'SCORE提案', 1761400000000003000, 7, 'scoreProposal', '', '', 'N', 'Y', 'M', '0', '0', '', 'tool', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, 'SCORE提案目录');
insert ignore into sys_menu values(1761400000000003091, 'SCORE提案管理', 1761400000000003060, 1, 'proposal', 'department/scoreProposal/index', '', 'N', 'Y', 'C', '0', '0', 'department:scoreProposal:list', 'edit', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, 'SCORE提案管理与模板生成');
insert ignore into sys_menu values(1761400000000003061, 'SCORE查询', 1761400000000003091, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:scoreProposal:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003062, 'SCORE新增', 1761400000000003091, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:scoreProposal:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003063, 'SCORE修改', 1761400000000003091, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:scoreProposal:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003064, 'SCORE删除', 1761400000000003091, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:scoreProposal:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003065, 'SCORE审核', 1761400000000003091, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:scoreProposal:review', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003066, 'SCORE导出', 1761400000000003091, 6, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:scoreProposal:export', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003091);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003060);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003061);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003062);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003063);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003064);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003065);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003066);

insert ignore into sys_menu values(1761400000000003067, 'SCORE分类配置', 1761400000000003060, 2, 'scoreCategory', 'department/scoreCategory/index', '', 'N', 'Y', 'C', '0', '0', 'department:scoreCategory:list', 'category', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, 'SCORE提案全局大类与小类配置');
insert ignore into sys_menu values(1761400000000003077, 'SCORE分类查询', 1761400000000003067, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:scoreCategory:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003078, 'SCORE分类新增', 1761400000000003067, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:scoreCategory:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003079, 'SCORE分类修改', 1761400000000003067, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:scoreCategory:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003090, 'SCORE分类删除', 1761400000000003067, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:scoreCategory:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003067);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003077);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003078);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003079);
-- 科室审核人及周期任务配置：任务规则只对明确分配的成员生效，未分配成员不产生待办。
insert ignore into sys_menu values(1761400000000003100, '任务管理', 1761400000000003000, 11, 'task', 'department/task/index', '', 'N', 'Y', 'C', '0', '0', 'department:task:list', 'my-task', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '科室周期任务与业务审核人配置');
insert ignore into sys_menu values(1761400000000003101, '任务规则查询', 1761400000000003100, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:task:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003102, '任务规则新增', 1761400000000003100, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:task:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003103, '任务规则修改', 1761400000000003100, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:task:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003104, '任务规则删除', 1761400000000003100, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:task:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003105, '审核人配置', 1761400000000003100, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:task:reviewConfig', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003100);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003101);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003102);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003103);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003104);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003105);

insert ignore into sys_menu values(1761400000000003130, '科室配置', 1761400000000003000, 12, 'departmentConfig', 'department/config/index', '', 'N', 'Y', 'C', '0', '0', 'department:department:list', 'company', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '按系统部门配置业务科室并自动同步正式成员');
insert ignore into sys_menu values(1761400000000003131, '科室查询', 1761400000000003130, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:department:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003132, '科室新增', 1761400000000003130, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:department:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003133, '科室修改', 1761400000000003130, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:department:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003134, '科室停用', 1761400000000003130, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:department:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003135, '科室数据查看', 1761400000000003130, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:department:viewDept', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003130);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003131);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003132);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003133);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003134);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003135);
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003130 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003131 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003132 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003133 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003134 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003135 from sys_role_menu where menu_id = 1761400000000003000;

-- 泛微通用审批中心：仅用于非 SCORE、非 5WHY 的外部 OA 业务。
create table if not exists dm_oa_application (
    id                  bigint          not null comment '主键',
    application_no      varchar(40)     not null comment '申请编号',
    business_type       varchar(64)     not null comment '业务类型',
    source_module       varchar(64)     null comment '来源业务模块',
    business_id         varchar(100)    null comment '来源业务单据 ID',
    business_no         varchar(100)    null comment '来源业务单号',
    title               varchar(200)    not null comment '申请标题',
    content             varchar(5000)   not null comment '申请内容',
    urgency             varchar(20)     not null default 'NORMAL' comment '紧急程度',
    form_data_json      text            null comment '业务表单扩展数据 JSON',
    applicant_user_id   bigint          not null comment '申请人本地用户 ID',
    applicant_name      varchar(100)    null comment '申请人名称快照',
    dept_id             bigint          null comment '申请部门 ID',
    company_id          bigint          null comment '申请使用公司 ID',
    process_type        varchar(20)     not null default 'SEQUENTIAL' comment 'SEQUENTIAL/COUNTERSIGN/MIXED',
    approval_plan_id    bigint          null comment '本次使用的审批方案 ID，临时指定时为空',
    approval_mode       varchar(20)     not null default 'AUTO_RULE' comment '审批方式：AUTO_RULE/PLAN/MANUAL',
    workflow_config_id  bigint          not null comment '泛微流程配置 ID',
    status              varchar(30)     not null default 'DRAFT' comment '申请状态',
    submitted_at        datetime        null comment '提交时间',
    create_dept         bigint          null,
    create_by           bigint          null,
    create_time         datetime        null,
    update_by           bigint          null,
    update_time         datetime        null,
    version             bigint          not null default 1,
    del_flag            char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_application_no (application_no),
    key idx_dm_oa_application_applicant (applicant_user_id, status, del_flag),
    key idx_dm_oa_application_business (business_type, status, del_flag),
    key idx_dm_oa_application_workflow (workflow_config_id),
    key idx_dm_oa_application_approval (workflow_config_id, business_type, approval_plan_id, status, del_flag)
) engine=innodb comment='泛微通用审批申请';

create table if not exists dm_oa_application_dept (
    id              bigint          not null comment '主键',
    application_id  bigint          not null comment '审批申请 ID',
    dept_id         bigint          not null comment '申请适用部门，本地泛微组织 ID',
    dept_name       varchar(200)    null comment '部门名称快照',
    sort_no         int             not null default 0 comment '部门顺序',
    create_dept     bigint          null,
    create_by       bigint          null,
    create_time     datetime        null,
    update_by       bigint          null,
    update_time     datetime        null,
    del_flag        char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_application_dept (application_id, dept_id, del_flag),
    key idx_dm_oa_application_dept (application_id, sort_no, del_flag)
) engine=innodb comment='泛微审批申请适用部门快照';

create table if not exists dm_oa_department_approval (
    id                  bigint          not null comment '主键',
    workflow_config_id  bigint          not null comment '泛微流程配置 ID',
    business_type       varchar(64)     not null comment '业务类型',
    source_module       varchar(64)     null comment '来源业务模块；为空表示该业务下通用方案',
    business_dept_id    bigint          null comment '业务需要发起泛微流程的归属组织；为空表示全组织通用',
    plan_name           varchar(100)    not null comment '审批方案名称',
    match_condition_json mediumtext     null comment '自动匹配条件 JSON；为空表示通用方案',
    priority            int             not null default 0 comment '自动匹配优先级',
    status              varchar(20)     not null default 'ENABLED' comment 'ENABLED/DISABLED',
    remark              varchar(1000)   null,
    create_dept         bigint          null,
    create_by           bigint          null,
    create_time         datetime        null,
    update_by           bigint          null,
    update_time         datetime        null,
    version             bigint          not null default 1,
    del_flag            char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_approval_plan_name (workflow_config_id, business_type, source_module, business_dept_id, plan_name, del_flag),
    key idx_dm_oa_approval_plan_match (workflow_config_id, business_type, source_module, business_dept_id, priority, status, del_flag)
) engine=innodb comment='泛微审批方案';

create table if not exists dm_oa_approval_plan_user (
    id                bigint          not null comment '主键',
    approval_id       bigint          not null comment '审批方案 ID',
    local_user_id     bigint          not null comment '本地泛微同步用户 ID',
    stage_code        varchar(32)     not null default 'APPROVAL' comment '审批阶段：APPROVAL/LEVEL_1/COUNTERSIGN/LEVEL_3/COPY',
    stage_name        varchar(100)    null comment '审批阶段名称',
    stage_mode        varchar(20)     null comment '阶段方式：SEQUENTIAL/COUNTERSIGN',
    participant_role  varchar(20)     not null default 'APPROVER' comment 'APPROVER/COPY',
    sort_no           int             not null default 0 comment '同类人员顺序',
    create_dept       bigint          null,
    create_by         bigint          null,
    create_time       datetime        null,
    update_by         bigint          null,
    update_time       datetime        null,
    del_flag          char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_dept_approval_user (approval_id, local_user_id, stage_code, participant_role, del_flag),
    key idx_dm_oa_dept_approval_user (approval_id, stage_code, participant_role, sort_no, del_flag)
) engine=innodb comment='泛微审批方案用户';

create table if not exists dm_oa_process_instance (
    id                    bigint          not null comment '主键',
    application_id        bigint          not null comment '本地申请 ID',
    business_type         varchar(64)     null comment '流程分类，可空；流程不绑定具体业务',
    business_id           varchar(100)    null comment '业务单据 ID',
    business_no           varchar(100)    null comment '业务单号',
    source_module         varchar(64)     null comment '来源业务模块',
    business_title        varchar(200)    null comment '业务标题快照',
    workflow_config_id    bigint          not null comment '流程配置 ID',
    workflow_id           varchar(64)     not null comment '泛微 workflowId',
    oa_request_id         varchar(64)     null comment '泛微 requestId',
    applicant_user_id     bigint          not null comment '本地申请人 ID',
    applicant_oa_user_id  varchar(64)     not null comment '申请人泛微用户 ID',
    local_status          varchar(30)     not null comment '本地标准状态',
    oa_status             varchar(100)    null comment '泛微原始状态值',
    oa_status_raw         text            null comment '泛微原始响应摘要',
    request_name          varchar(250)    null comment '泛微请求名称',
    submitted_at          datetime        null,
    completed_at          datetime        null,
    last_sync_at          datetime        null,
    fail_reason           varchar(2000)   null,
    retry_count           int             not null default 0,
    idempotency_key       varchar(64)     not null,
    config_snapshot_json  text            null comment '提交时的流程配置快照',
    oa_link               varchar(1000)   null,
    create_dept           bigint          null,
    create_by             bigint          null,
    create_time           datetime        null,
    update_by             bigint          null,
    update_time           datetime        null,
    version               bigint          not null default 1,
    del_flag              char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_process_application (application_id, del_flag),
    unique key uk_dm_oa_process_idempotency (idempotency_key),
    key idx_dm_oa_process_request (oa_request_id),
    key idx_dm_oa_process_status (local_status, last_sync_at),
    key idx_dm_oa_process_applicant (applicant_user_id)
) engine=innodb comment='泛微审批流程实例';

create table if not exists dm_oa_workflow_config (
    id                    bigint          not null comment '主键',
    business_type         varchar(64)     null comment '流程分类，可空；流程不绑定具体业务',
    workflow_id           varchar(64)     not null comment '泛微 workflowId',
    workflow_name         varchar(100)    not null comment '页面显示的可选流程名称',
    process_type          varchar(20)     not null default 'SEQUENTIAL' comment '可选流程模式：SEQUENTIAL/COUNTERSIGN/MIXED',
    source_workflow_name  varchar(100)    null comment '原始泛微流程名称',
    request_name_template varchar(200)    null comment '请求名称模板',
    field_mapping_json    text            null comment '字段映射 JSON',
    status                varchar(20)     not null default 'ENABLED',
    remark                varchar(1000)   null,
    create_dept           bigint          null,
    create_by             bigint          null,
    create_time           datetime        null,
    update_by             bigint          null,
    update_time           datetime        null,
    version               bigint          not null default 1,
    del_flag              char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_workflow_option (workflow_id, process_type, del_flag),
    key idx_dm_oa_workflow_status (status, del_flag)
) engine=innodb comment='泛微流程配置';

create table if not exists dm_oa_business_type (
    id              bigint          not null comment '主键',
    business_type   varchar(64)     not null comment '稳定的业务类型标识，用于审批方案匹配',
    business_name   varchar(100)    not null comment '业务类型展示名称',
    status          varchar(20)     not null default 'ENABLED' comment 'ENABLED/DISABLED',
    remark          varchar(1000)   null comment '备注',
    create_dept     bigint          null,
    create_by       bigint          null,
    create_time     datetime        null,
    update_by       bigint          null,
    update_time     datetime        null,
    version         bigint          not null default 1,
    del_flag        char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_business_type (business_type, del_flag),
    key idx_dm_oa_business_type_status (status, del_flag)
) engine=innodb comment='泛微审批业务类型配置';

create table if not exists dm_oa_form_workflow (
    id                    bigint          not null comment '主键',
    workflow_id           varchar(64)     not null comment '泛微 workflowId',
    form_name             varchar(100)    not null comment '泛微表单名称',
    request_name_template varchar(200)    null comment '请求名称模板',
    field_mapping_json    text            null comment '公用字段映射 JSON',
    specific_field_mapping_json text      null comment '表单专属字段映射 JSON',
    field_schema_json     text            null comment '可视化表单字段定义及泛微字段映射 JSON',
    status                varchar(20)     not null default 'ENABLED' comment 'ENABLED/DISABLED',
    remark                varchar(1000)   null comment '备注',
    create_dept           bigint          null,
    create_by             bigint          null,
    create_time           datetime        null,
    update_by             bigint          null,
    update_time           datetime        null,
    version               bigint          not null default 1,
    del_flag              char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_form_workflow_id (workflow_id, del_flag),
    key idx_dm_oa_form_workflow_status (status, del_flag)
) engine=innodb comment='泛微可复用表单配置';

create table if not exists dm_oa_workflow_option (
    id              bigint          not null comment '主键',
    option_code     varchar(64)     not null comment '系统审批方式编码',
    option_name     varchar(100)    not null comment '审批方式展示名称',
    process_type    varchar(20)     null comment '历史兼容字段，不参与泛微提交',
    participant_mapping_json text   null comment '审批节点字段映射 JSON',
    sort_no         int             not null default 0 comment '显示顺序',
    status          varchar(20)     not null default 'ENABLED' comment 'ENABLED/DISABLED',
    remark          varchar(1000)   null comment '备注',
    create_dept     bigint          null,
    create_by       bigint          null,
    create_time     datetime        null,
    update_by       bigint          null,
    update_time     datetime        null,
    version         bigint          not null default 1,
    del_flag        char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_workflow_option_code (option_code, del_flag),
    key idx_dm_oa_workflow_option_status (status, del_flag)
) engine=innodb comment='泛微通用审批方式选项';

insert into dm_oa_form_workflow (id, workflow_id, form_name, request_name_template, field_mapping_json, specific_field_mapping_json, field_schema_json, status, remark, create_time, version, del_flag)
values (1761400000000003401, '645025', '公用审批（部门）', '{formName}-{title}',
        '{"titleField":"wjmc","contentField":"spxq","applicantField":"tbr","applicantDateField":"tbrq","approvalModeField":"splcfs","copyField":"csr","imageAttachmentField":"tp","fileAttachmentField":"fj"}', '{}',
        '{"version":1,"fields":[{"key":"title","label":"标题","oaFieldCode":"wjmc","controlType":"TEXT","semanticType":"TITLE","required":true,"sortNo":1},{"key":"content","label":"审批详情","oaFieldCode":"spxq","controlType":"TEXTAREA","semanticType":"CONTENT","required":true,"sortNo":2},{"key":"applicant","label":"申请人","oaFieldCode":"tbr","controlType":"USER_SINGLE","semanticType":"APPLICANT","required":true,"sortNo":3},{"key":"applicantDate","label":"申请时间","oaFieldCode":"tbrq","controlType":"DATETIME","semanticType":"APPLICANT_DATE","required":false,"sortNo":4},{"key":"approvalMode","label":"审批流程方式","oaFieldCode":"splcfs","controlType":"SELECT","semanticType":"APPROVAL_MODE","required":true,"sortNo":5,"options":[{"label":"依次签","optionCode":"0","oaValue":"0"},{"label":"会签","optionCode":"1","oaValue":"1"},{"label":"或签","optionCode":"15","oaValue":"15"},{"label":"依次签---或签","optionCode":"14","oaValue":"14"},{"label":"依次签---会签","optionCode":"2","oaValue":"2"},{"label":"会签---依次签","optionCode":"3","oaValue":"3"},{"label":"依次签---会签---会签","optionCode":"4","oaValue":"4"},{"label":"依次签---会签---依次签","optionCode":"5","oaValue":"5"},{"label":"会签---会签---依次签","optionCode":"9","oaValue":"9"},{"label":"主任会签---经理会签---分管领导会签","optionCode":"6","oaValue":"6"},{"label":"主任会签---经理会签---分管领导会签---公司领导","optionCode":"7","oaValue":"7"},{"label":"依次签---会签---会签---公司领导签","optionCode":"8","oaValue":"8"},{"label":"依次签---会签---会签---依次签","optionCode":"10","oaValue":"10"},{"label":"依次签---依次签---会签---依次签","optionCode":"11","oaValue":"11"},{"label":"依次签---会签---会签---会签---依次签","optionCode":"12","oaValue":"12"},{"label":"依次签---会签---会签---会签","optionCode":"13","oaValue":"13"}]},{"key":"copy","label":"抄送人","oaFieldCode":"csr","controlType":"USER_MULTI","semanticType":"COPY","multiple":true,"required":false,"sortNo":6},{"key":"image","label":"图片","oaFieldCode":"tp","controlType":"IMAGE","semanticType":"IMAGE","required":false,"sortNo":7},{"key":"attachment","label":"附件","oaFieldCode":"fj","controlType":"FILE","semanticType":"ATTACHMENT","required":false,"sortNo":8}]}' ,
        'ENABLED', '测试环境泛微表单 workflowId=645025', now(), 1, '0')
on duplicate key update
    form_name = values(form_name),
    field_mapping_json = values(field_mapping_json),
    specific_field_mapping_json = values(specific_field_mapping_json),
    field_schema_json = values(field_schema_json),
    status = values(status),
    remark = values(remark);

insert into dm_oa_form_workflow (
    id, workflow_id, form_name, request_name_template, field_mapping_json,
    specific_field_mapping_json, field_schema_json, status, remark, create_time, version, del_flag
)
values (
    1761400000000003402, '724025', '公用审批（智慧物流）', '{formName}-{title}',
    '{"titleField":"wjmc","contentField":"spxq","applicantField":"tbr","applicantDateField":"tbrq","approvalModeField":"splcfs","copyField":"csr","imageAttachmentField":"tp","fileAttachmentField":"fj"}', '{}',
    '{"version":1,"fields":[{"key":"title","label":"文件名称","oaFieldCode":"wjmc","controlType":"TEXT","semanticType":"TITLE","required":false,"sortNo":1},{"key":"content","label":"审批详情","oaFieldCode":"spxq","controlType":"TEXTAREA","semanticType":"CONTENT","required":true,"sortNo":2},{"key":"applicant","label":"申请人","oaFieldCode":"tbr","controlType":"USER_SINGLE","semanticType":"APPLICANT","required":true,"sortNo":3},{"key":"applicantDate","label":"申请时间","oaFieldCode":"tbrq","controlType":"DATETIME","semanticType":"APPLICANT_DATE","required":false,"sortNo":4},{"key":"approvalMode","label":"审批流程方式","oaFieldCode":"splcfs","controlType":"SELECT","semanticType":"APPROVAL_MODE","required":true,"sortNo":5,"options":[{"label":"依次签","optionCode":"0","oaValue":"0"},{"label":"会签","optionCode":"1","oaValue":"1"},{"label":"或签","optionCode":"15","oaValue":"15"},{"label":"依次签---或签","optionCode":"14","oaValue":"14"},{"label":"依次签---会签","optionCode":"2","oaValue":"2"},{"label":"会签---依次签","optionCode":"3","oaValue":"3"},{"label":"依次签---会签---会签","optionCode":"4","oaValue":"4"},{"label":"依次签---会签---依次签","optionCode":"5","oaValue":"5"},{"label":"会签---会签---依次签","optionCode":"9","oaValue":"9"},{"label":"主任会签---经理会签---分管领导会签","optionCode":"6","oaValue":"6"},{"label":"主任会签---经理会签---分管领导会签---公司领导","optionCode":"7","oaValue":"7"},{"label":"依次签---会签---会签---公司领导签","optionCode":"8","oaValue":"8"},{"label":"依次签---会签---会签---依次签","optionCode":"10","oaValue":"10"},{"label":"依次签---依次签---会签---依次签","optionCode":"11","oaValue":"11"},{"label":"依次签---会签---会签---会签---依次签","optionCode":"12","oaValue":"12"},{"label":"依次签---会签---会签---会签","optionCode":"13","oaValue":"13"}]},{"key":"copy","label":"抄送人","oaFieldCode":"csr","controlType":"USER_MULTI","semanticType":"COPY","multiple":true,"required":false,"sortNo":6},{"key":"image","label":"图片","oaFieldCode":"tp","controlType":"IMAGE","semanticType":"IMAGE","required":false,"sortNo":7},{"key":"attachment","label":"附件","oaFieldCode":"fj","controlType":"FILE","semanticType":"ATTACHMENT","required":false,"sortNo":8}]}' ,
    'ENABLED', '浏览器读取：workflowId=724025；创建页表单标题为“公用审批（部门）”，入口名称为“公用审批（智慧物流）”', now(), 1, '0'
)
on duplicate key update
    form_name = values(form_name), request_name_template = values(request_name_template),
    field_mapping_json = values(field_mapping_json), specific_field_mapping_json = values(specific_field_mapping_json),
    field_schema_json = values(field_schema_json), status = values(status), remark = values(remark);

insert into dm_oa_form_workflow (
    id, workflow_id, form_name, request_name_template, field_mapping_json,
    specific_field_mapping_json, field_schema_json, status, remark, create_time, version, del_flag
)
values (
    1761400000000003403, '721603', 'TEI青耀电气结算单', '{formName}-{title}',
    '{"titleField":"wjmc","contentField":"spsx","applicantField":"tbr","applicantDateField":"tbsj","approvalModeField":"splcfs","copyField":"csrmd","fileAttachmentField":"fj"}', '{"formFields":{"settlementDepartment":"jsbm","approvalAmountUsd":"spjedx"}}',
    '{"version":1,"fields":[{"key":"title","label":"文件名称","oaFieldCode":"wjmc","controlType":"TEXT","semanticType":"TITLE","required":false,"sortNo":1},{"key":"settlementDepartment","label":"结算部门","oaFieldCode":"jsbm","controlType":"TEXT","semanticType":"SPECIFIC","required":false,"sortNo":2},{"key":"approvalAmountUsd","label":"审批金额（美元）","oaFieldCode":"spjedx","controlType":"NUMBER","semanticType":"SPECIFIC","required":false,"placeholder":"请输入美元金额","sortNo":3},{"key":"content","label":"审批事项","oaFieldCode":"spsx","controlType":"TEXTAREA","semanticType":"CONTENT","required":true,"sortNo":4},{"key":"applicant","label":"申请人","oaFieldCode":"tbr","controlType":"USER_SINGLE","semanticType":"APPLICANT","required":true,"sortNo":5},{"key":"applicantDate","label":"申请时间","oaFieldCode":"tbsj","controlType":"DATETIME","semanticType":"APPLICANT_DATE","required":false,"sortNo":6},{"key":"approvalMode","label":"审批流程方式","oaFieldCode":"splcfs","controlType":"SELECT","semanticType":"APPROVAL_MODE","required":true,"sortNo":7,"options":[{"label":"依次签","optionCode":"0","oaValue":"0"},{"label":"会签","optionCode":"1","oaValue":"1"},{"label":"依次签---依次签","optionCode":"11","oaValue":"11"},{"label":"依次签---会签","optionCode":"2","oaValue":"2"},{"label":"会签---依次签","optionCode":"3","oaValue":"3"},{"label":"依次签---会签---会签","optionCode":"4","oaValue":"4"},{"label":"依次签---会签---依次签","optionCode":"5","oaValue":"5"},{"label":"会签---会签---依次签","optionCode":"6","oaValue":"6"},{"label":"依次签---会签---会签---依次签","optionCode":"7","oaValue":"7"},{"label":"依次签---依次签---会签---依次签","optionCode":"8","oaValue":"8"},{"label":"依次签---会签---会签---会签---依次签","optionCode":"9","oaValue":"9"},{"label":"依次签---会签---会签---会签","optionCode":"10","oaValue":"10"},{"label":"依次签---会签---依次签---会签---依次签","optionCode":"12","oaValue":"12"}]},{"key":"copy","label":"抄送人","oaFieldCode":"csrmd","controlType":"USER_MULTI","semanticType":"COPY","multiple":true,"required":false,"sortNo":8},{"key":"attachment","label":"附件","oaFieldCode":"fj","controlType":"FILE","semanticType":"ATTACHMENT","required":true,"sortNo":9}]}' ,
    'ENABLED', '浏览器读取：workflowId=721603；附件字段 fj 必填，审批事项字段 spsx 必填', now(), 1, '0'
)
on duplicate key update
    form_name = values(form_name), request_name_template = values(request_name_template),
    field_mapping_json = values(field_mapping_json), specific_field_mapping_json = values(specific_field_mapping_json),
    field_schema_json = values(field_schema_json), status = values(status), remark = values(remark);

insert into dm_oa_workflow_option (id, option_code, option_name, sort_no, status, remark)
values
    (1761400000000003501, '0',  '依次签',                                                  0,  'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003502, '1',  '会签',                                                    1,  'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003503, '15', '或签',                                                    2,  'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003504, '14', '依次签---或签',                                             3,  'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003505, '2',  '依次签---会签',                                             4,  'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003506, '3',  '会签---依次签',                                             5,  'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003507, '4',  '依次签---会签---会签',                                      6,  'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003508, '5',  '依次签---会签---依次签',                                    7,  'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003509, '9',  '会签---会签---依次签',                                      8,  'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003510, '6',  '主任会签---经理会签---分管领导会签',                         9,  'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003511, '7',  '主任会签---经理会签---分管领导会签---公司领导',                10, 'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003512, '8',  '依次签---会签---会签---公司领导签',                          11, 'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003513, '10', '依次签---会签---会签---依次签',                              12, 'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003514, '11', '依次签---依次签---会签---依次签',                            13, 'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003515, '12', '依次签---会签---会签---会签---依次签',                       14, 'ENABLED', '来自泛微测试表单审批流程方式字段'),
    (1761400000000003516, '13', '依次签---会签---会签---会签',                                15, 'ENABLED', '来自泛微测试表单审批流程方式字段')
on duplicate key update
    option_name = values(option_name),
    sort_no = values(sort_no),
    status = values(status),
    remark = values(remark);

update dm_oa_workflow_option
set participant_mapping_json = case option_code
    when '0' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"依次签","fieldCode":"ycq1","mode":"SEQUENTIAL","sortNo":1,"required":true}]}'
    when '1' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"会签","fieldCode":"hq2","mode":"COUNTERSIGN","sortNo":1,"required":true}]}'
    when '15' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"或签","fieldCode":"hq15","mode":"OR_SIGN","sortNo":1,"required":true}]}'
    when '14' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"依次签","fieldCode":"yjycq16","mode":"SEQUENTIAL","sortNo":1,"required":true},{"code":"STAGE_2","name":"或签","fieldCode":"ejhq16","mode":"OR_SIGN","sortNo":2,"required":true}]}'
    when '2' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"依次签","fieldCode":"ycq3","mode":"SEQUENTIAL","sortNo":1,"required":true},{"code":"STAGE_2","name":"会签","fieldCode":"hq3","mode":"COUNTERSIGN","sortNo":2,"required":true}]}'
    when '3' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"会签","fieldCode":"hq4","mode":"COUNTERSIGN","sortNo":1,"required":true},{"code":"STAGE_2","name":"依次签","fieldCode":"ycq4","mode":"SEQUENTIAL","sortNo":2,"required":true}]}'
    when '4' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"依次签","fieldCode":"ycq6","mode":"SEQUENTIAL","sortNo":1,"required":true},{"code":"STAGE_2","name":"一级会签","fieldCode":"yjhq","mode":"COUNTERSIGN","sortNo":2,"required":true},{"code":"STAGE_3","name":"二级会签","fieldCode":"ejhq","mode":"COUNTERSIGN","sortNo":3,"required":true}]}'
    when '5' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"一级依次签","fieldCode":"yjycq","mode":"SEQUENTIAL","sortNo":1,"required":true},{"code":"STAGE_2","name":"会签","fieldCode":"hq8","mode":"COUNTERSIGN","sortNo":2,"required":true},{"code":"STAGE_3","name":"二级依次签","fieldCode":"ejycq8","mode":"SEQUENTIAL","sortNo":3,"required":true}]}'
    when '9' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"一级会签","fieldCode":"yjhq10","mode":"COUNTERSIGN","sortNo":1,"required":true},{"code":"STAGE_2","name":"二级会签","fieldCode":"ejhq10","mode":"COUNTERSIGN","sortNo":2,"required":true},{"code":"STAGE_3","name":"依次签","fieldCode":"ycq10","mode":"SEQUENTIAL","sortNo":3,"required":true}]}'
    when '6' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"主任会签","fieldCode":"zrjhq5","mode":"COUNTERSIGN","sortNo":1,"required":true},{"code":"STAGE_2","name":"经理会签","fieldCode":"jljhq5","mode":"COUNTERSIGN","sortNo":2,"required":true},{"code":"STAGE_3","name":"分管领导会签","fieldCode":"fgfzjl5","mode":"COUNTERSIGN","sortNo":3,"required":true}]}'
    when '7' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"主任会签","fieldCode":"zrjhq7","mode":"COUNTERSIGN","sortNo":1,"required":true},{"code":"STAGE_2","name":"经理会签","fieldCode":"jljhq7","mode":"COUNTERSIGN","sortNo":2,"required":true},{"code":"STAGE_3","name":"分管领导会签","fieldCode":"fgfzjl7","mode":"COUNTERSIGN","sortNo":3,"required":true},{"code":"STAGE_4","name":"公司领导","fieldCode":"gsld1","mode":"COUNTERSIGN","sortNo":4,"required":true}]}'
    when '8' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"依次签","fieldCode":"ycq9","mode":"SEQUENTIAL","sortNo":1,"required":true},{"code":"STAGE_2","name":"一级会签","fieldCode":"yjhq9","mode":"COUNTERSIGN","sortNo":2,"required":true},{"code":"STAGE_3","name":"二级会签","fieldCode":"ejhq9","mode":"COUNTERSIGN","sortNo":3,"required":true},{"code":"STAGE_4","name":"公司领导","fieldCode":"gsld2","mode":"COUNTERSIGN","sortNo":4,"required":true}]}'
    when '10' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"一级依次签","fieldCode":"yjycq11","mode":"SEQUENTIAL","sortNo":1,"required":true},{"code":"STAGE_2","name":"一级会签","fieldCode":"yjhq11","mode":"COUNTERSIGN","sortNo":2,"required":true},{"code":"STAGE_3","name":"二级会签","fieldCode":"ejhq11","mode":"COUNTERSIGN","sortNo":3,"required":true},{"code":"STAGE_4","name":"二级依次签","fieldCode":"ejycq11","mode":"SEQUENTIAL","sortNo":4,"required":true}]}'
    when '11' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"一级依次签","fieldCode":"yjycq12","mode":"SEQUENTIAL","sortNo":1,"required":true},{"code":"STAGE_2","name":"二级依次签","fieldCode":"ejycq12","mode":"SEQUENTIAL","sortNo":2,"required":true},{"code":"STAGE_3","name":"会签","fieldCode":"yjhq12","mode":"COUNTERSIGN","sortNo":3,"required":true},{"code":"STAGE_4","name":"三级依次签","fieldCode":"sjjycq12","mode":"SEQUENTIAL","sortNo":4,"required":true}]}'
    when '12' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"一级依次签","fieldCode":"yjycq13","mode":"SEQUENTIAL","sortNo":1,"required":true},{"code":"STAGE_2","name":"一级会签","fieldCode":"yjhq13","mode":"COUNTERSIGN","sortNo":2,"required":true},{"code":"STAGE_3","name":"二级会签","fieldCode":"ejhq13","mode":"COUNTERSIGN","sortNo":3,"required":true},{"code":"STAGE_4","name":"三级会签","fieldCode":"sjhq13","mode":"COUNTERSIGN","sortNo":4,"required":true},{"code":"STAGE_5","name":"二级依次签","fieldCode":"ejycq13","mode":"SEQUENTIAL","sortNo":5,"required":true}]}'
    when '13' then '{"modeField":"splcfs","copyField":"csr","stages":[{"code":"STAGE_1","name":"一级依次签","fieldCode":"yjycq14","mode":"SEQUENTIAL","sortNo":1,"required":true},{"code":"STAGE_2","name":"一级会签","fieldCode":"yjhq14","mode":"COUNTERSIGN","sortNo":2,"required":true},{"code":"STAGE_3","name":"二级会签","fieldCode":"ejhq14","mode":"COUNTERSIGN","sortNo":3,"required":true},{"code":"STAGE_4","name":"三级会签","fieldCode":"sjhq14","mode":"COUNTERSIGN","sortNo":4,"required":true}]}'
    else participant_mapping_json
end
where option_code in ('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15');

create table if not exists dm_oa_business_workflow_binding (
    id                bigint          not null comment '主键',
    business_type     varchar(64)     not null comment '业务类型标识',
    form_id           bigint          not null comment '泛微表单配置 ID',
    default_option_id bigint          not null comment '默认审批方式选项 ID',
    status            varchar(20)     not null default 'ENABLED',
    create_dept       bigint          null,
    create_by         bigint          null,
    create_time       datetime        null,
    update_by         bigint          null,
    update_time       datetime        null,
    version           bigint          not null default 1,
    del_flag          char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_business_workflow_binding (business_type, del_flag),
    key idx_dm_oa_business_workflow_binding_form (form_id, status, del_flag)
) engine=innodb comment='业务类型与泛微表单绑定';

create table if not exists dm_oa_business_workflow_option (
    id          bigint          not null comment '主键',
    binding_id  bigint          not null comment '业务表单绑定 ID',
    option_id   bigint          not null comment '审批方式选项 ID',
    sort_no     int             not null default 0,
    create_dept bigint          null,
    create_by   bigint          null,
    create_time datetime        null,
    update_by   bigint          null,
    update_time datetime        null,
    del_flag    char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_business_workflow_option (binding_id, option_id, del_flag),
    key idx_dm_oa_business_workflow_option_binding (binding_id, sort_no, del_flag)
) engine=innodb comment='业务类型允许的审批方式';

create table if not exists dm_oa_process_event_log (
    id                bigint          not null comment '主键',
    process_id        bigint          not null comment '审批实例 ID',
    event_type        varchar(30)     not null comment '事件类型',
    from_status       varchar(30)     null,
    to_status         varchar(30)     null,
    request_summary   varchar(2000)   null,
    response_summary  varchar(2000)   null,
    error_code        varchar(100)    null,
    idempotency_key   varchar(64)     null,
    create_dept       bigint          null,
    create_by         bigint          null,
    create_time       datetime        null,
    update_by         bigint          null,
    update_time       datetime        null,
    primary key (id),
    key idx_dm_oa_event_process (process_id, create_time),
    key idx_dm_oa_event_type (event_type, create_time)
) engine=innodb comment='泛微审批事件日志';

create table if not exists dm_oa_application_attachment (
    id              bigint          not null comment '主键',
    application_id  bigint          not null comment '申请 ID',
    process_id      bigint          null comment '流程实例 ID',
    oss_id          bigint          not null comment '本地 OSS ID',
    attachment_type varchar(20)     not null default 'FILE' comment 'FILE 或 IMAGE',
    file_name       varchar(255)    not null comment '文件名称快照',
    file_url        varchar(1000)   null comment '本地 OSS URL 快照',
    sort_no         int             not null default 0,
    upload_status   varchar(30)     not null default 'PENDING' comment 'PENDING/URL_READY/PROCESS_BOUND/FAILED',
    oa_file_id      varchar(100)    null comment '泛微文件 ID',
    oa_file_path    varchar(1000)   null comment '泛微文件访问路径',
    fail_reason     varchar(1000)   null,
    create_dept     bigint          null,
    create_by       bigint          null,
    create_time     datetime        null,
    update_by       bigint          null,
    update_time     datetime        null,
    del_flag        char(1)         not null default '0',
    primary key (id),
    unique key uk_dm_oa_attachment (application_id, oss_id, del_flag),
    key idx_dm_oa_attachment_process (process_id),
    key idx_dm_oa_attachment_application (application_id, sort_no, del_flag)
) engine=innodb comment='泛微审批申请附件';

create table if not exists dm_oa_approval_participant (
    id                bigint          not null comment '主键',
    application_id     bigint          not null comment '申请 ID',
    process_id         bigint          null comment '流程实例 ID',
    stage_code         varchar(64)     not null comment '审批节点编码',
    stage_name         varchar(100)    null comment '审批节点名称',
    rule_id            bigint          null comment '审批规则 ID',
    rule_code          varchar(64)     null comment '审批规则编码',
    rule_name          varchar(100)    null comment '审批规则名称',
    stage_order        int             null comment '审批节点顺序',
    stage_mode         varchar(20)     null comment '审批方式',
    participant_role   varchar(20)     null comment '参与类型 APPROVER/COPY',
    participant_type   varchar(20)     not null default 'USER' comment 'USER/OA_USER',
    local_user_id      bigint          null comment '本地用户 ID',
    oa_user_id         varchar(64)     null comment '泛微用户 ID 快照',
    oa_user_name       varchar(100)    null comment '泛微用户名称快照',
    source_value       varchar(100)    null comment '动态规则来源值',
    sort_no            int             not null default 0,
    required           tinyint         not null default 1,
    create_dept        bigint          null,
    create_by          bigint          null,
    create_time        datetime        null,
    update_by          bigint          null,
    update_time        datetime        null,
    del_flag           char(1)         not null default '0',
    primary key (id),
    key idx_dm_oa_participant_application (application_id, stage_code, sort_no, del_flag),
    key idx_dm_oa_participant_process (process_id)
) engine=innodb comment='泛微审批动态审批人快照';

create table if not exists dm_oa_callback_event (
    id              bigint          not null comment '主键',
    event_key       varchar(128)    not null comment '回调事件幂等键',
    oa_request_id   varchar(64)     not null comment '泛微 requestId',
    process_id      bigint          null comment '本地流程实例 ID',
    event_status    varchar(20)     not null comment 'RECEIVED/PROCESSED/IGNORED/FAILED',
    raw_body        text            null comment '回调原始报文（已截断）',
    error_message   varchar(1000)   null,
    processed_at    datetime        null,
    create_dept     bigint          null,
    create_by       bigint          null,
    create_time     datetime        null,
    update_by       bigint          null,
    update_time      datetime        null,
    primary key (id),
    unique key uk_dm_oa_callback_event (event_key),
    key idx_dm_oa_callback_request (oa_request_id, create_time),
    key idx_dm_oa_callback_status (event_status, create_time)
) engine=innodb comment='泛微审批回调事件';

-- 泛微工作区权限：保留真实 API 按钮权限，统一挂到新的工作区菜单下。
-- 3201 原为旧的“通用审批申请”页面，这里复用为单项审批列表权限按钮，避免删除后丢失 ecology:application:list。
insert ignore into sys_menu values(1761400000000003201, '单项审批查询', 1761400000000003292, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:application:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '单项审批列表权限');
insert ignore into sys_menu values(1761400000000003202, '申请查询', 1761400000000003292, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:application:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003203, '申请新增', 1761400000000003292, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:application:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003204, '申请编辑', 1761400000000003292, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:application:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003205, '申请提交', 1761400000000003292, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:application:submit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003206, '申请同步', 1761400000000003292, 6, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:application:sync', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003207, '立即对账', 1761400000000003292, 7, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:application:reconcile', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003210, '流程配置', 1761400000000003299, 2, 'workflowConfig', 'ecology/workflowConfig/index', '', 'N', 'Y', 'C', '0', '0', 'ecology:workflowConfig:list', 'workflow', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '维护可复用的泛微表单及其审批方式选项');
insert ignore into sys_menu values(1761400000000003211, '流程配置新增', 1761400000000003210, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:workflowConfig:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003212, '流程配置修改', 1761400000000003210, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:workflowConfig:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003213, '流程配置删除', 1761400000000003210, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:workflowConfig:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003214, '流程配置查询', 1761400000000003210, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:workflowConfig:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003220, '审批方案权限', 1761400000000003291, 8, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:departmentApproval:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '审批方案列表权限');
insert ignore into sys_menu values(1761400000000003221, '配置查询', 1761400000000003291, 9, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:departmentApproval:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003222, '配置新增', 1761400000000003291, 10, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:departmentApproval:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003223, '配置修改', 1761400000000003291, 11, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:departmentApproval:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003224, '配置删除', 1761400000000003291, 12, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:departmentApproval:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003225, '审批链预览', 1761400000000003292, 8, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:application:preview', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003230, '审批监控', 1761400000000003292, 9, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:application:monitor', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '查看全部通用审批实例');
insert ignore into sys_menu values(1761400000000003250, '业务类型权限', 1761400000000003291, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:businessType:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '业务类型列表权限');
insert ignore into sys_menu values(1761400000000003251, '业务类型查询', 1761400000000003291, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:businessType:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003252, '业务类型新增', 1761400000000003291, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:businessType:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003253, '业务类型修改', 1761400000000003291, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:businessType:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003254, '业务类型停用', 1761400000000003291, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:businessType:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003201 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003202 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003203 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003204 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003205 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003206 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003207 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003210 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003211 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003212 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003213 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003214 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003220 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003221 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003222 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003223 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003224 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003225 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003230 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003250 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003251 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003252 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003253 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003254 from sys_role_menu where menu_id = 1761400000000003000;

-- 泛微 HRM 组织与人员同步中心
create table if not exists dm_oa_sync_batch (
    id                  bigint          not null comment '主键',
    sync_type           varchar(20)     not null comment 'ORGANIZATION/USER',
    sync_mode           varchar(20)     not null default 'FULL' comment 'FULL/INCREMENTAL',
    status              varchar(20)     not null default 'RUNNING' comment 'RUNNING/SUCCESS/PARTIAL/FAILED',
    watermark           varchar(30)     null comment '本次增量起始时间',
    started_at          datetime        not null,
    finished_at         datetime        null,
    total_count         int             not null default 0,
    success_count       int             not null default 0,
    created_count       int             not null default 0,
    updated_count       int             not null default 0,
    disabled_count      int             not null default 0,
    pending_count       int             not null default 0,
    failed_count        int             not null default 0,
    message             varchar(2000)   null,
    create_dept         bigint          null,
    create_by           bigint          null,
    create_time         datetime        null,
    update_by           bigint          null,
    update_time         datetime        null,
    version             bigint          not null default 1,
    del_flag            char(1)         not null default '0',
    primary key (id),
    key idx_dm_oa_sync_batch_type (sync_type, status, started_at),
    key idx_dm_oa_sync_batch_started (started_at)
) engine=innodb comment='泛微 HRM 同步批次';

create table if not exists dm_oa_sync_detail (
    id                  bigint          not null comment '主键',
    batch_id            bigint          not null comment '同步批次 ID',
    entity_type         varchar(30)     not null comment 'SUBCOMPANY/DEPARTMENT/JOBTITLE/USER',
    source_id           varchar(64)     null comment '泛微源 ID',
    source_key          varchar(200)    null comment '泛微源业务键',
    local_id            bigint          null comment '本地对象 ID',
    action              varchar(30)     null comment 'CREATE/UPDATE/DISABLE/SKIP',
    detail_status       varchar(30)     not null comment 'SUCCESS/PENDING/FAILED/CONFLICT',
    message             varchar(2000)   null,
    create_dept         bigint          null,
    create_by           bigint          null,
    create_time         datetime        null,
    update_by           bigint          null,
    update_time         datetime        null,
    version             bigint          not null default 1,
    del_flag            char(1)         not null default '0',
    primary key (id),
    key idx_dm_oa_sync_detail_batch (batch_id, detail_status),
    key idx_dm_oa_sync_detail_source (entity_type, source_key)
) engine=innodb comment='泛微 HRM 同步明细';

insert ignore into sys_menu values(1761400000000003241, '同步并接管组织', 1761400000000003290, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:hrmSync:organization', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '按泛微组织重建本地部门并同步岗位');
insert ignore into sys_menu values(1761400000000003242, '同步人员', 1761400000000003290, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:hrmSync:user', '#', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '按泛微工号同步系统用户');
insert ignore into sys_menu values(1761400000000003245, '同步批次查询', 1761400000000003290, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:hrmSync:batch:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '查看泛微 HRM 同步批次');
insert ignore into sys_menu values(1761400000000003246, '同步明细查询', 1761400000000003290, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:hrmSync:detail:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '查看泛微 HRM 同步异常明细');
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003241 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003242 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003245 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003246 from sys_role_menu where menu_id = 1761400000000003000;

-- 通用业务导入到泛微：Excel 导入、部门匹配、分组附件和 OA 申请批次。
create table if not exists dm_oa_import_business_config (
    id bigint not null comment '主键', business_type varchar(64) not null comment '业务类型编码', business_name varchar(100) not null comment '业务名称',
    sheet_name varchar(100) null comment 'Excel 工作表名称', header_row int not null default 0 comment '表头行，从 0 开始',
    field_definitions_json mediumtext null, parameter_definitions_json mediumtext null, group_by_json mediumtext null,
    dept_field varchar(64) null, company_field varchar(64) null, aggregation_json mediumtext null, form_mapping_json mediumtext null,
    attachment_config_json mediumtext null, request_name_template varchar(200) null, content_template varchar(5000) null,
    default_workflow_config_id bigint null, default_approval_plan_id bigint null,
    default_approval_mode varchar(20) not null default 'AUTO_RULE', status varchar(20) not null default 'ENABLED', remark varchar(1000) null,
    create_dept bigint null, create_by bigint null, create_time datetime null, update_by bigint null, update_time datetime null,
    version bigint not null default 1, del_flag char(1) not null default '0', primary key (id),
    unique key uk_dm_oa_import_business_type (business_type, del_flag), key idx_dm_oa_import_business_status (status, del_flag)
) engine=innodb comment='通用泛微导入业务模板';

create table if not exists dm_oa_import_batch (
    id bigint not null comment '主键', config_id bigint not null, business_type varchar(64) not null, batch_no varchar(64) not null,
    source_file_name varchar(255) null, status varchar(30) not null default 'READY', total_count int not null default 0,
    matched_count int not null default 0, group_count int not null default 0, application_count int not null default 0,
    failed_count int not null default 0, skipped_count int not null default 0, message varchar(2000) null, create_dept bigint null, create_by bigint null, create_time datetime null,
    update_by bigint null, update_time datetime null, version bigint not null default 1, del_flag char(1) not null default '0', primary key (id),
    unique key uk_dm_oa_import_batch_no (batch_no, del_flag), key idx_dm_oa_import_batch_query (config_id, status, create_time, del_flag)
) engine=innodb comment='通用泛微导入批次';

create table if not exists dm_oa_import_record (
    id bigint not null comment '主键', batch_id bigint not null, row_no int not null, data_json longtext null, group_key varchar(500) null,
    group_name varchar(500) null, dept_id bigint null, company_id bigint null, application_id bigint null, attachment_oss_id bigint null,
    status varchar(30) not null default 'UNMATCHED', error_message varchar(500) null, skip_reason varchar(500) null, create_dept bigint null, create_by bigint null,
    create_time datetime null, update_by bigint null, update_time datetime null, version bigint not null default 1, del_flag char(1) not null default '0',
    primary key (id), key idx_dm_oa_import_record_batch (batch_id, row_no, del_flag), key idx_dm_oa_import_record_dept (batch_id, dept_id, status, del_flag),
    key idx_dm_oa_import_record_application (application_id, del_flag)
) engine=innodb comment='通用泛微导入明细';

create table if not exists dm_oa_import_dept_alias (
    id bigint not null comment '主键', business_type varchar(64) not null comment '业务类型编码', source_dept_name varchar(255) not null, normalized_name varchar(255) not null, dept_id bigint not null,
    target_dept_name varchar(255) null, status char(1) not null default '0', create_dept bigint null, create_by bigint null, create_time datetime null,
    update_by bigint null, update_time datetime null, version bigint not null default 1, del_flag char(1) not null default '0', primary key (id),
    unique key uk_dm_oa_import_dept_alias_scope (business_type, normalized_name, del_flag), key idx_dm_oa_import_dept_alias_business (business_type, status, del_flag), key idx_dm_oa_import_dept_alias_dept (dept_id, status, del_flag)
) engine=innodb comment='通用泛微导入部门别名映射';

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003270, '批量导入权限', 1761400000000003292, 10, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importBusiness:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '批量导入列表权限');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003271, '通用导入查询', 1761400000000003292, 11, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importBusiness:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '查询通用导入批次');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003272, '通用导入数据', 1761400000000003292, 12, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importBusiness:import', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '导入 Excel 业务数据');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003273, '通用导入部门匹配', 1761400000000003292, 13, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importBusiness:map', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '维护来源部门到泛微部门的映射');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003274, '通用导入提交泛微', 1761400000000003292, 14, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importBusiness:submit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '按分组生成附件并提交泛微');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003275, '通用导入批次删除', 1761400000000003292, 15, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importBusiness:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '删除尚未创建泛微申请的导入批次');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003280, '导入模板权限', 1761400000000003291, 13, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importConfig:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '导入模板列表权限');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003281, '导入模板查询', 1761400000000003291, 14, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importConfig:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '查询导入业务模板');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003282, '导入模板新增', 1761400000000003291, 15, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importConfig:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '新增导入业务模板');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003283, '导入模板修改', 1761400000000003291, 16, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importConfig:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '修改导入业务模板');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003284, '导入模板删除', 1761400000000003291, 17, '', '', '', 'N', 'Y', 'F', '0', '0', 'ecology:importConfig:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '删除导入业务模板');

insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003270 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003271 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003272 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003273 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003274 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003275 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003280 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003281 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003282 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003283 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003284 from sys_role_menu where menu_id = 1761400000000003000;

-- 泛微工作区导航统一由“泛微工作中心”承载，旧目录节点不再创建。
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003299, '泛微工作中心', 1761400000000003000, 13, 'ecologyWorkspace', '', '', 'N', 'Y', 'M', '0', '0', '', 'workflow', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '泛微组织同步、业务配置、业务提交和申请记录');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003290, '泛微同步', 1761400000000003299, 1, 'hrmSync', 'ecology/hrm-sync/index', '', 'N', 'Y', 'C', '0', '0', 'ecology:hrmSync:list', 'tree', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '同步泛微组织、岗位和人员');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003291, '业务配置', 1761400000000003299, 3, 'businessConfig', 'ecology/businessConfig/index', '', 'N', 'Y', 'C', '0', '0', 'ecology:businessConfig:list', 'tool', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '维护业务类型绑定、审批方案和导入模板');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003292, '业务提交', 1761400000000003299, 4, 'businessSubmit', 'ecology/businessSubmit/index', '', 'N', 'Y', 'C', '0', '0', 'ecology:businessSubmit:list', 'upload', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '发起单项审批或批量业务导入');
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
values (1761400000000003293, '我的申请', 1761400000000003299, 5, 'myApplication', 'ecology/myApplication/index', '', 'N', 'Y', 'C', '0', '0', 'ecology:myApplication:list', 'documentation', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '查看本人发起的审批和导入申请');

insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003299 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003290 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003291 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003292 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id) select distinct role_id, 1761400000000003293 from sys_role_menu where menu_id = 1761400000000003000;
