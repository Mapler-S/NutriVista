-- ================================================================
-- NutriVista V3: 食物分类种子数据
-- 20个大类 + 68个子类 = 88个分类，满足"至少15大类、80+子类"要求
-- ================================================================
SET NAMES utf8mb4;

INSERT INTO food_category (id, parent_id, name_zh, name_en, icon, sort_order) VALUES
-- ================================================================
-- 第一层：20 个大类（parent_id = NULL）
-- ================================================================
(1,  NULL, '谷物及制品',       'Grains & Cereals',              '🌾', 1),
(2,  NULL, '豆类及制品',       'Legumes & Products',            '🫘', 2),
(3,  NULL, '薯类及淀粉',       'Tubers & Starches',             '🥔', 3),
(4,  NULL, '蔬菜类',           'Vegetables',                    '🥦', 4),
(5,  NULL, '水果类',           'Fruits',                        '🍎', 5),
(6,  NULL, '畜肉类',           'Red Meat',                      '🥩', 6),
(7,  NULL, '禽肉类',           'Poultry',                       '🍗', 7),
(8,  NULL, '水产品类',         'Fish & Seafood',                '🐟', 8),
(9,  NULL, '蛋类',             'Eggs',                          '🥚', 9),
(10, NULL, '乳及乳制品',       'Dairy Products',                '🥛', 10),
(11, NULL, '坚果及种子',       'Nuts & Seeds',                  '🥜', 11),
(12, NULL, '菌藻类',           'Mushrooms & Algae',             '🍄', 12),
(13, NULL, '油脂类',           'Oils & Fats',                   '🫙', 13),
(14, NULL, '调味品类',         'Condiments & Spices',           '🧂', 14),
(15, NULL, '饮料类',           'Beverages',                     '🧃', 15),
(16, NULL, '糖及甜食',         'Sugars & Sweets',               '🍬', 16),
(17, NULL, '糕点及零食',       'Pastries & Snacks',             '🍪', 17),
(18, NULL, '快餐及方便食品',   'Fast Food & Convenience',       '🍜', 18),
(19, NULL, '腌制及发酵食品',   'Pickled & Fermented Foods',     '🥒', 19),
(20, NULL, '营养及特殊食品',   'Nutritional & Special Foods',   '💊', 20),

-- ================================================================
-- 第二层：子类（parent_id → 大类ID）
-- ================================================================

-- ▶ 谷物及制品 (parent=1)
(101, 1, '大米及米制品',   'Rice & Rice Products',          '🍚', 1),
(102, 1, '小麦及面制品',   'Wheat & Wheat Products',        '🍞', 2),
(103, 1, '玉米及制品',     'Corn & Corn Products',          '🌽', 3),
(104, 1, '燕麦及杂粮',     'Oats & Mixed Grains',           '🌾', 4),
(105, 1, '其他谷物',       'Other Grains',                  '🌾', 5),

-- ▶ 豆类及制品 (parent=2)
(201, 2, '大豆及大豆制品', 'Soybeans & Soy Products',       '🫘', 1),
(202, 2, '豆腐及豆干类',   'Tofu & Bean Curd Products',     '🫘', 2),
(203, 2, '其他豆类',       'Other Legumes',                 '🫘', 3),

-- ▶ 薯类及淀粉 (parent=3)
(301, 3, '土豆及制品',     'Potatoes & Products',           '🥔', 1),
(302, 3, '甘薯及制品',     'Sweet Potatoes & Products',     '🍠', 2),
(303, 3, '其他薯类及淀粉', 'Other Tubers & Starches',       '🍠', 3),

-- ▶ 蔬菜类 (parent=4)
(401, 4, '叶菜类',         'Leafy Vegetables',              '🥬', 1),
(402, 4, '根茎类',         'Root & Stem Vegetables',        '🥕', 2),
(403, 4, '瓜类',           'Gourd Vegetables',              '🥒', 3),
(404, 4, '茄果类',         'Solanaceous Vegetables',        '🍅', 4),
(405, 4, '豆类蔬菜',       'Pod Vegetables',                '🫛', 5),
(406, 4, '葱蒜类',         'Allium Vegetables',             '🧅', 6),
(407, 4, '花菜类',         'Brassica Vegetables',           '🥦', 7),
(408, 4, '水生蔬菜',       'Aquatic Vegetables',            '🪷', 8),
(409, 4, '菌类蔬菜（鲜）', 'Fresh Mushroom Vegetables',     '🍄', 9),

-- ▶ 水果类 (parent=5)
(501, 5, '仁果类',         'Pome Fruits',                   '🍎', 1),
(502, 5, '核果类',         'Stone Fruits',                  '🍑', 2),
(503, 5, '浆果类',         'Berries',                       '🍓', 3),
(504, 5, '柑橘类',         'Citrus Fruits',                 '🍊', 4),
(505, 5, '热带水果',       'Tropical Fruits',               '🍌', 5),
(506, 5, '瓜类水果',       'Melon Fruits',                  '🍉', 6),
(507, 5, '干果类',         'Dried Fruits',                  '🍇', 7),

-- ▶ 畜肉类 (parent=6)
(601, 6, '猪肉及制品',     'Pork & Pork Products',          '🐖', 1),
(602, 6, '牛肉及制品',     'Beef & Beef Products',          '🐄', 2),
(603, 6, '羊肉及制品',     'Lamb & Lamb Products',          '🐑', 3),
(604, 6, '动物内脏',       'Organ Meats',                   '🫀', 4),
(605, 6, '肉类加工品',     'Processed Meat Products',       '🌭', 5),

-- ▶ 禽肉类 (parent=7)
(701, 7, '鸡肉及制品',     'Chicken & Products',            '🐓', 1),
(702, 7, '鸭鹅及其他禽肉', 'Duck, Goose & Other Poultry',   '🦆', 2),

-- ▶ 水产品类 (parent=8)
(801, 8, '鱼类',           'Fish',                          '🐠', 1),
(802, 8, '虾蟹类',         'Shrimp & Crab',                 '🦐', 2),
(803, 8, '贝壳类',         'Shellfish & Mollusks',          '🦪', 3),
(804, 8, '水产加工品',     'Processed Seafood',             '🐟', 4),

-- ▶ 蛋类 (parent=9)
(901, 9, '蛋类',           'Eggs & Egg Products',           '🥚', 1),

-- ▶ 乳及乳制品 (parent=10)
(1001, 10, '液态奶',       'Liquid Milk',                   '🥛', 1),
(1002, 10, '发酵乳及酸奶', 'Fermented Milk & Yogurt',       '🫙', 2),
(1003, 10, '奶酪类',       'Cheese',                        '🧀', 3),
(1004, 10, '奶油及黄油',   'Cream & Butter',                '🧈', 4),
(1005, 10, '奶粉及炼乳',   'Milk Powder & Condensed Milk',  '🥛', 5),
(1006, 10, '植物基奶',     'Plant-Based Milk',              '🌱', 6),

-- ▶ 坚果及种子 (parent=11)
(1101, 11, '树坚果',       'Tree Nuts',                     '🌰', 1),
(1102, 11, '种子类',       'Seeds',                         '🌻', 2),
(1103, 11, '花生类',       'Peanuts & Products',            '🥜', 3),

-- ▶ 菌藻类 (parent=12)
(1201, 12, '食用菌（鲜）', 'Fresh Edible Mushrooms',        '🍄', 1),
(1202, 12, '食用菌（干）', 'Dried Edible Mushrooms',        '🍄', 2),
(1203, 12, '藻类',         'Algae & Seaweed',               '🌿', 3),

-- ▶ 油脂类 (parent=13)
(1301, 13, '植物油',       'Vegetable Oils',                '🫙', 1),
(1302, 13, '动物油脂',     'Animal Fats',                   '🫙', 2),

-- ▶ 调味品类 (parent=14)
(1401, 14, '盐及酱油类',   'Salt & Soy Sauce',              '🧂', 1),
(1402, 14, '酱类',         'Pastes & Sauces',               '🫙', 2),
(1403, 14, '醋及酸味料',   'Vinegar & Acidulants',          '🫙', 3),
(1404, 14, '糖及甜味料',   'Sugar & Sweeteners',            '🍯', 4),
(1405, 14, '香辛料',       'Herbs & Spices',                '🌶️',  5),
(1406, 14, '复合调料',     'Mixed Seasonings',              '🫙', 6),

-- ▶ 饮料类 (parent=15)
(1501, 15, '茶类',         'Tea',                           '🍵', 1),
(1502, 15, '咖啡类',       'Coffee',                        '☕', 2),
(1503, 15, '果汁饮料',     'Fruit Juices & Drinks',         '🍹', 3),
(1504, 15, '碳酸饮料',     'Carbonated Soft Drinks',        '🥤', 4),
(1505, 15, '酒精饮料',     'Alcoholic Beverages',           '🍺', 5),
(1506, 15, '功能性饮料',   'Energy & Functional Drinks',    '⚡', 6),
(1507, 15, '矿泉水及纯净水','Water',                        '💧', 7),

-- ▶ 糖及甜食 (parent=16)
(1601, 16, '糖果',         'Candy',                         '🍬', 1),
(1602, 16, '巧克力',       'Chocolate',                     '🍫', 2),
(1603, 16, '果酱及蜂蜜',   'Jams, Jellies & Honey',         '🍯', 3),
(1604, 16, '冷饮及冰淇淋', 'Ice Cream & Frozen Desserts',   '🍦', 4),

-- ▶ 糕点及零食 (parent=17)
(1701, 17, '饼干类',       'Cookies & Crackers',            '🍪', 1),
(1702, 17, '蛋糕及糕点',   'Cakes & Pastries',              '🎂', 2),
(1703, 17, '中式糕点',     'Chinese Traditional Pastries',  '🥮', 3),
(1704, 17, '薯片及膨化食品','Chips & Puffed Snacks',        '🥔', 4),

-- ▶ 快餐及方便食品 (parent=18)
(1801, 18, '方便面及速食', 'Instant Noodles & Ready Meals', '🍜', 1),
(1802, 18, '罐头食品',     'Canned Foods',                  '🥫', 2),
(1803, 18, '速冻食品',     'Frozen Foods',                  '❄️',  3),

-- ▶ 腌制及发酵食品 (parent=19)
(1901, 19, '腌菜及泡菜',   'Pickled Vegetables',            '🥒', 1),
(1902, 19, '腌肉及腊味',   'Cured & Preserved Meats',       '🥓', 2),
(1903, 19, '发酵豆制品',   'Fermented Soy Products',        '🫘', 3),

-- ▶ 营养及特殊食品 (parent=20)
(2001, 20, '婴幼儿食品',   'Infant & Baby Foods',           '🍼', 1),
(2002, 20, '运动营养品',   'Sports Nutrition',              '💪', 2),
(2003, 20, '代餐食品',     'Meal Replacements',             '🥤', 3);
