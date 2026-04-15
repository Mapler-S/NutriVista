-- ================================================================
-- NutriVista V2: 饮食记录、收藏、常用组合表
-- ================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------
-- 饮食记录表（每天每餐一条）
-- meal_type: BREAKFAST / MORNING_SNACK / LUNCH / AFTERNOON_TEA / DINNER / SUPPER
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS meal_record (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL DEFAULT 1  COMMENT '用户ID',
    meal_date  DATE        NOT NULL            COMMENT '记录日期',
    meal_type  VARCHAR(20) NOT NULL            COMMENT '餐次类型枚举',
    notes      VARCHAR(500) NULL              COMMENT '备注',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_date_type (user_id, meal_date, meal_type),
    INDEX idx_user_date (user_id, meal_date),
    INDEX idx_meal_date (meal_date),
    CONSTRAINT fk_meal_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='饮食记录表';

-- ----------------------------------------------------------------
-- 饮食记录明细表（一次饮食 → 多种食物）
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS meal_item (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    meal_id        BIGINT        NOT NULL COMMENT '所属饮食记录ID',
    food_id        BIGINT        NOT NULL COMMENT '食物ID',
    weight_gram    DECIMAL(8,2)  NOT NULL COMMENT '食用重量(g)',
    sort_order     INT           NOT NULL DEFAULT 0,
    created_at     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    INDEX idx_meal_id (meal_id),
    INDEX idx_food_id (food_id),
    CONSTRAINT fk_item_meal FOREIGN KEY (meal_id) REFERENCES meal_record(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_food FOREIGN KEY (food_id) REFERENCES food(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='饮食记录明细表';

-- ----------------------------------------------------------------
-- 用户收藏食物
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_favorite (
    user_id    BIGINT      NOT NULL,
    food_id    BIGINT      NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, food_id),
    INDEX idx_user_fav (user_id),
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_fav_food FOREIGN KEY (food_id) REFERENCES food(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户收藏食物';

-- ----------------------------------------------------------------
-- 常用食物组合（如"我的标准早餐"）
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS food_combo (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL DEFAULT 1,
    name        VARCHAR(100) NOT NULL COMMENT '组合名称',
    description VARCHAR(300) NULL,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    INDEX idx_combo_user (user_id),
    CONSTRAINT fk_combo_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='常用食物组合';

-- ----------------------------------------------------------------
-- 食物组合明细
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS food_combo_item (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    combo_id            BIGINT       NOT NULL,
    food_id             BIGINT       NOT NULL,
    default_weight_gram DECIMAL(8,2) NOT NULL DEFAULT 100.00,
    sort_order          INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_combo_id (combo_id),
    CONSTRAINT fk_ci_combo FOREIGN KEY (combo_id) REFERENCES food_combo(id) ON DELETE CASCADE,
    CONSTRAINT fk_ci_food  FOREIGN KEY (food_id)  REFERENCES food(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='食物组合明细';

SET FOREIGN_KEY_CHECKS = 1;
