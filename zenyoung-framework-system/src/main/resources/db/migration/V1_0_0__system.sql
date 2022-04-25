-- --------------------------------------------------------------------------------------------------------------------
-- 删除表
-- 12.字典数据表
drop table if exists tbl_sys_dict_data;
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
-- 初始化-部门数据
insert into tbl_sys_dept(`id`,`code`,`name`,`parent_id`) values(100, 0, '系统平台', 0);
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
-- 初始化账号
set @account = 'master';
delete from tbl_sys_user where account = @account;
set @passwd = '123456';
set @adminUserId = 100101;
set @deptId = 100
insert into tbl_sys_user(`dept_id`,`id`,`name`,`account`,`passwd`) values (@deptId, @adminUserId, '管理员', @account, md5(concat(@passwd, @account)));
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
-- 初始化角色
set @adminRoleId = 100;
delete from tbl_sys_role where id = @adminRoleId;
insert into tbl_sys_role(`id`,`name`,`abbr`,`data_scope`) values (@adminRoleId, '系统管理员', 'admin', 1);
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
-- 初始化默认数据
delete from tbl_sys_user_roles where user_id = @adminUserId and role_id = @adminRoleId;
insert into tbl_sys_user_roles(`user_id`,`role_id`) value (@adminUserId, @adminRoleId);
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
-- 初始化菜单表数据
-- 一级菜单
insert into tbl_sys_menu(`id`,`code`,`name`,`parent_id`,`path`,`component`,`is_link`,`is_cache`,`type`,`visible`,`perms`,`icon`) values
(1, 1, '系统管理', null, 'sys', null, 0, 0, 1, 1, null, 'system'),
(2, 2, '系统监控', null, 'monitor', null, 0, 0, 1, 1, null, 'monitor'),
(3, 3, '系统工具', null, 'tool', null, 0, 0, 1, 1, null, 'tool'),
-- 二级菜单
(100, 1, '用户管理', 1, 'user', 'sys/user/index', 0, 0, 2, 1, 'sys:user:list', 'user'),
(101, 2, '角色管理', 1, 'role', 'sys/role/index', 0, 0, 2, 1, 'sys:role:list', 'peoples'),
(102, 3, '菜单管理', 1, 'menu', 'sys/menu/index', 0, 0, 2, 1, 'sys:menu:list', 'tree-table'),
(103, 4, '部门管理', 1, 'dept', 'sys/dept/index', 0, 0, 2, 1, 'sys:dept:list', 'tree'),
(104, 5, '岗位管理', 1, 'post', 'sys/post/index', 0, 0, 2, 1, 'sys:post:list', 'post'),
(105, 6, '字典管理', 1, 'dict', 'sys/dict/index', 0, 0, 2, 1, 'sys:dict:list', 'dict'),
(106, 7, '参数设置', 1, 'config', 'sys/config/index', 0, 0, 2, 1, 'sys:config:list', 'edit'),
(108, 9, '日志管理', 1, 'log', 'sys/log/index', 0, 0, 2, 1, 'sys:log:list', 'log'),
(200, 1, '在线用户', 2, 'online', 'monitor/online/index',0, 0, 2, 1, 'monitor:online:list', 'online'),
(201, 2, '数据监控', 2, 'druid', 'monitor/druid/index', 0, 0, 2, 1, 'monitor:druid:list', 'druid'),
(202, 3, '服务监控', 2, 'server', 'monitor/server/index', 0, 0, 2, 1, 'monitor:server:list', 'server'),
(203, 4, '缓存监控', 2, 'cache', 'monitor/cache/index', 0, 0, 2, 1, 'monitor:cache:list', 'redis'),
(301, 1, '表单构建', 3, 'build', 'tool/build/index', 0, 0, 2, 1, 'tool:build:list', 'build'),
(302, 2, '代码生成', 3, 'gen', 'tool/gen/index', 0, 0, 2, 1, 'tool:gen:list', 'code'),
(303, 3, '系统接口', 3, 'swagger', 'tool/swagger/index', 0, 0, 2, 1, 'tool:swagger:list','swagger'),
-- 三级菜单
(10801, 1, '操作日志', 108, 'operaLog', 'monitor/operaLog/index', 0, 0, 2, 1, 'monitor:operaLog:list', 'form'),
(10802, 2, '登录日志', 108, 'loginLog', 'monitor/loginLog/index', 0, 0, 2, 1, 'monitor:loginLog:list', 'loginInfo'),
-- 用户管理按钮
(10001, 1, '用户查询', 100, null, null, 0, 0, 3, 1, 'sys:user:query','#'),
(10002, 2, '用户新增', 100, null, null, 0, 0, 3, 1, 'sys:user:add', '#'),
(10003, 3, '用户修改', 100, null, null, 0, 0, 3, 1, 'sys:user:edit', '#'),
(10004, 4, '用户删除', 100, null, null, 0, 0, 3, 1, 'sys:user:del', '#'),
(10005, 5, '重置密码', 100, null, null, 0, 0, 3, 1, 'sys:user:resetPwd', '#'),
-- 角色管理按钮
(10101, 1, '角色查询', 101, null, null, 0, 0, 3, 1, 'sys:role:query', '#'),
(10102, 2, '角色新增', 101, null, null, 0, 0, 3, 1, 'sys:role:add', '#'),
(10103, 3, '角色修改', 101, null, null, 0, 0, 3, 1, 'sys:role:edit', '#'),
(10104, 4, '角色删除', 101, null, null, 0, 0, 3, 1, 'sys:role:del', '#'),
-- 菜单管理按钮
(10201, 1, '菜单查询', 102, null, null, 0, 0, 3, 1, 'sys:menu:query', '#'),
(10202, 2, '菜单新增', 102, null, null, 0, 0, 3, 1, 'sys:menu:add', '#'),
(10203, 3, '菜单修改', 102, null, null, 0, 0, 3, 1, 'sys:menu:edit', '#'),
(10204, 4, '菜单删除', 102, null, null, 0, 0, 3, 1, 'sys:menu:del', '#'),
-- 部门管理按钮
(10301, 1, '部门查询', 103, null, null, 0, 0, 3, 1, 'sys:dept:query', '#'),
(10302, 2, '部门新增', 103, null, null, 0, 0, 3, 1, 'sys:dept:add', '#'),
(10303, 3, '部门修改', 103, null, null, 0, 0, 3, 1, 'sys:dept:edit', '#'),
(10304, 4, '部门删除', 103, null, null, 0, 0, 3, 1, 'sys:dept:del','#'),
-- 岗位管理按钮
(10401, 1, '岗位查询', 104, null, null, 0, 0, 3, 1, 'sys:post:query', '#'),
(10402, 2, '岗位新增', 104, null, null, 0, 0, 3, 1, 'sys:post:add', '#'),
(10403, 3, '岗位修改', 104, null, null, 0, 0, 3, 1, 'sys:post:edit', '#'),
(10404, 4, '岗位删除', 104, null, null, 0, 0, 3, 1, 'sys:post:del', '#'),
-- 字典管理按钮
(10501, 1, '字典查询', 105, null, null, 0, 0, 3, 1, 'sys:dict:query', '#'),
(10502, 2, '字典新增', 105, null, null, 0, 0, 3, 1, 'sys:dict:add', '#'),
(10503, 3, '字典修改', 105, null, null, 0, 0, 3, 1, 'sys:dict:edit','#'),
(10504, 4, '字典删除', 105, null, null, 0, 0, 3, 1, 'sys:dict:del', '#'),
-- 参数设置按钮
(10601, 1, '参数查询', 106, null, null, 0, 0, 3, 1, 'sys:config:query', '#'),
(10602, 2, '参数新增', 106, null, null, 0, 0, 3, 1, 'sys:config:add', '#'),
(10603, 3, '参数修改', 106, null, null, 0, 0, 3, 1, 'sys:config:edit', '#'),
(10604, 4, '参数删除', 106, null, null, 0, 0, 3, 1, 'sys:config:edit', '#'),
-- 操作日志按钮
(10811, 1, '操作查询', 10801, null, null, 0, 0, 3, 1, 'monitor:operaLog:query', '#'),
(10821, 2, '操作删除', 10801, null, null, 0, 0, 3, 1, 'monitor:operaLog:del', '#'),
-- 登录日志按钮
(10812, 1, '登录查询', 10802, null, null, 0, 0, 3, 1, 'monitor:loginLog:query','#'),
(10822, 2, '登录删除', 10802, null, null, 0, 0, 3, 1, 'monitor:loginLog:del', '#'),
-- 在线用户按钮
(20001, 1, '在线查询', 200, null, null, 0, 0, 3, 1, 'monitor:online:query', '#'),
(20002, 2, '批量强退', 200, null, null, 0, 0, 3, 1, 'monitor:online:batchLogout', '#'),
(20003, 3, '单条强退', 200, null, null , 0, 0, 3, 1, 'monitor:online:forceLogout', '#');
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
-- 初始化角色菜单
delete from tbl_sys_role_menus where role_id = @adminRoleId;
insert into tbl_sys_role_menus(`role_id`, `menu_id`)
select @adminRoleId, id
from tbl_sys_menu;
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
-- 初始化字典类型
delete from tbl_sys_dict_type where `type` in ('sys_gender','sys_show_hide','sys_normal_disable','sys_notice_type','sys_notice_status','sys_common_status');
--
insert into tbl_sys_dict_type(`id`,`name`,`type`) values
(1, '性别', 'sys_gender'),
(2, '菜单状态', 'sys_show_hide'),
(3, '系统开关', 'sys_normal_disable'),
(4, '通知类型', 'sys_notice_type'),
(5, '通知状态', 'sys_notice_status'),
(6, '系统状态', 'sys_common_status');
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
-- 初始化字典数据
insert into tbl_sys_dict_data(`id`,`code`,`label`,`value`,`is_default`,`type`,`css_class`,`list_class`) values
(101, 1, '未知', '0', 0, 'sys_gender', '', ''),(102, 2, '男', '1', 1, 'sys_gender', '', ''),(103, 3, '女', '2', 0, 'sys_gender', '', ''),
(201, 1, '显示', '1', 1, 'sys_show_hide', '', 'primary'),(202, 2, '隐藏', '0', 0, 'sys_show_hide', '', 'danger'),
(301, 1, '正常', '1', 1, 'sys_normal_disable', '', 'primary'),(302, 2, '停用', '0', 0, 'sys_normal_disable', '', 'danger'),
(401, 1, '通知', '1', 1, 'sys_notice_type', '', 'warning'),(402, 2, '公告', '2', 0, 'sys_notice_type', '', 'success'),
(501, 1, '正常', '1', 1, 'sys_notice_status', '', 'primary'),(502, 2, '关闭', '2', 0, 'sys_notice_status', '', 'danger'),
(601, 1, '启用', '1', 1, 'sys_common_status', '', 'primary'),(602, 2, '停用', '0', 0, 'sys_common_status', '', 'danger'),
(603, 3, '删除', '-1', 0, 'sys_common_status', '', 'warning');
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
-- 初始化系统配置参数
insert into tbl_sys_config(`id`,`name`,`key`,`val`,`type`) values
(1,'主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 1),
(2,'主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 1),
(3,'用户管理-账号初始密码', 'sys.user.initPassword', '123456', 1),
(4,'账号自助-验证码开关', 'sys.account.captchaOnOff', 'true', 1),
(5,'账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 1);
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
