-- ---------------------------------------------------------------------------------------------------------------------
-- 1.0-系统模块
-- ---------------------------------------------------------------------------------------------------------------------
-- 删除表
-- 4.用户岗位关联表
drop table if exists tbl_sys_user_posts;
-- 3.岗位表
drop table if exists tbl_sys_post;
-- 2.用户表
drop table if exists tbl_sys_user;
-- ---------------------------------------------------------------------------------------------------------------------
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
) engine=InnoDB default charset=utf8mb4 comment '系统模块_部门表';
-- ---------------------------------------------------------------------------------------------------------------------
-- 初始化-部门数据
insert into tbl_sys_dept(`id`,`code`,`name`,`parent_id`) values(100, 0, '系统平台', 0);
-- ---------------------------------------------------------------------------------------------------------------------
-- 2.用户表
drop table if exists tbl_sys_user;
create table tbl_sys_user (
    `id`        bigint unsigned not null    comment '用户ID',
    `name`      varchar(32) default null    comment '用户姓名',
    `account`   varchar(32) not null        comment '用户账号',
    `passwd`    varchar(64) default null    comment '登录密码',
    `mobile`    varchar(32) default null    comment '手机号码',
    `email`     varchar(255) default null   comment '邮箱地址',

    `dept_id`   bigint unsigned not null    comment '所属部门ID',

    `status`  tinyint not null default 1    comment '状态(-1:删除,0:停用,1:启用)',

    `create_time` timestamp   not null default current_timestamp comment '创建时间',
    `update_time` timestamp   not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_user` primary key (`id`),
    constraint `uk_sys_user_account` unique key (`account`),
    constraint `fk_sys_user_dept` foreign key(`dept_id`) references tbl_sys_dept(`id`)
) engine=InnoDB default charset=utf8mb4 comment '系统模块_用户表';
-- ---------------------------------------------------------------------------------------------------------------------
-- 初始化账号
set @account = 'admin';
delete from tbl_sys_user where account = @account;
set @passwd = '123456';
set @deptId = 100
insert into tbl_sys_user(`dept_id`,`id`,`name`,`account`,`passwd`) values
(@deptId,100101, '管理员', @account, md5(concat(@passwd, @account)));
-- ---------------------------------------------------------------------------------------------------------------------
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
) engine=InnoDB default charset=utf8mb4 comment '系统模块_岗位表';
-- ---------------------------------------------------------------------------------------------------------------------
-- 4.用户岗位关联表
drop table if exists tbl_sys_user_posts;
create table tbl_sys_user_posts (
    `user_id`   bigint unsigned not null comment '用户ID',
    `post_id`   bigint unsigned not null comment '岗位ID',

    constraint `pk_sys_user_posts` primary key(`user_id`, `post_id`),
    constraint `fk_sys_user_posts_u` foreign key(`user_id`) references tbl_sys_user(`id`),
    constraint `fk_sys_user_posts_p` foreign key(`post_id`) references tbl_sys_post(`id`)
) engine=InnoDB default charset=utf8mb4 comment '系统模块_用户岗位关联表';
-- ---------------------------------------------------------------------------------------------------------------------
-- 5.角色表
drop table if exists tbl_sys_role;
create table tbl_sys_role (
    `id`    bigint unsigned not null comment '角色ID',
    `code`  int unsigned default 0   comment '角色代码(排序)',
    `name`      varchar(32) not null comment '角色名称',
    `abbr`      varchar(32) not null comment '角色简称',

    `data_scope` tinyint unsigned default 0   comment '数据范围(0:未数据授权,1:全部数据权限,2:自定数据权限,3:本部门数据权限,4:部门及以下数据权限)',
    `menu_check` tinyint unsigned default 1   comment '菜单树选择是否关联显示',
    `dept_check` tinyint unsigned default 1   comment '部门树选择是否关联显示',

    `status`  tinyint not null default 1 comment '状态(-1:删除,0:停用,1:启用)',

    `create_time` timestamp   not null default current_timestamp comment '创建时间',
    `update_time` timestamp   not null default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_sys_role` primary key(`id`),
    constraint `uk_sys_role_name` unique key(`name`)
) engine=InnoDB default charset=utf8mb4 comment '系统模块_角色表';
-- ---------------------------------------------------------------------------------------------------------------------
