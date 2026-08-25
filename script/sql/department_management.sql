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
    job_title           varchar(100)    default null comment '岗位',
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
    key idx_dm_person_profile_service_period (create_dept, member_status, join_date, leave_date)
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
    key idx_dm_daily_report_leave (leave_id)
) engine=innodb comment='科室每日工作日报';

create table if not exists dm_daily_calendar_override (
    id                  bigint(20)      not null comment '主键',
    dept_id             bigint(20)      not null comment '科室ID',
    user_id             bigint(20)      default null comment '调休上班人员ID；科室统一休息日为空',
    calendar_date       date            not null comment '例外日期',
    day_type            varchar(20)     not null comment '日期类型（WORKDAY调休上班 REST休息）',
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
-- REST 为科室统一休息日（user_id 为空），WORKDAY 必须绑定具体人员。

create table if not exists dm_daily_leave (
    id                  bigint(20)      not null comment '主键',
    dept_id             bigint(20)      not null comment '科室ID',
    user_id             bigint(20)      not null comment '人员ID',
    start_date          date            not null comment '休假开始日期',
    end_date            date            not null comment '休假结束日期',
    leave_type          varchar(50)     not null default '休假' comment '休假类型',
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
    key idx_dm_daily_leave_dept_range (dept_id, start_date, end_date)
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
    unique key uk_dm_weekly_report_range (week_start, week_end),
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
    review_status               varchar(20)     not null default 'PENDING' comment '审核状态（PENDING待审核 APPROVED已通过 REJECTED已驳回）',
    review_comment              varchar(1000)   default null comment '审核意见',
    reviewer_user_id            bigint(20)      default null comment '实际审核人系统用户ID',
    version                     bigint(20)      default 0 comment '版本号',
    create_dept                 bigint(20)      default null comment '创建部门',
    create_by                   bigint(20)      default null comment '创建者',
    create_time                 datetime        default null comment '创建时间',
    update_by                   bigint(20)      default null comment '更新者',
    update_time                 datetime        default null comment '更新时间',
    del_flag                    char(1)         default '0' comment '删除标志（0存在 1删除）',
    primary key (id),
    key idx_dm_five_why_dept_date (dept_id, analysis_date),
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
    team_members                text            comment '企业参与人员/部门EIT小组成员',
    employee_no                 varchar(64)     default null comment '提议人工号',
    proposer_name               varchar(100)    not null comment '提议者姓名',
    proposer_role               varchar(100)    default null comment '提议者岗位',
    proposer_level              varchar(50)     default null comment '职位层级字典值（dm_score_job）',
    dept_name                   varchar(150)    default null comment '车间/部门',
    main_category               varchar(500)    default null comment '提案大类',
    sub_category                varchar(500)    default null comment '提案小类',
    problem_description         text            comment '问题描述',
    improvement_measure         text            comment '改进措施',
    implementer_supervisor      text            comment '实施人/监督人',
    before_oss_id               bigint(20)      default null comment '改进前图片OSS ID',
    after_oss_id                bigint(20)      default null comment '改进后图片OSS ID',
    start_date                  date            default null comment '开始日期',
    planned_completion_date     date            default null comment '计划完成日期',
    actual_completion_date      date            default null comment '实际完成日期',
    completion_status            varchar(50)     default null comment '完成状态',
    remark                      varchar(1000)   default null comment '备注',
    review_status               varchar(20)     not null default 'PENDING' comment '审核状态（PENDING待审核 APPROVED已通过 REJECTED已驳回）',
    review_comment              varchar(1000)   default null comment '审核意见',
    reviewer_user_id            bigint(20)      default null comment '实际审核人系统用户ID',
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
    key idx_dm_score_category (main_category),
    key idx_dm_score_category_id (main_category_id, sub_category_id)
) engine=innodb comment='科室SCORE提案记录';

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
    key idx_dm_task_rule_dept_status (dept_id, status, task_type)
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
    key idx_dm_task_assignment_user (dept_id, user_id, status)
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
insert ignore into sys_menu values(1761400000000003000, '科室管理', 0, 6, 'department', null, '', 'N', 'Y', 'M', '0', '0', '', 'post', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '科室管理目录');
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
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003003);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003030);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003031);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003032);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003033);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003034);

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

insert ignore into sys_menu values(1761400000000003120, '资料分类配置', 1761400000000003000, 10, 'documentCategory', 'department/documentCategory/index', '', 'N', 'Y', 'C', '0', '0', 'department:documentCategory:list', 'tree', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '按科室维护资料分类配置');
insert ignore into sys_menu values(1761400000000003121, '分类查询', 1761400000000003120, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:documentCategory:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003122, '分类新增', 1761400000000003120, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:documentCategory:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003123, '分类修改', 1761400000000003120, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:documentCategory:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_menu values(1761400000000003124, '分类删除', 1761400000000003120, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'department:documentCategory:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '');
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003120);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003121);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003122);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003123);
insert ignore into sys_role_menu values (1761300000000000001, 1761400000000003124);
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003120 from sys_role_menu where menu_id = 1761400000000003000;
insert ignore into sys_role_menu (role_id, menu_id)
select distinct role_id, 1761400000000003121 from sys_role_menu where menu_id = 1761400000000003000;
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
