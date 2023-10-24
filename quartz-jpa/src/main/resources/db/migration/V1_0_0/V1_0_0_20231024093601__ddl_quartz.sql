-- --------------------------------------------------------------------------------------------------------------------
-- 定时任务
-- --------------------------------------------------------------------------------------------------------------------
-- 1.定时任务表
drop table if exists `tbl_quartz_task`;
create table `tbl_quartz_task` (
     `id`   bigint unsigned not null comment '定时任务ID',
     `name`    varchar(128) not null comment '定时任务名称',
     `cron`    varchar(128) not null comment '定时Cron表达式',

     `job_class` varchar(512) not null comment '任务',

) engine=InnoDB default charset = utf8mb4 comment '定时任务表';
-- --------------------------------------------------------------------------------------------------------------------
