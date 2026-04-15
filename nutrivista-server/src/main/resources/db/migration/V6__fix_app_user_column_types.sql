-- ================================================================
-- V6: 修正 app_user 列类型与 JPA 实体映射的不一致
-- SMALLINT/TINYINT → INT，以匹配 Java Integer 类型（Hibernate validate 要求）
-- ================================================================
ALTER TABLE app_user
    MODIFY COLUMN gender     INT NULL COMMENT '性别：1=男，2=女',
    MODIFY COLUMN birth_year INT NULL COMMENT '出生年份';
