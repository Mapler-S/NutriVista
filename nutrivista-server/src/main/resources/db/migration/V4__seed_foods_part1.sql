-- ================================================================
-- NutriVista V4: 食物种子数据 Part 1
-- 谷物/豆类/薯类/蔬菜/水果，共约 290 种食物
-- 数据来源：中国食物成分表第6版 + USDA FoodData Central
-- 所有营养值基于每 100g 可食部
-- ================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ================================================================
-- ① 谷物及制品
-- ================================================================

-- 大米及米制品 (category_id=101)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(1,101,'粳米（生，圆粒）','Japonica rice, raw','CNF'),
(2,101,'籼米（生，长粒）','Indica rice, raw','CNF'),
(3,101,'糙米（生）','Brown rice, raw','CNF'),
(4,101,'黑米（生）','Black glutinous rice, raw','CNF'),
(5,101,'糯米（生，圆粒）','Glutinous rice, raw','CNF'),
(6,101,'米饭（白米，熟）','White rice, cooked','CNF'),
(7,101,'米饭（糙米，熟）','Brown rice, cooked','CNF'),
(8,101,'白米粥','Rice porridge, thin','CNF'),
(9,101,'米饭（黑米，熟）','Black rice, cooked','CNF'),
(10,101,'糯米饭（熟）','Glutinous rice, cooked','CNF'),
(11,101,'米粉（干）','Rice vermicelli, dried','CNF'),
(12,101,'年糕','Nian Gao, rice cake','CNF'),
(13,101,'米糕','Rice cake (steamed)','CNF'),
(14,101,'汤圆（甜馅）','Tang Yuan, sweet rice ball','CNF'),
(15,101,'粽子（猪肉）','Zongzi, pork rice dumpling','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,ash,vitamin_b1,vitamin_b2,vitamin_b3,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(1,360,6.8,0.7,79.5,0.5,0.2,13.1,0.4,0.110,0.050,1.80,7,2.3,34,120,103,3,2.0,3.0),
(2,346,7.4,0.8,77.2,0.7,0.1,13.0,0.6,0.090,0.040,1.50,10,1.8,25,112,97,2,1.8,2.8),
(3,357,7.5,2.9,76.0,3.5,0.6,11.8,1.3,0.200,0.040,3.00,23,2.2,112,303,268,4,1.8,5.7),
(4,333,9.4,2.5,72.2,3.9,0.3,13.0,0.9,0.160,0.080,3.60,13,3.2,147,356,256,3,2.5,3.2),
(5,352,7.3,1.0,78.3,0.8,0.2,12.8,0.6,0.110,0.040,1.80,22,2.1,49,148,134,3,1.5,2.1),
(6,130,2.7,0.3,28.6,0.4,0.1,68.4,0.3,0.020,0.010,0.50,5,0.2,12,43,35,1,0.5,4.8),
(7,123,2.7,1.0,25.6,1.6,0.4,70.0,0.5,0.070,0.010,1.30,10,0.4,44,83,79,2,0.6,9.8),
(8,46,1.1,0.3,9.7,0.1,0.1,88.6,0.2,0.010,0.010,0.20,2,0.1,5,18,15,1,0.2,1.5),
(9,113,2.8,0.9,23.5,2.1,0.2,71.0,0.6,0.060,0.040,1.20,7,0.8,56,115,82,2,0.7,2.3),
(10,188,4.1,0.9,42.2,0.5,0.3,52.5,0.3,0.030,0.030,0.70,15,1.2,24,63,52,2,0.8,3.2),
(11,351,7.4,0.8,79.5,0.5,0.5,11.0,0.8,0.060,0.030,1.50,4,1.6,34,125,86,2,1.0,2.4),
(12,154,2.6,0.3,35.6,0.3,3.5,60.5,0.4,0.020,0.020,0.40,4,0.3,8,26,22,1,0.3,2.3),
(13,180,3.5,0.4,40.2,0.4,4.0,55.0,0.5,0.020,0.020,0.50,10,0.4,10,32,28,1,0.4,2.0),
(14,230,3.8,4.2,44.5,0.6,8.0,46.5,0.5,0.030,0.030,0.50,30,0.7,12,45,48,5,0.5,3.0),
(15,215,6.0,9.8,27.2,0.6,1.2,55.0,0.8,0.080,0.050,1.20,18,1.5,28,82,92,60,0.8,8.0);

-- 小麦及面制品 (category_id=102)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(16,102,'小麦面粉（白，低筋）','Wheat flour, all-purpose','CNF'),
(17,102,'全麦面粉','Whole wheat flour','CNF'),
(18,102,'馒头（白面）','Steamed white bread (Mantou)','CNF'),
(19,102,'全麦馒头','Whole wheat Mantou','CNF'),
(20,102,'白面包','White bread','USDA'),
(21,102,'全麦面包','Whole wheat bread','USDA'),
(22,102,'挂面（干）','Dried wheat noodles','CNF'),
(23,102,'煮面条（白面）','Boiled wheat noodles','CNF'),
(24,102,'意大利面（干）','Pasta, dried','USDA'),
(25,102,'意大利面（熟）','Pasta, cooked','USDA'),
(26,102,'饺子皮（生）','Dumpling wrappers, raw','CNF'),
(27,102,'花卷','Flower rolls (Hua Juan)','CNF'),
(28,102,'油条','Fried dough stick (You Tiao)','CNF'),
(29,102,'烧饼（芝麻）','Sesame flatbread (Shao Bing)','CNF'),
(30,102,'饼干（苏打）','Soda crackers','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,ash,vitamin_b1,vitamin_b2,vitamin_b3,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(16,366,10.3,1.0,76.1,2.7,0.5,12.0,0.6,0.100,0.060,1.80,22,3.5,22,118,107,2,0.9,5.1),
(17,340,13.2,2.5,72.0,10.7,0.4,10.5,1.8,0.360,0.150,4.70,34,3.9,138,346,405,5,2.6,35.6),
(18,223,7.0,1.1,47.0,1.3,1.0,43.5,0.6,0.050,0.030,0.90,13,1.5,15,76,80,4,0.7,6.1),
(19,210,7.5,1.6,43.0,4.2,0.8,46.0,0.9,0.120,0.060,2.00,18,2.0,32,112,118,5,0.9,10.0),
(20,265,9.0,3.2,49.4,2.7,5.3,37.0,1.6,0.210,0.130,2.90,162,3.6,23,100,115,491,0.8,16.0),
(21,247,13.0,4.2,40.7,6.0,5.6,37.0,2.1,0.340,0.140,4.00,74,2.9,76,212,248,472,1.5,25.0),
(22,350,11.0,1.0,74.0,1.6,0.4,12.5,0.9,0.090,0.040,1.50,21,3.2,22,117,108,35,1.1,7.4),
(23,138,4.5,0.7,27.5,1.2,0.3,66.0,0.4,0.030,0.020,0.50,8,1.1,8,43,40,15,0.4,10.0),
(24,371,13.0,1.5,74.7,3.2,2.7,10.4,0.8,0.150,0.060,1.70,21,3.3,53,189,215,6,1.4,63.2),
(25,158,5.8,0.9,30.9,1.8,0.6,62.0,0.4,0.020,0.010,0.40,7,1.3,18,58,45,1,0.5,26.4),
(26,276,8.9,1.2,57.3,1.5,0.3,31.0,0.8,0.070,0.040,1.10,15,2.5,15,85,70,20,0.8,5.0),
(27,211,6.5,1.3,44.0,1.2,1.0,46.5,0.6,0.040,0.030,0.80,12,1.4,14,70,75,5,0.7,5.8),
(28,386,6.4,17.6,51.0,0.9,1.5,22.0,1.1,0.050,0.040,0.80,13,1.8,15,70,60,152,0.6,7.2),
(29,390,9.5,11.0,61.0,2.0,3.0,16.0,1.8,0.120,0.060,2.00,36,3.2,32,118,112,485,0.9,8.3),
(30,421,9.5,11.5,70.6,2.5,4.2,5.0,2.0,0.130,0.100,1.30,12,3.6,16,98,97,1100,0.4,11.7);

-- 玉米及制品 (category_id=103)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(31,103,'玉米（鲜，整穗）','Sweet corn, raw','USDA'),
(32,103,'玉米（干粒）','Corn, dried grain','CNF'),
(33,103,'玉米面（黄）','Cornmeal, yellow','USDA'),
(34,103,'爆米花（无盐）','Popcorn, air-popped','USDA'),
(35,103,'玉米片（早餐谷物）','Corn flakes, cereal','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,ash,vitamin_b1,vitamin_b2,vitamin_b3,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium,vitamin_a,vitamin_c) VALUES
(31,86,3.2,1.2,18.7,2.0,6.3,76.0,0.6,0.200,0.060,1.80,2,0.5,37,89,270,15,0.5,0.5,10,7),
(32,365,9.4,4.7,74.3,7.3,0.6,10.0,1.4,0.290,0.130,2.30,26,2.4,120,256,287,4,2.2,5.0,11,0),
(33,362,8.7,3.6,76.9,7.3,0.4,9.6,1.2,0.250,0.090,1.80,6,3.4,93,241,315,1,1.7,10.1,17,0),
(34,387,12.9,4.5,77.8,15.1,0.9,3.0,1.8,0.040,0.100,1.80,7,3.2,144,358,329,8,3.1,11.0,0,0),
(35,357,7.5,0.4,84.1,3.3,8.9,3.0,1.9,0.360,0.430,5.00,1,8.1,14,95,99,660,0.3,2.8,0,13);

-- 燕麦及杂粮 (category_id=104)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(36,104,'燕麦片（生）','Oats, rolled, raw','USDA'),
(37,104,'燕麦粥（熟）','Oatmeal, cooked','USDA'),
(38,104,'小米（生）','Millet, raw','CNF'),
(39,104,'高粱米（生）','Sorghum, raw','CNF'),
(40,104,'荞麦（生）','Buckwheat, raw','USDA'),
(41,104,'藜麦（生）','Quinoa, raw','USDA'),
(42,104,'大麦（生）','Barley, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,ash,vitamin_b1,vitamin_b2,vitamin_b3,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(36,389,16.9,6.9,66.3,10.6,0.9,8.2,1.7,0.760,0.140,1.10,54,4.7,177,523,429,2,4.0,28.9),
(37,71,2.5,1.5,12.0,1.7,0.1,84.0,0.3,0.140,0.020,0.20,11,1.1,25,77,61,49,0.5,6.3),
(38,378,11.0,4.2,75.1,1.6,1.8,11.1,1.4,0.330,0.100,1.50,29,5.1,107,285,284,4,2.3,5.5),
(39,360,10.4,3.1,74.7,4.3,0.6,10.8,1.5,0.290,0.100,3.80,22,6.3,129,329,281,5,1.7,3.6),
(40,343,13.3,2.3,71.5,6.5,0.9,9.8,2.2,0.100,0.230,7.00,18,2.2,231,347,460,1,2.4,8.5),
(41,368,14.1,6.1,64.2,7.0,2.3,13.3,2.4,0.360,0.320,1.50,47,4.6,197,457,563,5,3.1,8.5),
(42,354,12.5,2.3,73.5,17.3,0.8,9.4,2.1,0.650,0.285,4.60,33,3.6,133,264,452,12,2.8,37.7);

-- ================================================================
-- ② 豆类及制品
-- ================================================================

-- 大豆及大豆制品 (category_id=201)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(43,201,'黄豆（大豆，干）','Soybeans, dry','CNF'),
(44,201,'黑豆（干）','Black soybeans, dry','CNF'),
(45,201,'豆浆（无糖）','Soy milk, unsweetened','CNF'),
(46,201,'腐竹（干）','Tofu skin (Yuba), dried','CNF'),
(47,201,'毛豆（鲜）','Edamame, fresh','USDA'),
(48,201,'纳豆','Natto, fermented soybeans','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,ash,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c) VALUES
(43,446,36.5,19.0,30.2,15.5,7.3,8.0,4.2,367,8.2,225,704,1797,2,3.3,6.6,0.830,0.200,2.10,0),
(44,381,36.0,15.9,33.6,10.2,5.9,9.9,4.2,224,7.1,243,569,1377,3,3.5,5.9,0.200,0.330,2.00,0),
(45,35,3.0,1.8,1.6,1.1,0.8,93.5,0.3,10,0.5,25,55,118,2,0.2,1.2,0.020,0.010,0.10,0),
(46,461,44.6,21.7,22.3,1.0,2.2,8.6,3.4,116,6.1,80,399,529,9,2.6,5.5,0.080,0.090,0.50,0),
(47,122,11.9,5.0,9.9,4.2,2.2,72.0,1.5,63,2.1,64,262,436,15,1.4,1.4,0.270,0.155,1.05,27),
(48,212,17.7,11.0,14.4,5.4,0.0,59.5,1.9,217,8.6,115,174,729,9,1.9,8.9,0.280,0.190,2.00,13);

-- 豆腐及豆干类 (category_id=202)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(49,202,'北豆腐（老豆腐）','Firm tofu (Northern style)','CNF'),
(50,202,'南豆腐（嫩豆腐）','Soft tofu (Southern style)','CNF'),
(51,202,'内酯豆腐（绢豆腐）','Silken tofu','CNF'),
(52,202,'豆腐干（白干）','Dried tofu, firm','CNF'),
(53,202,'豆腐干（五香）','Spiced dried tofu','CNF'),
(54,202,'豆腐泡（油豆腐）','Fried tofu puffs','CNF'),
(55,202,'豆皮（千张）','Tofu sheets','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,water,ash,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(49,76,8.1,3.7,2.0,0.4,85.5,0.8,164,1.9,63,119,106,7,1.1,1.6),
(50,57,5.5,2.5,2.8,0.3,88.9,0.5,116,1.5,45,85,95,5,0.8,1.1),
(51,49,5.0,2.2,2.0,0.2,90.2,0.4,50,0.8,27,56,72,4,0.5,0.8),
(52,140,16.2,3.6,6.5,0.8,70.5,1.2,308,4.2,111,216,160,63,2.0,2.0),
(53,195,19.0,11.5,5.1,0.7,62.0,1.6,287,3.8,99,179,129,210,1.5,1.8),
(54,242,9.8,20.5,5.7,0.6,63.2,0.7,83,2.6,62,103,113,8,1.2,1.5),
(55,210,21.0,10.0,7.0,0.5,60.0,1.5,389,5.0,131,258,178,4,2.4,2.0);

-- 其他豆类 (category_id=203)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(56,203,'绿豆（干）','Mung beans, dry','CNF'),
(57,203,'红小豆（赤豆，干）','Adzuki beans, dry','CNF'),
(58,203,'鹰嘴豆（干）','Chickpeas, dry','USDA'),
(59,203,'扁豆（干）','Lentils, dry','USDA'),
(60,203,'蚕豆（干）','Fava beans, dry','CNF'),
(61,203,'芸豆（白腰豆，干）','White kidney beans, dry','USDA'),
(62,203,'豌豆（干）','Dried peas','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,ash,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium,vitamin_b1,vitamin_b2) VALUES
(56,329,21.6,0.8,62.0,6.4,5.0,10.5,3.2,125,6.5,147,337,787,3,2.2,1.8,0.250,0.110),
(57,324,20.2,0.6,63.4,7.7,0.8,10.8,3.3,74,7.4,138,305,860,2,2.1,3.6,0.160,0.110),
(58,364,20.5,6.0,60.7,12.2,10.7,10.0,2.5,105,6.2,115,366,875,24,2.8,8.2,0.480,0.212),
(59,338,23.7,1.8,60.1,7.9,3.4,10.0,2.1,56,7.5,122,451,955,8,3.6,8.3,0.870,0.211),
(60,341,26.0,1.3,58.3,8.0,6.4,9.6,3.1,103,6.7,192,421,1062,13,2.1,1.9,0.480,0.280),
(61,333,23.4,0.9,60.3,15.2,4.0,11.0,3.6,143,10.4,190,407,1406,16,3.3,3.2,0.530,0.220),
(62,343,24.6,1.2,63.7,8.8,7.5,9.5,2.8,74,4.7,124,366,981,5,2.7,1.7,0.380,0.180);

-- ================================================================
-- ③ 薯类及淀粉
-- ================================================================

INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(63,301,'马铃薯（土豆，生）','Potato, raw','CNF'),
(64,301,'马铃薯（烤，带皮）','Potato, baked with skin','USDA'),
(65,301,'薯条（炸）','French fries','USDA'),
(66,302,'甘薯（红薯，生）','Sweet potato, raw','CNF'),
(67,302,'紫薯（生）','Purple sweet potato, raw','CNF'),
(68,303,'山药（生）','Chinese yam, raw','CNF'),
(69,303,'芋头（生）','Taro, raw','CNF'),
(70,303,'木薯（生）','Cassava, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,ash,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(63,77,2.0,0.1,17.5,2.1,0.8,79.0,1.0,0.081,0.032,1.10,19,12,0.8,23,57,425,6,0.3),
(64,93,2.5,0.1,21.2,2.2,1.0,75.0,1.2,0.083,0.038,1.50,14,8,0.6,27,70,544,11,0.4),
(65,312,3.4,14.7,41.4,3.5,0.3,39.0,1.5,0.110,0.030,2.20,5,11,0.6,23,77,579,282,0.5),
(66,86,1.6,0.1,20.1,3.0,4.2,77.3,0.9,0.100,0.060,0.60,3,30,0.6,25,47,337,55,0.3),
(67,81,1.7,0.2,18.3,3.3,3.8,78.0,0.8,0.090,0.050,0.50,11,28,0.8,20,38,290,36,0.3),
(68,63,1.7,0.1,14.2,1.1,0.5,83.7,0.6,0.050,0.030,0.30,5,16,0.3,20,34,213,18,0.3),
(69,79,1.8,0.2,18.1,2.3,0.6,78.5,1.1,0.050,0.030,0.50,5,35,0.6,33,84,591,11,0.2),
(70,160,1.4,0.3,38.1,1.8,1.7,59.5,0.7,0.087,0.048,0.85,21,16,0.3,21,27,271,14,0.3);

-- ================================================================
-- ④ 蔬菜类
-- ================================================================

-- 叶菜类 (category_id=401)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(71,401,'菠菜（生）','Spinach, raw','USDA'),
(72,401,'油菜（小白菜，生）','Bok choy, raw','CNF'),
(73,401,'大白菜（生）','Napa cabbage, raw','CNF'),
(74,401,'卷心菜（圆白菜，生）','Cabbage, green, raw','USDA'),
(75,401,'生菜（圆叶，生）','Lettuce, butterhead, raw','USDA'),
(76,401,'芹菜（生）','Celery, raw','USDA'),
(77,401,'韭菜（生）','Chinese chives, raw','CNF'),
(78,401,'空心菜（通菜，生）','Water spinach, raw','CNF'),
(79,401,'苋菜（红苋，生）','Amaranth leaves, red, raw','CNF'),
(80,401,'苦菊（生）','Chicory, raw','CNF'),
(81,401,'茼蒿（生）','Crown daisy, raw','CNF'),
(82,401,'羽衣甘蓝（生）','Kale, raw','USDA'),
(83,401,'芥蓝（生）','Chinese kale, raw','CNF'),
(84,401,'萝卜缨（生）','Radish leaves, raw','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,vitamin_k,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(71,23,2.9,0.4,3.6,2.2,0.4,91.4,469,0.078,0.189,0.72,28,483,194,99,2.71,79,49,558,79,0.53),
(72,16,1.5,0.3,2.3,1.1,1.3,95.1,238,0.040,0.080,0.60,36,46,66,108,0.7,19,37,252,65,0.25),
(73,16,1.2,0.2,3.2,0.8,1.5,95.0,48,0.040,0.060,0.50,47,55,66,43,0.3,13,29,199,57,0.25),
(74,25,1.3,0.1,5.8,2.5,3.2,92.2,5,0.061,0.040,0.23,37,76,43,40,0.47,12,26,170,18,0.18),
(75,13,1.4,0.2,2.2,1.3,0.9,95.8,166,0.057,0.062,0.36,4,102,73,35,1.24,13,33,238,5,0.20),
(76,16,0.7,0.2,3.0,1.6,1.3,95.4,22,0.021,0.057,0.32,3,30,36,40,0.20,11,24,260,80,0.13),
(77,30,2.4,0.4,4.6,1.8,0.9,91.2,235,0.060,0.120,0.90,36,68,93,48,1.6,25,38,247,14,0.43),
(78,19,2.6,0.2,3.2,2.1,1.2,93.0,378,0.080,0.100,0.80,25,57,57,100,2.5,37,38,312,95,0.30),
(79,25,2.8,0.4,4.1,1.8,1.3,92.0,352,0.080,0.150,0.60,47,0,85,187,2.9,64,58,340,42,0.72),
(80,19,1.8,0.3,3.3,1.5,1.7,93.6,147,0.060,0.080,0.50,13,102,109,35,1.0,18,32,236,38,0.28),
(81,24,1.9,0.3,4.2,1.2,0.6,93.0,252,0.060,0.120,0.60,12,46,114,108,2.3,22,36,220,42,0.35),
(82,49,4.3,0.9,8.8,3.6,2.3,84.0,500,0.110,0.130,1.00,120,817,141,150,1.5,47,92,491,38,0.56),
(83,18,1.6,0.2,3.4,1.3,1.5,94.0,72,0.040,0.090,0.60,62,72,70,128,1.4,19,34,218,78,0.35),
(84,27,2.2,0.3,5.1,1.8,1.5,91.0,387,0.080,0.150,0.70,44,0,128,128,1.2,28,40,362,88,0.40);

-- 根茎类 (category_id=402)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(85,402,'胡萝卜（生）','Carrot, raw','USDA'),
(86,402,'白萝卜（生）','White radish (Daikon), raw','CNF'),
(87,402,'洋葱（生）','Onion, raw','USDA'),
(88,402,'莲藕（生）','Lotus root, raw','CNF'),
(89,402,'竹笋（鲜）','Bamboo shoots, fresh','CNF'),
(90,402,'姜（生）','Ginger root, raw','USDA'),
(91,402,'莴苣（生）','Celtuce, raw','CNF'),
(92,402,'茭白（生）','Water bamboo shoots, raw','CNF'),
(93,402,'百合（鲜）','Fresh lily bulb, raw','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(85,41,0.9,0.2,9.6,2.8,4.7,88.3,835,0.066,0.058,0.98,6,19,33,0.30,12,35,320,69,0.24),
(86,16,0.7,0.1,3.4,1.8,2.0,94.6,3,0.020,0.030,0.20,21,22,36,0.3,9,23,173,61,0.18),
(87,40,1.1,0.1,9.3,1.7,4.2,89.1,0,0.046,0.027,0.11,7,19,23,0.21,10,29,146,4,0.17),
(88,74,2.6,0.2,16.4,2.6,1.0,80.1,3,0.110,0.050,0.40,44,13,44,1.4,22,58,293,44,0.28),
(89,27,2.6,0.3,5.2,1.8,2.2,91.2,22,0.050,0.050,0.50,4,7,9,0.5,3,59,533,4,0.55),
(90,80,1.8,0.8,15.8,2.0,1.7,79.0,0,0.025,0.034,0.75,5,11,16,0.60,43,34,415,13,0.34),
(91,15,0.9,0.1,2.9,1.3,1.2,95.3,36,0.030,0.030,0.30,4,37,23,0.3,12,29,185,7,0.24),
(92,23,1.2,0.2,5.0,1.9,2.0,92.5,5,0.030,0.060,0.40,4,0,4,0.3,6,39,209,4,0.25),
(93,91,2.6,0.1,21.8,1.4,0.9,74.0,0,0.060,0.070,0.50,5,30,11,0.5,13,61,510,6,0.52);

-- 瓜类 (category_id=403)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(94,403,'黄瓜（生）','Cucumber, raw','USDA'),
(95,403,'冬瓜（生）','Winter melon (Dong Gua), raw','CNF'),
(96,403,'南瓜（生）','Pumpkin, raw','USDA'),
(97,403,'丝瓜（生）','Loofah gourd, raw','CNF'),
(98,403,'苦瓜（生）','Bitter melon, raw','CNF'),
(99,403,'西葫芦（生）','Zucchini, raw','USDA'),
(100,403,'佛手瓜（生）','Chayote, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(94,15,0.7,0.1,3.6,0.5,1.7,95.2,5,0.027,0.033,0.10,3,16,0.28,13,24,147,2,0.20),
(95,11,0.4,0.2,2.4,0.7,1.2,96.5,13,0.010,0.010,0.20,18,19,0.2,8,12,78,2,0.07),
(96,26,1.0,0.1,6.5,0.5,2.8,92.1,426,0.050,0.110,0.60,9,21,0.8,12,44,340,1,0.32),
(97,20,1.0,0.2,4.2,0.6,1.8,94.3,47,0.040,0.040,0.40,8,14,0.4,11,32,156,3,0.21),
(98,17,1.0,0.1,3.5,2.8,1.9,94.3,24,0.070,0.040,0.40,84,9,0.4,17,36,296,5,0.36),
(99,17,1.2,0.2,3.5,1.1,2.5,94.8,10,0.045,0.094,0.45,17,16,0.37,18,38,261,8,0.29),
(100,19,0.8,0.2,4.5,1.7,1.8,93.8,9,0.025,0.029,0.47,7,17,0.34,13,23,125,2,0.74);

-- 茄果类 (category_id=404)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(101,404,'番茄（西红柿，生）','Tomato, red, raw','USDA'),
(102,404,'茄子（生）','Eggplant, raw','USDA'),
(103,404,'青椒（生）','Green bell pepper, raw','USDA'),
(104,404,'红甜椒（生）','Red bell pepper, raw','USDA'),
(105,404,'朝天椒（鲜，红）','Red chili pepper, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(101,18,0.9,0.2,3.9,1.2,2.6,94.5,42,0.037,0.019,0.59,23,15,10,0.27,11,24,237,5,0.17),
(102,25,1.0,0.2,5.7,3.0,3.5,92.3,1,0.039,0.037,0.65,2,22,9,0.24,14,24,229,2,0.16),
(103,20,0.9,0.2,4.5,1.7,2.4,93.9,18,0.057,0.028,0.48,80,10,10,0.34,10,20,175,3,0.13),
(104,31,1.0,0.3,7.4,2.1,4.2,92.2,157,0.054,0.085,0.98,128,46,7,0.43,12,26,211,4,0.25),
(105,40,1.5,0.4,8.8,1.5,5.3,88.0,48,0.090,0.090,1.24,144,23,14,1.03,23,43,322,9,0.26);

-- 豆类蔬菜 (category_id=405)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(106,405,'四季豆（扁豆，生）','Green beans, raw','USDA'),
(107,405,'豇豆（鲜，生）','Cowpea, fresh pods, raw','CNF'),
(108,405,'豌豆（鲜，生）','Green peas, raw','USDA'),
(109,405,'蚕豆（鲜，生）','Fava beans, fresh, raw','CNF'),
(110,405,'毛豆（鲜）（见豆类制品47）','Edamame (see legumes)','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(106,31,1.8,0.1,6.9,2.7,3.3,90.3,35,0.082,0.104,0.73,12,33,37,1.03,25,38,209,6,0.24),
(107,34,2.0,0.2,7.1,2.7,2.5,90.2,20,0.050,0.060,0.80,18,53,42,1.4,33,49,145,3,0.38),
(108,81,5.4,0.4,14.5,5.1,5.7,78.9,38,0.266,0.132,2.09,40,65,25,1.47,33,108,244,5,1.24),
(109,80,7.0,0.6,14.4,4.2,2.9,76.5,32,0.110,0.130,1.20,36,148,40,1.9,33,125,270,2,0.65),
(110,122,11.9,5.0,9.9,4.2,2.2,72.0,9,0.270,0.155,1.05,27,311,63,2.11,64,262,436,15,1.37);

-- 葱蒜类 (category_id=406)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(111,406,'大蒜（生）','Garlic, raw','USDA'),
(112,406,'大葱（生）','Welsh onion, raw','CNF'),
(113,406,'香葱（小葱，生）','Scallion, raw','USDA'),
(114,406,'洋葱（生）（见根茎87）','Onion, raw (see roots)','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(111,149,6.4,0.5,33.1,2.1,1.0,58.6,0.200,0.110,0.70,31,181,1.7,25,153,401,17,1.2),
(112,30,1.6,0.3,6.5,1.8,2.8,91.8,0.030,0.040,0.40,17,45,0.7,19,38,144,5,0.2),
(113,32,1.8,0.2,7.3,2.6,2.3,89.8,0.060,0.090,0.52,19,72,1.48,20,37,276,16,0.39),
(114,40,1.1,0.1,9.3,1.7,4.2,89.1,0.046,0.027,0.11,7,23,0.21,10,29,146,4,0.17);

-- 花菜类 (category_id=407)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(115,407,'西兰花（生）','Broccoli, raw','USDA'),
(116,407,'花椰菜（白色花菜，生）','Cauliflower, raw','USDA'),
(117,407,'紫甘蓝（生）','Red/Purple cabbage, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,vitamin_k,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(115,34,2.8,0.4,6.6,2.6,1.7,89.3,31,0.071,0.117,0.64,89,63,102,47,0.73,21,66,316,33,0.41),
(116,25,1.9,0.3,5.0,2.0,1.9,92.1,0,0.050,0.060,0.51,46,57,16,22,0.42,15,44,299,30,0.27),
(117,31,1.4,0.2,7.4,2.1,3.8,90.4,56,0.069,0.068,0.42,57,18,38,45,0.80,16,30,243,27,0.22);

-- 水生蔬菜 (category_id=408)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(118,408,'藕（莲藕，生）（见根茎88）','Lotus root (see roots)','CNF'),
(119,408,'荸荠（马蹄，生）','Water chestnut, raw','CNF'),
(120,408,'茭白（生）（见根茎92）','Water bamboo (see roots)','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(119,60,1.5,0.2,13.9,1.1,5.5,83.0,0.020,0.020,1.00,4,4,0.5,12,44,306,15,0.34),
(120,23,1.2,0.2,5.0,1.9,2.0,92.5,0.030,0.060,0.40,4,4,0.3,6,39,209,4,0.25);

-- 菌类蔬菜（鲜）(category_id=409)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(121,409,'香菇（鲜）','Shiitake mushroom, fresh','CNF'),
(122,409,'平菇（鲜）','Oyster mushroom, fresh','CNF'),
(123,409,'金针菇（鲜）','Enoki mushroom, fresh','CNF'),
(124,409,'杏鲍菇（鲜）','King oyster mushroom, fresh','CNF'),
(125,409,'口蘑（鲜）','Button mushroom (Chinese type), fresh','CNF'),
(126,409,'茶树菇（鲜）','Tea tree mushroom, fresh','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_d,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc,selenium) VALUES
(121,34,2.2,0.5,6.8,2.5,2.5,88.0,0.100,0.190,2.50,0.8,2,0.3,11,53,304,2,0.66,2.6),
(122,20,1.9,0.3,3.3,2.3,1.8,94.0,0.070,0.160,3.10,0.4,5,1.0,13,71,337,6,0.40,2.9),
(123,26,2.0,0.4,4.3,2.7,1.5,93.2,0.150,0.190,6.40,0.3,2,1.1,18,97,268,4,0.39,2.3),
(124,31,1.3,0.1,6.1,2.2,1.3,92.0,0.020,0.070,3.20,0.5,3,0.5,8,56,240,6,0.51,1.6),
(125,26,2.7,0.4,4.6,1.4,1.2,91.8,0.080,0.350,3.60,0.2,6,1.1,9,86,470,8,0.63,3.5),
(126,24,2.0,0.3,5.0,3.5,1.8,91.5,0.070,0.280,6.00,0.4,3,1.0,13,67,310,5,0.48,3.2);

-- ================================================================
-- ⑤ 水果类
-- ================================================================

-- 仁果类 (category_id=501)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(127,501,'苹果（生）','Apple, raw, with skin','USDA'),
(128,501,'梨（生）','Pear, raw','USDA'),
(129,501,'山楂（生）','Hawthorn berry, raw','CNF'),
(130,501,'枇杷（生）','Loquat, raw','USDA'),
(131,501,'海棠果（生）','Crabapple, raw','CNF');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(127,52,0.3,0.2,13.8,2.4,10.4,85.6,3,0.017,0.026,0.09,5,3,6,0.12,5,11,107,1,0.04),
(128,58,0.4,0.1,15.5,3.1,9.8,83.7,1,0.012,0.026,0.16,4,7,9,0.18,7,12,119,1,0.10),
(129,102,0.5,0.6,25.1,3.1,12.2,73.0,17,0.020,0.020,0.40,53,0,52,0.9,18,24,299,5,0.28),
(130,47,0.5,0.2,12.1,1.4,7.1,86.7,76,0.019,0.024,0.18,1,14,16,0.28,13,27,266,1,0.05),
(131,74,0.3,0.2,19.8,1.8,13.5,79.0,8,0.010,0.030,0.20,20,0,12,0.5,6,16,135,3,0.15);

-- 核果类 (category_id=502)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(132,502,'桃（生）','Peach, raw','USDA'),
(133,502,'李子（生）','Plum, raw','USDA'),
(134,502,'樱桃（生）','Cherry, sweet, raw','USDA'),
(135,502,'杏（生）','Apricot, raw','USDA'),
(136,502,'枣（鲜）','Jujube (Chinese date), fresh','CNF'),
(137,502,'芒果（生）','Mango, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(132,39,0.9,0.3,9.5,1.5,8.4,88.9,16,0.024,0.031,0.81,7,4,6,0.25,9,20,190,0,0.17),
(133,46,0.7,0.3,11.4,0.6,9.9,87.2,17,0.028,0.026,0.42,9,5,6,0.17,7,16,157,0,0.10),
(134,50,1.0,0.3,12.2,1.6,8.5,85.8,3,0.027,0.033,0.15,7,4,13,0.36,11,21,222,0,0.07),
(135,48,1.4,0.4,11.1,2.0,9.2,86.4,96,0.030,0.040,0.60,10,9,13,0.39,10,23,259,1,0.20),
(136,122,1.2,0.3,30.5,1.9,28.8,67.0,2,0.060,0.090,0.90,243,0,22,1.2,25,23,375,1,0.29),
(137,60,0.8,0.4,15.0,1.6,13.7,83.5,54,0.028,0.038,0.67,36,43,11,0.16,10,14,168,1,0.09);

-- 浆果类 (category_id=503)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(138,503,'草莓（生）','Strawberry, raw','USDA'),
(139,503,'蓝莓（生）','Blueberry, raw','USDA'),
(140,503,'葡萄（生，红/绿）','Grape, raw','USDA'),
(141,503,'猕猴桃（生）','Kiwifruit, raw','USDA'),
(142,503,'石榴（生）','Pomegranate, raw','USDA'),
(143,503,'桑葚（生）','Mulberry, raw','USDA'),
(144,503,'无花果（生）','Fig, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(138,32,0.7,0.3,7.7,2.0,4.9,90.9,1,0.024,0.022,0.39,59,24,16,0.41,13,24,153,1,0.14),
(139,57,0.7,0.3,14.5,2.4,10.0,84.2,3,0.037,0.041,0.42,10,6,6,0.28,6,12,77,1,0.16),
(140,69,0.7,0.2,18.1,0.9,15.5,81.0,3,0.069,0.070,0.19,11,2,10,0.36,7,20,191,2,0.07),
(141,61,1.1,0.5,14.7,3.0,9.0,83.1,4,0.027,0.025,0.34,93,25,34,0.31,17,34,312,3,0.14),
(142,68,1.7,1.2,18.7,4.0,13.7,78.0,0,0.067,0.053,0.29,10,38,10,0.30,12,36,236,3,0.35),
(143,43,1.4,0.4,9.8,1.7,8.1,87.7,1,0.029,0.101,0.62,36,6,39,1.85,18,38,194,10,0.12),
(144,74,0.8,0.3,19.2,2.9,16.3,79.1,7,0.060,0.050,0.40,2,6,35,0.37,17,14,232,1,0.15);

-- 柑橘类 (category_id=504)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(145,504,'橙子（生）','Orange, navel, raw','USDA'),
(146,504,'柠檬（生）','Lemon, raw','USDA'),
(147,504,'柚子（生）','Pomelo, raw','CNF'),
(148,504,'蜜橘（生）','Mandarin orange, raw','USDA'),
(149,504,'葡萄柚（生）','Grapefruit, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(145,47,0.9,0.1,11.8,2.4,9.4,86.8,11,0.087,0.040,0.28,59,30,40,0.10,10,17,181,0,0.07),
(146,29,1.1,0.3,9.3,2.8,2.5,89.1,1,0.040,0.020,0.10,53,11,26,0.60,8,16,138,2,0.06),
(147,39,0.7,0.1,9.6,1.0,8.0,88.0,3,0.040,0.030,0.30,23,0,24,0.3,14,20,216,1,0.17),
(148,53,0.8,0.3,13.3,2.0,10.6,85.2,12,0.058,0.036,0.38,27,16,37,0.15,12,20,166,2,0.07),
(149,42,0.8,0.1,10.7,1.6,6.9,87.3,46,0.043,0.031,0.25,34,13,22,0.08,9,18,135,0,0.07);

-- 热带水果 (category_id=505)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(150,505,'香蕉（生）','Banana, raw','USDA'),
(151,505,'菠萝（生）','Pineapple, raw','USDA'),
(152,505,'木瓜（生）','Papaya, raw','USDA'),
(153,505,'荔枝（生）','Lychee, raw','USDA'),
(154,505,'龙眼（生）','Longan, raw','CNF'),
(155,505,'榴莲（生）','Durian, raw','USDA'),
(156,505,'火龙果（生）','Dragon fruit (Pitaya), raw','CNF'),
(157,505,'芭蕉（生）','Plantain, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(150,89,1.1,0.3,22.8,2.6,12.2,74.9,3,0.031,0.073,0.67,9,20,5,0.26,27,22,358,1,0.15),
(151,50,0.5,0.1,13.1,1.4,9.9,86.0,3,0.079,0.032,0.49,48,18,13,0.29,12,8,109,1,0.12),
(152,43,0.5,0.3,10.8,1.7,7.8,88.1,47,0.023,0.027,0.36,62,38,24,0.10,10,5,182,8,0.08),
(153,66,0.8,0.4,16.5,1.3,15.2,81.8,0,0.011,0.065,1.06,72,14,5,0.31,10,31,171,1,0.07),
(154,60,1.3,0.1,15.1,1.1,14.3,83.0,0,0.040,0.140,1.30,84,0,6,0.3,10,21,248,3,0.05),
(155,147,1.5,5.3,27.1,3.8,5.6,64.9,2,0.374,0.200,1.07,20,36,4,0.43,30,39,436,2,0.28),
(156,60,1.2,0.4,13.2,2.9,8.0,84.0,1,0.043,0.045,0.43,9,7,18,0.74,40,30,436,39,0.37),
(157,122,1.3,0.4,31.9,2.3,14.8,65.3,56,0.052,0.054,0.69,18,22,3,0.60,37,34,499,4,0.14);

-- 瓜类水果 (category_id=506)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(158,506,'西瓜（生）','Watermelon, raw','USDA'),
(159,506,'哈密瓜（生）','Hami melon (Honeydew type), raw','CNF'),
(160,506,'香瓜（甜瓜，生）','Cantaloupe, raw','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,folate,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(158,30,0.6,0.2,7.6,0.4,6.2,91.5,28,0.033,0.021,0.18,8,3,7,0.24,10,11,112,1,0.10),
(159,34,0.5,0.2,8.4,0.9,7.7,90.1,20,0.050,0.020,0.50,36,0,4,0.2,8,11,190,18,0.10),
(160,34,0.8,0.2,8.2,0.9,7.9,90.2,169,0.044,0.024,0.73,36,21,9,0.21,12,15,267,16,0.18);

-- 干果类 (category_id=507)
INSERT INTO food (id,category_id,name_zh,name_en,data_source) VALUES
(161,507,'葡萄干','Raisins, seedless','USDA'),
(162,507,'红枣（干）','Jujube, dried','CNF'),
(163,507,'枸杞（干）','Wolfberry (Goji berry), dried','CNF'),
(164,507,'桂圆（干）','Longan, dried','CNF'),
(165,507,'无花果（干）','Fig, dried','USDA');
INSERT INTO food_nutrition(food_id,energy_kcal,protein,fat,carbohydrate,fiber,sugar,water,vitamin_a,vitamin_b1,vitamin_b2,vitamin_b3,vitamin_c,calcium,iron,magnesium,phosphorus,potassium,sodium,zinc) VALUES
(161,299,3.1,0.5,79.2,3.7,59.2,15.5,0,0.106,0.125,0.77,3,50,1.88,32,101,749,11,0.22),
(162,264,3.2,0.5,67.8,6.2,38.7,22.0,2,0.050,0.090,0.90,14,64,2.3,39,56,524,6,0.65),
(163,258,13.9,1.5,64.1,16.9,0.0,10.0,1625,0.150,0.430,4.00,48,190,9.0,96,209,454,36,1.48),
(164,286,4.9,0.4,74.9,0.9,68.6,18.8,0,0.040,0.180,1.10,0,38,4.4,60,89,658,3,0.78),
(165,249,3.3,0.9,63.9,9.8,47.9,30.1,0,0.085,0.082,0.62,1,162,2.03,68,67,680,10,0.55);

SET FOREIGN_KEY_CHECKS = 1;
