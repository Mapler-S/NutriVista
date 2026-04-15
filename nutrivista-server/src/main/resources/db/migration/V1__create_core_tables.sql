-- ================================================================
-- NutriVista V1: 核心表结构 (食物分类、食物、营养成分、用户)
-- ================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------
-- 食物分类表（两级结构：大类 → 子类）
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS food_category (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    parent_id   INT          NULL     COMMENT 'NULL=顶级大类，非NULL=子类',
    name_zh     VARCHAR(80)  NOT NULL COMMENT '中文名称',
    name_en     VARCHAR(150) NOT NULL COMMENT '英文名称',
    icon        VARCHAR(20)  NULL     COMMENT 'Emoji图标',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序权重（越小越靠前）',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    INDEX idx_parent (parent_id),
    INDEX idx_sort (parent_id, sort_order),
    CONSTRAINT fk_cat_parent FOREIGN KEY (parent_id) REFERENCES food_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='食物分类表（两级：大类-子类）';

-- ----------------------------------------------------------------
-- 食物主表
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS food (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '食物ID',
    category_id  INT          NOT NULL COMMENT '所属子分类ID',
    name_zh      VARCHAR(200) NOT NULL COMMENT '中文名（含状态，如"鸡胸肉（烤）"）',
    name_en      VARCHAR(350) NOT NULL COMMENT '英文名',
    alias        VARCHAR(500) NULL     COMMENT '别名列表，逗号分隔，扩展搜索召回',
    data_source  VARCHAR(30)  NOT NULL DEFAULT 'CNF'  COMMENT '数据来源：CNF/USDA/CUSTOM',
    is_active    TINYINT(1)   NOT NULL DEFAULT 1      COMMENT '是否启用',
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    INDEX  idx_category (category_id),
    INDEX  idx_name_zh  (name_zh(50)),
    INDEX  idx_active   (is_active),
    FULLTEXT INDEX ft_food_search (name_zh, name_en, alias) WITH PARSER ngram
        COMMENT 'ngram全文索引，支持中文任意子串搜索',
    CONSTRAINT fk_food_category FOREIGN KEY (category_id) REFERENCES food_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='食物主表（8000+种）';

-- ----------------------------------------------------------------
-- 食物营养成分表（与 food 共享主键，1:1）
-- 所有值基于每 100g 可食部分，null 表示数据缺失
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS food_nutrition (
    food_id              BIGINT        NOT NULL COMMENT '食物ID（同时为PK和FK）',

    -- === 基础宏量营养素 ===
    energy_kcal          DECIMAL(8,2)  NULL COMMENT '能量 (kcal)',
    energy_kj            DECIMAL(8,2)  NULL COMMENT '能量 (kJ)',
    water                DECIMAL(8,2)  NULL COMMENT '水分 (g)',
    protein              DECIMAL(8,2)  NULL COMMENT '蛋白质 (g)',
    fat                  DECIMAL(8,2)  NULL COMMENT '脂肪 (g)',
    carbohydrate         DECIMAL(8,2)  NULL COMMENT '碳水化合物 (g)',
    fiber                DECIMAL(8,2)  NULL COMMENT '膳食纤维 (g)',
    sugar                DECIMAL(8,2)  NULL COMMENT '糖 (g)',
    starch               DECIMAL(8,2)  NULL COMMENT '淀粉 (g)',
    ash                  DECIMAL(8,2)  NULL COMMENT '灰分 (g)',

    -- === 脂肪酸 ===
    saturated_fat        DECIMAL(8,2)  NULL COMMENT '饱和脂肪酸 (g)',
    monounsaturated_fat  DECIMAL(8,2)  NULL COMMENT '单不饱和脂肪酸 (g)',
    polyunsaturated_fat  DECIMAL(8,2)  NULL COMMENT '多不饱和脂肪酸 (g)',
    trans_fat            DECIMAL(8,2)  NULL COMMENT '反式脂肪酸 (g)',
    cholesterol          DECIMAL(8,2)  NULL COMMENT '胆固醇 (mg)',

    -- === 矿物质 ===
    calcium              DECIMAL(8,2)  NULL COMMENT '钙 (mg)',
    iron                 DECIMAL(8,3)  NULL COMMENT '铁 (mg)',
    magnesium            DECIMAL(8,2)  NULL COMMENT '镁 (mg)',
    phosphorus           DECIMAL(8,2)  NULL COMMENT '磷 (mg)',
    potassium            DECIMAL(8,2)  NULL COMMENT '钾 (mg)',
    sodium               DECIMAL(8,2)  NULL COMMENT '钠 (mg)',
    zinc                 DECIMAL(8,3)  NULL COMMENT '锌 (mg)',
    copper               DECIMAL(8,4)  NULL COMMENT '铜 (mg)',
    manganese            DECIMAL(8,4)  NULL COMMENT '锰 (mg)',
    selenium             DECIMAL(8,3)  NULL COMMENT '硒 (μg)',
    iodine               DECIMAL(8,3)  NULL COMMENT '碘 (μg)',
    chromium             DECIMAL(8,3)  NULL COMMENT '铬 (μg)',

    -- === 脂溶性维生素 ===
    vitamin_a            DECIMAL(10,2) NULL COMMENT '维生素A (μg RAE)',
    beta_carotene        DECIMAL(10,2) NULL COMMENT 'β-胡萝卜素 (μg)',
    vitamin_d            DECIMAL(8,3)  NULL COMMENT '维生素D (μg)',
    vitamin_e            DECIMAL(8,2)  NULL COMMENT '维生素E (mg α-TE)',
    vitamin_k            DECIMAL(8,2)  NULL COMMENT '维生素K (μg)',

    -- === 水溶性维生素 ===
    vitamin_c            DECIMAL(8,2)  NULL COMMENT '维生素C (mg)',
    vitamin_b1           DECIMAL(8,3)  NULL COMMENT '维生素B1/硫胺素 (mg)',
    vitamin_b2           DECIMAL(8,3)  NULL COMMENT '维生素B2/核黄素 (mg)',
    vitamin_b3           DECIMAL(8,2)  NULL COMMENT '维生素B3/烟酸 (mg)',
    vitamin_b5           DECIMAL(8,3)  NULL COMMENT '维生素B5/泛酸 (mg)',
    vitamin_b6           DECIMAL(8,3)  NULL COMMENT '维生素B6 (mg)',
    vitamin_b12          DECIMAL(10,3) NULL COMMENT '维生素B12 (μg)',
    folate               DECIMAL(8,2)  NULL COMMENT '叶酸 (μg DFE)',
    biotin               DECIMAL(8,3)  NULL COMMENT '生物素B7 (μg)',

    PRIMARY KEY (food_id),
    CONSTRAINT fk_nutrition_food FOREIGN KEY (food_id) REFERENCES food(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='食物营养成分表（每100g可食部）';

-- ----------------------------------------------------------------
-- 用户表（预留多用户扩展，当前阶段使用 id=1 演示账号）
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_user (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    username       VARCHAR(50)  NOT NULL COMMENT '用户名',
    email          VARCHAR(200) NULL     COMMENT '邮箱（预留）',
    display_name   VARCHAR(100) NULL     COMMENT '显示名称',
    avatar         VARCHAR(500) NULL     COMMENT '头像URL',
    gender         TINYINT      NULL     COMMENT '性别：1=男，2=女',
    birth_year     SMALLINT     NULL     COMMENT '出生年份',
    height_cm      DECIMAL(5,1) NULL     COMMENT '身高(cm)',
    weight_kg      DECIMAL(5,1) NULL     COMMENT '体重(kg)',
    activity_level VARCHAR(20)  NOT NULL DEFAULT 'MODERATE'
        COMMENT '活动水平：SEDENTARY/LIGHT/MODERATE/ACTIVE/VERY_ACTIVE',
    dietary_goal   VARCHAR(20)  NOT NULL DEFAULT 'MAINTAIN'
        COMMENT '饮食目标：LOSE/MAINTAIN/GAIN',
    created_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户表（预留多用户，当前默认单用户 id=1）';

-- 初始化演示用户
INSERT INTO app_user (id, username, display_name, gender, birth_year, height_cm, weight_kg)
VALUES (1, 'demo', 'NutriVista 演示用户', 1, 1994, 175.0, 70.0)
ON DUPLICATE KEY UPDATE id = id;

SET FOREIGN_KEY_CHECKS = 1;
