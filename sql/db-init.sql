-- 用户表
drop table user;
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    user_account  varchar(256)                           not null comment '账号',
    user_password varchar(512)                           not null comment '密码',
    union_id      varchar(256)                           null comment '微信开放平台id',
    mp_open_id     varchar(256)                           null comment '公众号openId',
    user_name     varchar(256)                           null comment '用户昵称',
    user_avatar   varchar(1024)                          null comment '用户头像',
    user_profile  varchar(512)                           null comment '用户简介',
    user_role     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除',
    index idx_union_id (union_id)
) comment '用户' collate = utf8mb4_unicode_ci;

drop table app;
-- 应用表
create table if not exists app
(
    id              bigint auto_increment comment 'id' primary key,
    app_name         varchar(128)                       not null comment '应用名',
    app_desc         varchar(2048)                      null comment '应用描述',
    app_icon         varchar(1024)                      null comment '应用图标',
    app_type         tinyint  default 0                 not null comment '应用类型（0-得分类，1-测评类）',
    scoring_strategy tinyint  default 0                 not null comment '评分策略（0-自定义，1-AI）',
    review_status    int      default 0                 not null comment '审核状态：0-待审核, 1-通过, 2-拒绝',
    review_message   varchar(512)                       null comment '审核信息',
    reviewer_id      bigint                             null comment '审核人 id',
    review_time      datetime                           null comment '审核时间',
    user_id          bigint                             not null comment '创建用户 id',
    create_time      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete        tinyint  default 0                 not null comment '是否删除',
    index idx_app_name (app_name)
) comment '应用' collate = utf8mb4_unicode_ci;

-- 题目表
create table if not exists question
(
    id              bigint auto_increment comment 'id' primary key,
    question_content text                               null comment '题目内容（json格式）',
    app_id           bigint                             not null comment '应用 id',
    user_id          bigint                             not null comment '创建用户 id',
    create_time      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete        tinyint  default 0                 not null comment '是否删除',
    index idx_app_id (app_id)
) comment '题目' collate = utf8mb4_unicode_ci;

drop table scoring_result;
-- 评分结果表
create table if not exists scoring_result
(
    id               bigint auto_increment comment 'id' primary key,
    result_name       varchar(128)                       not null comment '结果名称，如物流师',
    result_desc       text                               null comment '结果描述',
    result_picture    varchar(1024)                      null comment '结果图片',
    result_prop       varchar(128)                       null comment '结果属性集合 JSON，如 [I,S,T,J]',
    result_score_range int                                null comment '结果得分范围，如 80，表示 80及以上的分数命中此结果',
    app_id            bigint                             not null comment '应用 id',
    user_id           bigint                             not null comment '创建用户 id',
    create_time       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete         tinyint  default 0                 not null comment '是否删除',
    index idx_app_id (app_id)
) comment '评分结果' collate = utf8mb4_unicode_ci;

-- 用户答题记录表
create table if not exists user_answer
(
    id              bigint auto_increment primary key,
    app_id           bigint                             not null comment '应用 id',
    app_type         tinyint  default 0                 not null comment '应用类型（0-得分类，1-角色测评类）',
    scoring_strategy tinyint  default 0                 not null comment '评分策略（0-自定义，1-AI）',
    choices         text                               null comment '用户答案（JSON 数组）',
    result_id        bigint                             null comment '评分结果 id',
    result_name      varchar(128)                       null comment '结果名称，如物流师',
    result_desc      text                               null comment '结果描述',
    result_picture   varchar(1024)                      null comment '结果图标',
    result_score     int                                null comment '得分',
    user_id          bigint                             not null comment '用户 id',
    create_time      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete        tinyint  default 0                 not null comment '是否删除',
    index idx_app_id (app_id),
    index idx_user_id (user_id)
) comment '用户答题记录' collate = utf8mb4_unicode_ci;