-- --------------------------------------------------------------------------------------------------------------------
-- 删除关联表
-- --------------------------------------------------------------------------------------------------------------------
-- 10.角色菜单表
drop table if exists tbl_sys_role_menus;
-- 8.用户角色关联表
drop table if exists tbl_sys_user_roles;
-- 7.部门角色关联表
drop table if exists tbl_sys_dept_roles;
-- 6.岗位角色关联表
drop table if exists tbl_sys_post_roles;
-- 4.用户岗位关联表
drop table if exists tbl_sys_user_posts;
-- 3.岗位表
drop table if exists tbl_sys_post;
-- 2.用户表
drop table if exists tbl_sys_user;
-- --------------------------------------------------------------------------------------------------------------------
-- --------------------------------------------------------------------------------------------------------------------
-- 1.部门表
drop table if exists tbl_sys_dept;
create table tbl_sys_dept (
    `id`        bigint unsigned not null    comment '部门ID',
    `code`      int unsigned default 0      comment '部门代码(排序)',
    `name`      varchar(32) not null        comment '部门名称',

    `parent_id` bigint unsigned default 0   comment '父部门ID',
    `ancestors` varchar(2048) default null  comment '祖级列表',

    `leader`    varchar(32) default null    comment '负责人',
    `mobile`    varchar(20) default null    comment '联系电话',
    `email`    varchar(128) default null    comment '邮箱',

    `status`      tinyint not null default 1 comment '状态(-1:删除,0:停用,1:启用)',

    `create_time` timestamp   not null default current_timestamp comment '创建时间',
    `update_time` timestamp   not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_dept` primary key(`id`),
    constraint `uk_sys_dept_name` unique key(`name`)
) engine=InnoDB default charset=utf8mb4 comment '部门表';
-- --------------------------------------------------------------------------------------------------------------------
-- 2.用户表
drop table if exists tbl_sys_user;
create table tbl_sys_user (
    `id`        bigint unsigned not null comment '用户ID',
    `name`      varchar(32) default null comment '用户姓名',
    `account`       varchar(32) not null comment '用户账号',
    `passwd`    varchar(64) default null comment '登录密码',
    `mobile`    varchar(32) default null comment '手机号码',
    `email`    varchar(128) default null comment '邮箱地址',

    `dept_id`   bigint unsigned not null comment '所属部门ID',

    `status`  tinyint not null default 1 comment '状态(-1:删除,0:停用,1:启用)',

    `create_time` timestamp   not null default current_timestamp comment '创建时间',
    `update_time` timestamp   not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_user` primary key (`id`),
    constraint `uk_sys_user_account` unique key (`account`),
    constraint `fk_sys_user_dept` foreign key(`dept_id`) references tbl_sys_dept(`id`)
) engine=InnoDB default charset=utf8mb4 comment '用户表';
-- --------------------------------------------------------------------------------------------------------------------
-- 3.岗位表
drop table if exists tbl_sys_post;
create table tbl_sys_post (
    `id`        bigint unsigned not null comment '岗位ID',
    `code`      varchar(32) not null     comment '岗位编码',
    `name`      varchar(32) default null comment '岗位名称',

    `dept_id`   bigint unsigned not null comment '所属部门ID',

    `status`  tinyint not null default 1 comment '状态(-1:删除,0:停用,1:启用)',

    `create_time` timestamp   not null default current_timestamp comment '创建时间',
    `update_time` timestamp   not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_post` primary key(`id`),
    constraint `uk_sys_post_code` unique key(`code`),
    constraint `fk_sys_post_dept` foreign key(`dept_id`) references tbl_sys_dept(`id`)
) engine=InnoDB default charset=utf8mb4 comment '岗位表';
-- --------------------------------------------------------------------------------------------------------------------
-- 4.用户岗位关联表
drop table if exists tbl_sys_user_posts;
create table tbl_sys_user_posts (
    `user_id`   bigint unsigned not null comment '用户ID',
    `post_id`   bigint unsigned not null comment '岗位ID',

    constraint `pk_sys_user_posts` primary key(`user_id`, `post_id`),
    constraint `fk_sys_user_posts_u` foreign key(`user_id`) references tbl_sys_user(`id`),
    constraint `fk_sys_user_posts_p` foreign key(`post_id`) references tbl_sys_post(`id`)
) engine=InnoDB default charset=utf8mb4 comment '用户岗位关联表';
-- --------------------------------------------------------------------------------------------------------------------
-- 5.角色表
drop table if exists tbl_sys_role;
create table tbl_sys_role (
    `id`    bigint unsigned not null        comment '角色ID',
    `code`  int unsigned default 0          comment '角色代码(排序)',
    `name`      varchar(32) not null        comment '角色名称',
    `abbr`      varchar(32) not null        comment '角色简称',
    `remark`   varchar(255) default null    comment '角色备注',

    `scope` tinyint unsigned default 0   comment '数据权限范围(0:未数据授权,1:全部数据权限,2:自定数据权限,3:本部门数据权限,4:部门及以下数据权限,5:仅本人数据权限)',
    `scope_menus`    text default null   comment '关联菜单权限范围(用,分隔)',
    `scope_depts`    text default null   comment '关联部门权限范围(用,分隔)',

    `status`  tinyint not null default 1 comment '状态(-1:删除,0:停用,1:启用)',

    `create_time` timestamp   not null default current_timestamp comment '创建时间',
    `update_time` timestamp   not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_role` primary key(`id`),
    constraint `uk_sys_role_name` unique key(`name`),
    constraint `uk_sys_role_abbr` unique key(`abbr`)
) engine=InnoDB default charset=utf8mb4 comment '角色表';
-- --------------------------------------------------------------------------------------------------------------------
-- 6.岗位角色关联表
drop table if exists tbl_sys_post_roles;
create table tbl_sys_post_roles (
    `post_id` bigint unsigned not null comment '岗位ID',
    `role_id` bigint unsigned not null comment '角色ID',

    constraint `pk_sys_post_roles` primary key(`post_id`,`role_id`),
    constraint `fk_sys_post_roles_p` foreign key(`post_id`) references tbl_sys_post(`id`),
    constraint `fk_sys_post_roles_r` foreign key(`role_id`) references tbl_sys_role(`id`)
) engine=InnoDB default charset=utf8mb4 comment '岗位角色关联表';
-- --------------------------------------------------------------------------------------------------------------------
-- 7.部门角色关联表
drop table if exists tbl_sys_dept_roles;
create table tbl_sys_dept_roles (
    `dept_id` bigint unsigned not null comment '部门ID',
    `role_id` bigint unsigned not null comment '角色ID',

    constraint `pk_sys_dept_roles` primary key(`dept_id`,`role_id`),
    constraint `fk_sys_dept_roles_d` foreign key(`dept_id`) references tbl_sys_dept(`id`),
    constraint `fk_sys_dept_roles_r` foreign key(`role_id`) references tbl_sys_role(`id`)
) engine=InnoDB default charset=utf8mb4 comment '部门角色关联表';
-- --------------------------------------------------------------------------------------------------------------------
-- 8.用户角色关联表
drop table if exists tbl_sys_user_roles;
create table tbl_sys_user_roles (
    `user_id` bigint unsigned not null comment '用户ID',
    `role_id` bigint unsigned not null comment '角色ID',

    constraint `pk_sys_user_roles` primary key(`user_id`,`role_id`),
    constraint `fk_sys_user_roles_u` foreign key(`user_id`) references tbl_sys_user(`id`),
    constraint `fk_sys_user_roles_r` foreign key(`role_id`) references tbl_sys_role(`id`)
) engine=InnoDB default charset=utf8mb4 comment '用户角色关联表';
-- --------------------------------------------------------------------------------------------------------------------
-- 9.菜单权限表
drop table if exists tbl_sys_menu;
create table tbl_sys_menu (
    `id`    bigint unsigned not null comment '菜单ID',
    `code`  int unsigned default 0   comment '菜单代码(排序)',
    `name`  varchar(128) not null    comment '菜单名称',

    `parent_id` bigint unsigned default null  comment '父菜单ID',

    `path`         varchar(255) default null  comment '路由地址',
    `component`    varchar(255) default null  comment '组件路径',
    `query`        varchar(255) default null  comment '路由参数',
    `is_link`     tinyint unsigned default 0  comment '是否为外链(0:否,1:是)',
    `is_cache`    tinyint unsigned default 0  comment '是否缓存(0:不缓存,1:缓存)',
    `type`        tinyint unsigned default 1  comment '菜单类型(1:目录,2:菜单,3:按钮)',
    `visible`     tinyint unsigned default 1  comment '菜单状态(1:显示,0:隐藏)',
    `perms`       varchar(255) default null   comment '权限标识',
    `icon`        varchar(128) default '#'    comment '菜单图标',

    `status`  tinyint not null default 1 comment '状态(-1:删除,0:停用,1:启用)',

    `create_time` timestamp   not null default current_timestamp comment '创建时间',
    `update_time` timestamp   not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_menu` primary key(`id`)
) engine=InnoDB default charset=utf8mb4 comment '菜单权限表';
-- --------------------------------------------------------------------------------------------------------------------
-- 10.角色菜单表
drop table if exists tbl_sys_role_menus;
create table tbl_sys_role_menus (
    `role_id` bigint unsigned not null comment '角色ID',
    `menu_id` bigint unsigned not null comment '菜单ID',

    constraint `pk_sys_role_menus` primary key(`role_id`,`menu_id`),
    constraint `fk_sys_role_menus_r` foreign key(`role_id`) references tbl_sys_role(`id`),
    constraint `fk_sys_role_menus_m` foreign key(`menu_id`) references tbl_sys_menu(`id`)
) engine=InnoDB default charset=utf8mb4 comment '角色菜单表';
-- --------------------------------------------------------------------------------------------------------------------
-- 11.字典类型表
drop table if exists tbl_sys_dict_type;
create table tbl_sys_dict_type (
    `id`          bigint unsigned not null  comment '字典ID',
    `name`        varchar(64)  not null     comment '字典名称',
    `type`        varchar(128) not null     comment '字典类型',
    `remark`      varchar(255) default null comment '字典备注',

    `status`      tinyint               default 1 comment '状态(-1:删除,0:停用,1:启用)',
    `create_time` timestamp    not null default current_timestamp comment '创建时间',
    `update_time` timestamp    not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_dict_type` primary key (`id`),
    constraint `uk_sys_dict_type_type` unique key (`type`)
) engine=InnoDB default charset=utf8mb4 comment '字典类型表';
-- --------------------------------------------------------------------------------------------------------------------
-- 12.字典数据表
drop table if exists tbl_sys_dict_data;
create table tbl_sys_dict_data (
    `id`        bigint unsigned not null    comment '字典数据ID',
    `code`        int unsigned default 0    comment '字典代码(排序)',
    `label`        varchar(128) not null    comment '字典标签',
    `val`         varchar(255) default ''   comment '字典键值',
    `is_default` tinyint unsigned default 0 comment '是否默认(0:否,1:是)',

    `type`        varchar(128) not null     comment '字典类型',

    `css_class`  varchar(128) default null  comment '样式属性',
    `list_class` varchar(128) default null  comment '表格回显样式',

    `remark`  varchar(255) default null     comment '备注',

    `status` tinyint default 1  comment '状态(-1:删除,0:停用,1:启用)',

    `create_time` timestamp not null default current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_dict_data` primary key (`id`),
    constraint `uk_sys_dict_data_value` unique key (`type`,`val`),
    constraint `fk_sys_dict_data_t` foreign key(`type`) references tbl_sys_dict_type(`type`)
) engine=InnoDB default charset=utf8mb4 comment '字典数据表';
-- --------------------------------------------------------------------------------------------------------------------
-- 13.参数配置表
drop table if exists tbl_sys_config;
create table tbl_sys_config (
    `id`    bigint unsigned  not null    comment '参数ID',
    `name`  varchar(128) not null        comment '参数名称',
    `key`   varchar(128) not null        comment '参数键名',
    `val`   varchar(512) not null        comment '参数键值',
    `type`  tinyint unsigned default 0   comment '系统内置(0:是,1:否)',

    `status` tinyint default 1  comment '状态(-1:删除,0:停用,1:启用)',

    `create_time` timestamp not null default current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_config` primary key(`id`),
    constraint `uk_sys_config_key` unique key(`key`)
) engine=InnoDB default charset=utf8mb4 comment '参数配置表';
-- --------------------------------------------------------------------------------------------------------------------
-- 14.系统访问记录
drop table if exists tbl_sys_login_log;
create table tbl_sys_login_log (
    `id`            bigint unsigned  not null    comment '访问ID',
    `account`       varchar(128) not null        comment '用户账号',
    `ip_addr`       varchar(32)  default null    comment '登录IP地址',
    `ip_location`   varchar(128) default null    comment '登录地点',
    `browser`       varchar(128) default null    comment '浏览器类型',
    `os`            varchar(128) default null    comment '操作系统',
    `device`        varchar(128) default null    comment '客户端设备',
    `msg`           varchar(255) default null    comment '提示消息',

    `status` tinyint default 1  comment '状态(-1:删除,0:失败,1:成功)',

    `create_time` timestamp not null default current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_login_log` primary key(`id`)
) engine=InnoDB default charset=utf8mb4 comment '参数配置表';
-- --------------------------------------------------------------------------------------------------------------------
-- 15.操作记录表
drop table if exists tbl_sys_opera_log;
create table tbl_sys_opera_log (
    `id`        bigint unsigned  not null    comment '操作ID',
    `title`     varchar(128) default null    comment '模块标题',
    `type`      tinyint unsigned default 0   comment '业务类型(0:查询,1:新增,2:修改,3:删除,4:其它)',
    `method`    varchar(255) default null    comment '方法名称',
    `url`       varchar(512) default null    comment '请求URL',
    `req_method` varchar(32) default null    comment '请求方式',

    `opera_name`     varchar(64)  default null  comment '操作人员',
    `opera_ip`       varchar(32)  default null  comment '操作IP地址',
    `opera_location` varchar(128) default null  comment '操作地址',
    `opera_device`   varchar(128) default null  comment '操作设备',
    `opera_param`            text default null  comment '请求参数',
    `opera_result`           text default null  comment '返回参数',
    `error_msg`              text default null  comment '错误消息',

    `status` tinyint default 1  comment '状态(-1:删除,0:失败,1:成功)',

    `create_time` timestamp not null default current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_opera_log` primary key(`id`)
) engine=InnoDB default charset=utf8mb4 comment '系统操作记录';
-- --------------------------------------------------------------------------------------------------------------------
-- --------------------------------------------------------------------------------------------------------------------
