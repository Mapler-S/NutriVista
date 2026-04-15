-- V9: 菜谱使用日志表（记录推荐事件和采用事件）
CREATE TABLE recipe_usage_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    recipe_id   VARCHAR(64)  NOT NULL,
    recipe_name VARCHAR(200) NOT NULL,
    cuisine     VARCHAR(50),
    event_type  ENUM('RECOMMEND','ADOPTED') NOT NULL COMMENT 'RECOMMEND=被推荐展示，ADOPTED=用户实际添加到饮食记录',
    meal_date   DATE         NOT NULL        COMMENT '推荐/采用发生的日期',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_user_date      (user_id, meal_date),
    INDEX idx_user_type_date (user_id, event_type, meal_date),
    INDEX idx_cuisine        (cuisine)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
