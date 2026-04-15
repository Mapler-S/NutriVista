-- ================================================================
-- NutriVista V7: 食物种子数据 Part 3
-- 新增约 72 种常见食物，补充各分类品种不足
-- 数据来源：
--   CNF  = 中国食物成分表 第6版（中国疾控中心营养与食品安全所，2018）
--   USDA = USDA FoodData Central（2023版）
-- 所有营养值基于每 100g 可食部分
-- ID 范围：354 – 425
-- ================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ================================================================
-- 补充谷物及杂粮（category_id 104/105）
-- ================================================================

INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(354,104,'薏米仁（生）',   'Coix seed (Job''s tears), raw',  'CNF'),
(355,104,'小米粥（熟）',   'Millet congee, cooked',          'CNF'),
(356,105,'黄米（糜子，生）','Proso millet, raw',              'CNF'),
(357,104,'藜麦饭（熟）',   'Quinoa, cooked',                 'USDA'),
(358,103,'玉米饼（熟）',   'Cornbread/corn pancake, cooked', 'CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_b1,vitamin_b2,vitamin_b3,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(354,357,12.8,3.3,71.1,0.5,0.2,10.5, 0.220,0.150,2.50, 42,3.6, 88,217,231, 4,1.7, 3.1),
(355, 46, 1.4,0.7, 8.4,0.6,0.1,88.0, 0.020,0.010,0.30,  4,0.3,  8, 22, 35, 2,0.3, 1.0),
(356,356, 9.7,1.5,75.7,4.4,0.4,11.5, 0.290,0.100,1.50, 19,4.3,120,279,233, 6,1.9, 3.2),
(357,120, 4.4,1.9,21.3,2.8,0.9,71.6, 0.054,0.049,0.75, 17,1.5, 64,152,172, 7,1.1, 5.1),
(358,200, 4.2,3.5,38.1,2.0,2.5,53.0, 0.060,0.030,0.70, 12,0.8, 18, 82,118,20,0.5, 3.0);

-- ================================================================
-- 补充蔬菜类
-- ================================================================

-- 豆芽及芽苗菜（归入豆类蔬菜 405）
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(359,405,'绿豆芽（生）','Mung bean sprouts, raw','CNF'),
(360,405,'黄豆芽（生）','Soybean sprouts, raw','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(359,18,1.7,0.1,3.4,0.7,1.1,95.3,  1,0.040,0.080,0.60,  8,23,0.90,21, 37,149, 4,0.35),
(360,44,4.5,1.6,4.5,1.5,2.0,90.0,  5,0.040,0.110,0.80,  8,21,0.90,21, 74,  7, 0,0.40);

-- 根茎及茎菜（402）
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(361,402,'芦笋（生）',     'Asparagus, raw',          'USDA'),
(362,402,'冬笋（生）',     'Winter bamboo shoot, raw','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(361,20,2.2,0.1,3.9,2.1,1.9,93.2, 38,0.143,0.141,0.98,  5,52,24,2.14,14, 52,202, 2,0.54),
(362,40,4.1,0.1,7.7,1.0,1.5,87.4, 30,0.080,0.100,0.50,  4, 7,22,0.70, 8, 56,490, 6,0.61);

-- 叶菜（401）
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(363,401,'豌豆苗（生）',   'Pea shoots, raw',       'CNF'),
(364,401,'芥菜（叶，生）', 'Mustard greens, raw',   'USDA'),
(365,401,'马齿苋（生）',   'Purslane, raw',         'USDA'),
(366,401,'香椿（生）',     'Chinese toon buds, raw','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(363, 38,3.8,0.4,6.0,2.0,1.0,89.5,195,0.290,0.160,2.40,40,118,43,1.80,33,118,370,  5,0.63),
(364, 27,2.9,0.2,4.9,3.2,1.8,91.7,295,0.080,0.110,0.80,70,187,103,1.50,32, 58,354, 21,0.25),
(365, 14,1.3,0.1,2.7,0.7,1.8,94.8, 26,0.047,0.113,0.48,21, 14, 65,1.99,29, 44,494, 45,0.17),
(366, 47,1.7,0.4,10.9,1.8,0.4,84.0, 34,0.050,0.120,0.90,40, 93, 96,3.90,36,147,461,  4,0.42);

-- 葱蒜类（406）
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(367,406,'蒜苗（青蒜，生）','Garlic stems/greens, raw','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(367,40,2.5,0.4,7.8,1.8,1.0,88.0, 0.060,0.100,0.80,35,29,1.2,18,44,179,5,0.22);

-- ================================================================
-- 补充水果类
-- ================================================================

-- 仁果（501）
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(368,501,'柿子（生）','Persimmon, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(368,70,0.6,0.2,18.6,3.6,12.5,80.3, 81,0.030,0.020,0.10,  7, 8,0.15, 9,17,161,1,0.11);

-- 浆果（503）
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(369,503,'树莓（生）',    'Raspberry, raw',   'USDA'),
(370,503,'蔓越莓（生）',  'Cranberry, raw',   'USDA'),
(371,503,'杨梅（生）',    'Red bayberry, raw','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(369,52,1.2,0.7,11.9,6.5,4.4,85.8, 2,0.032,0.038,0.60,26,25,0.69,22,29,151,1,0.42),
(370,46,0.4,0.1,12.2,4.6,4.3,87.1, 3,0.012,0.020,0.10,14, 8,0.25, 6,13, 85,2,0.10),
(371,28,0.8,0.2, 6.7,1.0,4.5,91.9, 7,0.010,0.050,0.30, 9,14,1.00,10, 8,149,1,0.14);

-- 热带水果（505）
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(372,505,'番石榴（生）',   'Guava, raw',          'USDA'),
(373,505,'百香果（生）',   'Passion fruit, raw',  'USDA'),
(374,505,'杨桃（生）',     'Star fruit, raw',     'USDA'),
(375,505,'鳄梨（牛油果）', 'Avocado, raw',        'USDA'),
(376,505,'椰子肉（生）',   'Coconut meat, raw',   'USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(372, 68,2.6,1.0,14.3,5.4, 8.9,80.8, 31,0.067,0.040,1.08,228,49, 18,0.26,22, 40,417, 2,0.23),
(373, 97,2.2,0.7,23.4,10.4,11.2,72.9, 64,0.000,0.130,1.50, 30,14, 12,1.61,29, 68,348,28,0.10),
(374, 31,1.0,0.3, 6.7,2.8, 3.9,91.4,  3,0.014,0.016,0.40, 34, 6,  3,0.08,10, 12,133, 2,0.12),
(375,160,2.0,14.7, 8.5,6.7, 0.7,73.2,  7,0.067,0.130,1.74, 10,81, 12,0.55,29, 52,485, 7,0.64),
(376,354,3.3,33.5,15.2,9.0, 6.2,47.0,  0,0.066,0.020,0.54,  3, 8, 14,2.43,32,113,356,20,1.10);

-- ================================================================
-- 补充水产品类
-- ================================================================

-- 鱼类（801）
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(377,801,'鲅鱼（生）',     'Spanish mackerel, raw',   'CNF'),
(378,801,'黄鳝（生）',     'Rice paddy eel, raw',     'CNF'),
(379,801,'罗非鱼（生）',   'Tilapia, raw',            'USDA'),
(380,801,'鳗鱼（生）',     'Japanese eel, raw',       'USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,water,cholesterol,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_b12,vitamin_d,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(377,121,21.2,3.1,2.1,72.8, 75,0.030,0.100,3.50,3.8, 6.2, 35,0.8,32,184,395, 74,0.9,51.5),
(378, 89,18.0,1.4,1.2,78.2,186,0.060,0.980,3.70,0.7, 1.8, 42,2.5,18,206,263, 70,0.5,34.6),
(379, 96,20.1,1.7,0.0,77.5, 50,0.095,0.073,4.70,1.6, 3.5, 10,0.6,27,204,302, 56,0.4,41.8),
(380,184,18.4,11.7,0.0,68.3,126,0.234,0.045,3.54,3.5,23.3, 20,0.6,20,216,272, 51,1.6, 6.8);

-- 贝壳类（803）
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(381,803,'乌贼（墨鱼，生）','Cuttlefish, raw','CNF'),
(382,803,'鱿鱼（生）',       'Squid, raw',   'USDA'),
(383,803,'鲍鱼（生）',       'Abalone, raw', 'CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,water,cholesterol,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_b12,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(381, 83,15.2,0.9,3.4,79.2,226,0.020,0.050,2.90,5.0, 32,1.0, 66,180,400,165,1.4,37.5),
(382, 92,15.6,1.4,3.1,78.6,233,0.020,0.410,2.20,1.3, 32,0.7, 33,246,276,182,1.5,44.8),
(383, 84,17.1,0.8,3.4,77.6, 96,0.190,0.210,1.40,0.9,174,22.6,59,178,347,257,0.5,68.2);

-- 水产加工品（804）
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(384,804,'海参（干）',   'Sea cucumber, dried',   'CNF'),
(385,804,'海蜇（腌制）', 'Jellyfish, salted',     'CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,water,cholesterol,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(384,286,50.2,4.8,23.7,15.0, 66,285,13.2,149, 63, 43,4968,0.7,150.0),
(385, 33, 3.7,0.3, 3.8,90.8,  5,150,10.0,124,  6,160,6740,2.1, 15.0);

-- ================================================================
-- 补充坚果及种子（1101 / 1102）
-- ================================================================

INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(386,1101,'板栗（生）',       'Chestnut, raw',           'USDA'),
(387,1101,'松子（生）',       'Pine nut, raw',           'USDA'),
(388,1101,'莲子（干）',       'Lotus seed, dried',       'CNF'),
(389,1101,'银杏（白果，生）', 'Ginkgo nut, raw',         'CNF'),
(390,1102,'黑芝麻（生）',     'Sesame seed, black, raw', 'CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_b1,vitamin_b2,vitamin_e,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(386,245, 3.2, 2.2,52.9,5.1,10.5,41.1,0.238,0.168, 1.3, 29, 1.01, 32,107, 715,  3,0.5, 2.2),
(387,673,13.7,68.4,13.1,3.7, 3.6, 2.3,0.364,0.227, 9.3, 16, 5.53,251,575, 597,  2,6.5, 0.6),
(388,344,17.2, 2.0,67.2,3.0, 0.0,13.0,0.160,0.080, 2.7, 97, 3.60,242,550, 846,  5,2.8, 3.5),
(389,160, 4.7, 2.4,35.1,0.8, 0.0,58.9,0.060,0.080, 2.4,  8, 0.40,  8,100, 160,  3,0.5, 0.0),
(390,531,19.1,46.1,24.0,14.0, 0.0, 4.0,0.660,0.250,50.4,1266,22.70,290,516, 362,  8,6.1, 9.0);

-- ================================================================
-- 补充菌类（1201 鲜菌 / 1202 干菌）
-- ================================================================

INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(391,1201,'猴头菇（鲜）','Lion''s mane mushroom, fresh','CNF'),
(392,1201,'蟹味菇（鲜）','Shimeji mushroom, fresh',     'CNF'),
(393,1201,'鸡腿菇（鲜）','Shaggy mane mushroom, fresh', 'CNF'),
(394,1201,'白灵菇（鲜）','Abalone mushroom, fresh',     'CNF'),
(395,1202,'竹荪（干）',   'Bamboo pith fungus, dried',  'CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_d,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(391,26,2.0,0.2, 4.9,4.2,0.9,91.5, 0.010,0.290,2.80,0.2,  2,1.0,10, 43, 179,12,0.3,1.3),
(392,22,2.7,0.4, 3.4,2.7,1.3,92.0, 0.050,0.200,3.10,0.3,  1,0.5,12, 87, 380, 3,0.3,2.0),
(393,34,2.9,0.3, 6.5,1.8,2.0,89.1, 0.070,0.280,3.40,0.4,  8,0.9,11, 64, 395, 2,0.5,2.5),
(394,31,2.8,0.2, 5.9,2.3,1.4,90.4, 0.060,0.150,4.00,0.5,  5,0.6, 9, 72, 313, 6,0.6,1.5),
(395,254,19.4,2.6,60.4,25.0,1.5, 8.0, 0.050,1.000,8.00,0.0,  4,3.5,36,168,1350,86,2.5,7.8);

-- ================================================================
-- 补充调味品类（1402 酱类 / 1405 香辛料 / 1406 复合调料）
-- ================================================================

INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(396,1402,'蚝油',          'Oyster sauce',              'CNF'),
(397,1402,'辣椒酱',        'Chili sauce, cooked',       'CNF'),
(398,1405,'咖喱粉（干）',  'Curry powder, dry',         'USDA'),
(399,1406,'玉米淀粉',      'Cornstarch',                'USDA'),
(400,1406,'鸡精',          'Chicken bouillon powder',   'CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,water,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(396,105, 4.0, 0.3,23.0,0.4,65.0, 46, 3.8, 42, 89, 258,2733,0.9),
(397,165, 3.5,12.8, 9.2,3.0,68.0, 36, 2.9, 32, 78, 294,2310,0.6),
(398,325,12.7,13.8,55.8,33.2, 9.5,478,29.6,254,349,1543,  52,4.6),
(399,381, 0.3, 0.1,91.0,0.9, 7.4,  2, 0.5,  3, 13,   3,   9,0.1),
(400,260,13.0, 2.0,44.0,0.2,19.0, 28, 1.8, 36,120, 280,18980,0.5);

-- ================================================================
-- 补充饮料类（1501 茶 / 1503 果汁 / 1505 酒精饮料）
-- ================================================================

INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(401,1503,'椰子水',           'Coconut water',                  'USDA'),
(402,1505,'醪糟（甜米酒）',   'Fermented glutinous rice sweet', 'CNF'),
(403,1503,'椰汁（罐装，含糖）','Coconut milk drink, canned',    'CNF'),
(404,1501,'菊花茶（冲泡液）', 'Chrysanthemum tea, brewed',      'CNF'),
(405,1502,'速溶咖啡（三合一）','Instant coffee 3-in-1',         'CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(401,19,0.7,0.2, 3.7,1.1,2.6,94.9, 24,0.29,25,20,250,105,0.10),
(402,95,1.5,0.3,21.4,0.2,18.0,76.0,  7,0.20, 8,29, 82,  8,0.20),
(403,58,0.6,1.5,10.8,0.3,10.5,86.0, 11,0.20,12,18,102, 18,0.10),
(404, 3,0.1,0.0, 0.7,0.2, 0.0,99.0,  2,0.10, 1, 2, 15,  1,0.00),
(405,449,4.8,11.5,79.2,0.5,67.0, 2.0, 58,0.80,22,88,218,112,0.40);

-- ================================================================
-- 乳及乳制品补充（1001 / 1005 / 1003）
-- ================================================================

INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(406,1001,'羊奶（全脂）',       'Goat milk, whole',           'USDA'),
(407,1005,'炼乳（加糖）',       'Condensed milk, sweetened',  'USDA'),
(408,1003,'奶酪片（再制干酪）', 'Processed cheese slice',     'USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,cholesterol,saturated_fat,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b12,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(406, 69, 3.6, 4.1, 4.4,0,4.4,87.0, 11, 2.7, 57,0.048,0.138,0.07,134,0.05,14,111,204, 50,0.30),
(407,321, 7.9, 8.7,55.4,0,55.5,26.7, 34, 5.5, 84,0.053,0.414,0.23,284,0.16,26,218,371,127,1.00),
(408,316,15.8,25.6, 8.3,0, 5.0,47.6, 76,15.2,241,0.020,0.268,0.66,622,0.62,24,900,132,1378,2.50);

-- ================================================================
-- 常见熟食及家常食品
-- ================================================================

INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(409,1703,'猪肉包子（熟）',   'Steamed pork bun',               'CNF'),
(410,1703,'素菜包子（熟）',   'Steamed vegetable bun',          'CNF'),
(411,1803,'水煮饺子（猪肉）', 'Boiled dumplings, pork',         'CNF'),
(412,1801,'蛋炒饭',           'Egg fried rice',                 'CNF'),
(413,1801,'炒面（猪肉）',     'Stir-fried noodles, pork',       'CNF'),
(414,1801,'煎饼果子',         'Jianbing (Chinese savory crepe)','CNF'),
(415,1801,'肠粉（广式，鲜虾）','Cheung fun, shrimp',           'CNF'),
(416,1801,'馄饨（猪肉，汤）', 'Wonton soup, pork',              'CNF'),
(417,1801,'炸鸡腿',           'Fried chicken drumstick',        'USDA'),
(418,1801,'汉堡包（牛肉）',   'Hamburger, beef',                'USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,water,cholesterol,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(409,226,10.5, 5.5,34.0,1.2,48.0, 28,28,1.5,16, 98,148, 438,0.8),
(410,178, 6.8, 2.1,32.5,2.0,57.0,  0,32,1.2,14, 76,129, 292,0.6),
(411,208, 9.2, 6.3,30.1,1.2,53.0, 26,20,1.2,16, 98,148, 462,0.7),
(412,215, 5.3, 9.2,28.0,0.5,56.5, 92,14,0.5,12, 68,112, 340,0.5),
(413,205, 5.1, 7.8,30.2,1.0,55.5, 18,12,0.8,11, 58,102, 580,0.4),
(414,275, 9.5,10.0,37.0,1.2,42.0, 65,25,1.8,18, 98,168, 620,0.5),
(415,126, 4.2, 1.5,25.0,0.4,69.0,  8,16,0.4, 8, 42, 52, 280,0.3),
(416, 95, 4.8, 2.5,14.0,0.6,78.0, 25,15,0.6, 8, 48, 82, 512,0.4),
(417,285,24.5,18.2, 5.8,0.3,50.0, 95,12,0.8,21,168,220, 488,1.8),
(418,248,13.0,12.0,22.0,1.5,51.0, 42,60,2.2,22,110,218, 472,2.5);

-- ================================================================
-- 零食补充
-- ================================================================

INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(419,1704,'锅巴（原味）',    'Rice crackers, plain',       'CNF'),
(420,1601,'话梅（蜜饯）',    'Preserved plum (Huamei)',    'CNF'),
(421,1801,'米线（干）',      'Rice noodles, dried',        'CNF'),
(422,1803,'速冻包子（猪肉）','Frozen steamed pork bun',    'CNF'),
(423,1503,'豆浆（有糖）',    'Soy milk, sweetened',        'CNF'),
(424,1704,'旺旺仙贝（米饼）','Puffed rice cracker',        'CNF'),
(425,1602,'巧克力棒（牛奶）','Chocolate bar, milk',        'USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(419,386, 7.2, 5.8,75.6,1.5, 1.5, 4.5,  8,1.8,18,112,148, 512,0.7),
(420,189, 1.2, 0.3,48.8,0.5,36.0,29.0, 24,0.8,12, 16,128,6524,0.2),
(421,357, 8.2, 0.7,78.4,0.8, 0.2,10.5,  4,1.2,22, 88, 92,  22,0.8),
(422,218, 9.8, 5.2,33.0,1.2, 2.0,50.5, 28,1.3,14, 85,126, 408,0.7),
(423, 55, 2.8, 1.5, 8.5,0.6, 7.2,86.0,  8,0.3,18, 43,112,  15,0.2),
(424,390, 7.5, 4.2,78.5,0.6,16.0, 5.0, 18,1.2,15, 95,118, 326,0.5),
(425,480, 6.9,22.0,64.0,2.0,54.0, 4.0,168,2.5,52,168,280, 110,1.2);

SET FOREIGN_KEY_CHECKS = 1;
