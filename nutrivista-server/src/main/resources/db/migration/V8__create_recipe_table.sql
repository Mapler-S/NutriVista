-- V8: 创建菜谱完整信息表
-- tags / ingredients / steps / nutrition_summary / suitable_for 使用 MySQL JSON 类型存储

CREATE TABLE IF NOT EXISTS recipe (
    id                  VARCHAR(64)     NOT NULL,
    name                VARCHAR(128)    NOT NULL,
    name_en             VARCHAR(256),
    cuisine             VARCHAR(32),
    category            VARCHAR(32),
    sub_category        VARCHAR(32),
    difficulty          VARCHAR(16),
    prep_time_minutes   INT,
    cook_time_minutes   INT,
    servings            INT,
    calories_per_serving INT,
    tags                JSON,
    ingredients         JSON,
    steps               JSON,
    tips                TEXT,
    nutrition_summary   JSON,
    suitable_for        JSON,
    description         TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_cuisine   (cuisine),
    INDEX idx_category  (category),
    INDEX idx_calories  (calories_per_serving)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
