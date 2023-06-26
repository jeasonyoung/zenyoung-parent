-- ------------------------------------------------------------------------------------------------------------------
-- 创建_分段ID配置表-数据库脚本
-- ------------------------------------------------------------------------------------------------------------------
drop table if exists segment_id;
create table segment_id (
    `biz_type`  varchar(64) not null comment '业务类型',
    `max_id`    bigint unsigned default 0 comment '当前最大ID',
    `step`      bigint unsigned default 1 comment '步长',
    `delta`     int unsigned default 1 comment 'ID每次增量',
    `version`   bigint unsigned default 0 comment '版本号',

    `created_at` timestamp default current_timestamp comment '创建时间',
    `updated_at` timestamp default current_timestamp on update current_timestamp comment '更新时间',

    constraint `pk_segment_id` primary key(`biz_type`),
    index `idx_segment_id_max`(`max_id`)
) engine=InnoDB default charset=utf8mb4 comment '分段ID配置表';
-- ------------------------------------------------------------------------------------------------------------------
