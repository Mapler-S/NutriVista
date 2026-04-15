package com.nutrivista.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrivista.common.constant.MealType;
import com.nutrivista.common.exception.BusinessException;
import com.nutrivista.common.result.PageResult;
import com.nutrivista.common.result.Result;
import com.nutrivista.dto.meal.MealItemDto;
import com.nutrivista.dto.meal.MealRecordDto;
import com.nutrivista.dto.recipe.*;
import com.nutrivista.entity.Food;
import com.nutrivista.entity.MealItem;
import com.nutrivista.entity.MealRecord;
import com.nutrivista.entity.Recipe;
import com.nutrivista.entity.RecipeUsageLog;
import com.nutrivista.repository.FoodRepository;
import com.nutrivista.repository.MealItemRepository;
import com.nutrivista.repository.MealRecordRepository;
import com.nutrivista.repository.RecipeRepository;
import com.nutrivista.repository.RecipeUsageLogRepository;
import com.nutrivista.service.RecipeRecommendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeRecommendService   recommendService;
    private final RecipeRepository         recipeRepository;
    private final FoodRepository           foodRepository;
    private final MealRecordRepository     mealRecordRepository;
    private final MealItemRepository       mealItemRepository;
    private final RecipeUsageLogRepository recipeUsageLogRepository;
    private final ObjectMapper             objectMapper;

    // ── 推荐（RAG） ──────────────────────────────────────────────────────────────

    /**
     * POST /api/recipes/recommend
     * 根据用户自然语言描述 + 结构化条件，进行 RAG 菜谱推荐。
     */
    @PostMapping("/recommend")
    public Result<RecipeRecommendResponse> recommend(
            @RequestBody @Valid RecipeRecommendRequest request) {
        RecipeRecommendResponse response = recommendService.recommend(request);
        logRecommendEvents(response, 1L);
        return Result.success(response);
    }

    private void logRecommendEvents(RecipeRecommendResponse response, long userId) {
        if (response.getRecipes() == null || response.getRecipes().isEmpty()) return;
        LocalDate today = java.time.LocalDate.now();
        List<RecipeUsageLog> logs = response.getRecipes().stream().map(r -> {
            RecipeUsageLog log = new RecipeUsageLog();
            log.setUserId(userId);
            log.setRecipeId(r.getRecipeId());
            log.setRecipeName(r.getName() != null ? r.getName() : "");
            log.setCuisine(r.getCuisine());
            log.setEventType(RecipeUsageLog.EventType.RECOMMEND);
            log.setMealDate(today);
            return log;
        }).toList();
        recipeUsageLogRepository.saveAll(logs);
    }

    // ── 搜索 / 浏览 ──────────────────────────────────────────────────────────────

    /**
     * GET /api/recipes/search?keyword=&cuisine=&category=&page=1&pageSize=20
     */
    @GetMapping("/search")
    public Result<PageResult<RecipeListVO>> searchRecipes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<Recipe> paged = recipeRepository.search(
                blankToNull(keyword), blankToNull(cuisine), blankToNull(category),
                PageRequest.of(page - 1, pageSize, Sort.by("name")));

        List<RecipeListVO> vos = paged.getContent().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(vos, paged.getTotalElements(), page, pageSize));
    }

    /**
     * GET /api/recipes/cuisines — 返回各菜系及菜谱数量。
     */
    @GetMapping("/cuisines")
    public Result<List<CuisineVO>> getCuisines() {
        List<CuisineVO> cuisines = recipeRepository.countByCuisine().stream()
                .filter(row -> row[0] != null)
                .map(row -> new CuisineVO((String) row[0], ((Number) row[1]).intValue()))
                .collect(Collectors.toList());
        return Result.success(cuisines);
    }

    // ── 详情 ─────────────────────────────────────────────────────────────────────

    /**
     * GET /api/recipes/{id}
     */
    @GetMapping("/{id}")
    public Result<RecipeDetailVO> getRecipeDetail(@PathVariable String id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("菜谱"));
        return Result.success(toDetailVO(recipe));
    }

    // ── 添加到饮食记录 ───────────────────────────────────────────────────────────

    /**
     * POST /api/recipes/{id}/add-to-meal
     * 将菜谱的食材按份数换算后批量添加到指定餐次的饮食记录中。
     */
    @PostMapping("/{id}/add-to-meal")
    public Result<MealRecordDto> addRecipeToMeal(
            @PathVariable String id,
            @RequestBody @Valid AddRecipeToMealRequest request) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("菜谱"));

        // 解析食材列表
        List<Map<String, Object>> ingredients = parseJsonList(recipe.getIngredients());
        if (ingredients.isEmpty()) {
            return Result.badRequest("该菜谱暂无食材数据");
        }

        // 比例换算：用户 servings / 菜谱原始 servings
        int recipeServings = recipe.getServings() != null && recipe.getServings() > 0
                ? recipe.getServings() : 1;
        double ratio = (double) request.getServings() / recipeServings;

        // 获取或创建 MealRecord（硬编码 userId=1，与现有系统一致）
        final long userId = 1L;
        MealRecord mealRecord = mealRecordRepository
                .findByUserIdAndMealDateAndMealType(userId, request.getMealDate(), request.getMealType())
                .orElseGet(() -> {
                    MealRecord m = new MealRecord();
                    m.setUserId(userId);
                    m.setMealDate(request.getMealDate());
                    m.setMealType(request.getMealType());
                    return mealRecordRepository.save(m);
                });

        int sortBase = (int) mealItemRepository.countByMealRecordId(mealRecord.getId());
        int added = 0;

        for (Map<String, Object> ing : ingredients) {
            String name   = strOf(ing, "name");
            String unit   = strOf(ing, "unit");
            String amount = strOf(ing, "amount");

            // 只处理克/毫升单位（其他单位如"条"、"个"跳过）
            if (!isWeightUnit(unit)) {
                log.debug("跳过非重量食材: {} ({} {})", name, amount, unit);
                continue;
            }

            double rawAmount;
            try { rawAmount = Double.parseDouble(amount); }
            catch (NumberFormatException e) { continue; }

            BigDecimal weightGram = BigDecimal.valueOf(rawAmount * ratio)
                    .setScale(1, RoundingMode.HALF_UP);
            if (weightGram.compareTo(BigDecimal.valueOf(0.1)) < 0) continue;

            // 食物匹配：多级渐进策略
            Optional<Food> foodOpt = findBestFoodMatch(name);

            if (foodOpt.isEmpty()) {
                log.debug("未找到食物: {}", name);
                continue;
            }

            Food food = foodRepository.findByIdWithNutrition(foodOpt.get().getId())
                    .orElse(foodOpt.get());

            MealItem item = new MealItem();
            item.setMealRecord(mealRecord);
            item.setFood(food);
            item.setWeightGram(weightGram);
            item.setSortOrder(sortBase + added);
            mealItemRepository.save(item);
            added++;
        }

        if (added == 0) {
            return Result.badRequest("菜谱中未找到可匹配的食材（可能均为非克重单位）");
        }

        // 记录采用日志
        RecipeUsageLog adoptLog = new RecipeUsageLog();
        adoptLog.setUserId(userId);
        adoptLog.setRecipeId(id);
        adoptLog.setRecipeName(recipe.getName() != null ? recipe.getName() : "");
        adoptLog.setCuisine(recipe.getCuisine());
        adoptLog.setEventType(RecipeUsageLog.EventType.ADOPTED);
        adoptLog.setMealDate(request.getMealDate());
        recipeUsageLogRepository.save(adoptLog);

        // 重新查询（带 items eager loading）以构建完整 DTO
        MealRecord loaded = mealRecordRepository
                .findByUserIdAndMealDateEager(userId, request.getMealDate())
                .stream()
                .filter(mr -> mr.getId().equals(mealRecord.getId()))
                .findFirst()
                .orElse(mealRecord);

        return Result.success("成功添加 " + added + " 种食材", buildMealRecordDto(loaded));
    }

    // ── VO 转换 ──────────────────────────────────────────────────────────────────

    private RecipeListVO toListVO(Recipe r) {
        return RecipeListVO.builder()
                .id(r.getId())
                .name(r.getName())
                .nameEn(r.getNameEn())
                .cuisine(r.getCuisine())
                .category(r.getCategory())
                .calories(r.getCaloriesPerServing())
                .prepTime(r.getPrepTimeMinutes())
                .cookTime(r.getCookTimeMinutes())
                .difficulty(r.getDifficulty())
                .tags(parseStringList(r.getTags()))
                .build();
    }

    private RecipeDetailVO toDetailVO(Recipe r) {
        return RecipeDetailVO.builder()
                .id(r.getId())
                .name(r.getName())
                .nameEn(r.getNameEn())
                .cuisine(r.getCuisine())
                .category(r.getCategory())
                .subCategory(r.getSubCategory())
                .calories(r.getCaloriesPerServing())
                .prepTime(r.getPrepTimeMinutes())
                .cookTime(r.getCookTimeMinutes())
                .servings(r.getServings())
                .difficulty(r.getDifficulty())
                .description(r.getDescription())
                .tips(r.getTips())
                .tags(parseStringList(r.getTags()))
                .ingredients(parseIngredients(r.getIngredients()))
                .steps(parseStringList(r.getSteps()))
                .nutrition(parseNutrition(r.getNutritionSummary()))
                .suitableFor(parseStringList(r.getSuitableFor()))
                .build();
    }

    // ── MealRecord DTO（复刻 MealServiceImpl 的逻辑） ────────────────────────────

    private MealRecordDto buildMealRecordDto(MealRecord record) {
        List<MealItemDto> itemDtos = record.getItems().stream()
                .map(this::buildItemDto).collect(Collectors.toList());

        BigDecimal energy = BigDecimal.ZERO, protein = BigDecimal.ZERO,
                   fat = BigDecimal.ZERO,    carb    = BigDecimal.ZERO,
                   fiber = BigDecimal.ZERO;
        for (MealItemDto d : itemDtos) {
            energy  = energy.add(nvl(d.getEnergyKcal()));
            protein = protein.add(nvl(d.getProtein()));
            fat     = fat.add(nvl(d.getFat()));
            carb    = carb.add(nvl(d.getCarbohydrate()));
            fiber   = fiber.add(nvl(d.getFiber()));
        }
        MealRecordDto dto = new MealRecordDto();
        dto.setId(record.getId());
        dto.setMealType(record.getMealType().name());
        dto.setNotes(record.getNotes());
        dto.setItems(itemDtos);
        dto.setTotalEnergyKcal(energy.setScale(1, RoundingMode.HALF_UP));
        dto.setTotalProtein(protein.setScale(1, RoundingMode.HALF_UP));
        dto.setTotalFat(fat.setScale(1, RoundingMode.HALF_UP));
        dto.setTotalCarbohydrate(carb.setScale(1, RoundingMode.HALF_UP));
        dto.setTotalFiber(fiber.setScale(1, RoundingMode.HALF_UP));
        return dto;
    }

    private MealItemDto buildItemDto(MealItem item) {
        BigDecimal w = item.getWeightGram();
        var n = item.getFood().getNutrition();
        MealItemDto dto = new MealItemDto();
        dto.setId(item.getId());
        dto.setMealId(item.getMealRecord().getId());
        dto.setFoodId(item.getFood().getId());
        dto.setNameZh(item.getFood().getNameZh());
        dto.setNameEn(item.getFood().getNameEn());
        dto.setWeightGram(w);
        dto.setSortOrder(item.getSortOrder());
        if (n != null) {
            dto.setEnergyKcal(calc(n.getEnergyKcal(), w));
            dto.setProtein(calc(n.getProtein(), w));
            dto.setFat(calc(n.getFat(), w));
            dto.setCarbohydrate(calc(n.getCarbohydrate(), w));
            dto.setFiber(calc(n.getFiber(), w));
        }
        return dto;
    }

    // ── JSON 解析工具 ────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) { return List.of(); }
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) { return List.of(); }
    }

    private List<RecipeIngredient> parseIngredients(String json) {
        return parseJsonList(json).stream().map(m -> {
            RecipeIngredient ri = new RecipeIngredient();
            ri.setName(strOf(m, "name"));
            ri.setAmount(strOf(m, "amount"));
            ri.setUnit(strOf(m, "unit"));
            Object fid = m.get("food_id");
            if (fid instanceof Number n) ri.setFoodId(n.longValue());
            return ri;
        }).collect(Collectors.toList());
    }

    private NutritionSummary parseNutrition(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            var n = objectMapper.readTree(json);
            NutritionSummary ns = new NutritionSummary();
            ns.setProteinG(nodeDouble(n, "protein_g"));
            ns.setFatG(nodeDouble(n, "fat_g"));
            ns.setCarbG(nodeDouble(n, "carb_g"));
            ns.setFiberG(nodeDouble(n, "fiber_g"));
            return ns;
        } catch (Exception e) { return null; }
    }

    // ── 食物匹配（多级渐进策略） ──────────────────────────────────────────────────

    /**
     * 多级渐进食材匹配（越往后容忍度越高）：
     * 1. 精确匹配
     * 2. MySQL FULLTEXT Boolean Mode
     * 3. 反向子串：DB名称是输入的子串 → 如"面条" in "细面条"，"猪肉" in "猪肉末"
     * 4. 正向子串：输入是DB名称的子串 → 如"里脊" in "猪里脊"
     * 5. 剥除修饰词后重试1-4 → "猪瘦肉"→"猪肉"，"新鲜番茄"→"番茄"
     * 6. 滑动窗口子串（≥3字符）
     * 7. 关键食材字匹配（跳过形态/颜色字，按字符重叠度打分）
     *    → "蒜泥"→"大蒜"，"芽菜"→"豆芽"，"红油"→"食用油"
     */
    private Optional<Food> findBestFoodMatch(String name) {
        // 1. 精确
        Optional<Food> r = foodRepository.findByNameZhAndIsActiveTrue(name);
        if (r.isPresent()) return r;

        // 2. FULLTEXT
        List<Food> suggestions = foodRepository.findSuggestions(name);
        if (!suggestions.isEmpty()) return Optional.of(suggestions.get(0));

        // 3 & 4. 子串双向匹配
        r = bilateralMatch(name);
        if (r.isPresent()) return r;

        // 5. 剥除修饰词
        String stripped = stripFoodModifiers(name);
        if (!stripped.equals(name) && stripped.length() >= 2) {
            Optional<Food> s = foodRepository.findByNameZhAndIsActiveTrue(stripped);
            if (s.isPresent()) return s;
            suggestions = foodRepository.findSuggestions(stripped);
            if (!suggestions.isEmpty()) return Optional.of(suggestions.get(0));
            s = bilateralMatch(stripped);
            if (s.isPresent()) return s;
        }

        // 6. 滑动窗口（≥3字符，最多10次查询）
        int len = name.length();
        if (len >= 4) {
            int attempts = 0;
            outer:
            for (int wLen = len - 1; wLen >= 3; wLen--) {
                for (int start = 0; start + wLen <= len; start++) {
                    if (++attempts > 10) break outer;
                    r = bilateralMatch(name.substring(start, start + wLen));
                    if (r.isPresent()) return r;
                }
            }
        }

        // 7. 关键食材字匹配（最终兜底）
        return findByKeyCharacters(name);
    }

    /** 先做反向子串匹配，再做正向子串匹配 */
    private Optional<Food> bilateralMatch(String name) {
        List<Food> contained = foodRepository.findWhereNameContainedIn(name);
        if (!contained.isEmpty()) return Optional.of(contained.get(0));
        List<Food> superStr  = foodRepository.findByNameZhContaining(name);
        if (!superStr.isEmpty())  return Optional.of(superStr.get(0));
        return Optional.empty();
    }

    /** 剥除中文食材常见修饰成分，保留核心食材名 */
    private static final List<String> FOOD_MODIFIERS = List.of(
            "新鲜", "冷冻", "速冻", "腌制", "腌渍", "腌",
            "去皮", "去骨", "去壳", "去籽", "去头", "去腥",
            "切丝", "切块", "切片", "切丁", "剁碎", "碎末",
            "里脊", "外脊", "腱子", "前腿", "后腿"
    );

    private String stripFoodModifiers(String name) {
        String s = name;
        for (String mod : FOOD_MODIFIERS) s = s.replace(mod, "");
        s = s.replace("瘦", "").replace("嫩", "");
        String result = s.trim();
        return result.length() >= 2 ? result : name;
    }

    /**
     * 形态/颜色/状态描述字：在食材名中通常不代表食材本身，
     * 单独搜索这些字只会带来大量噪音，因此在 Tier 7 中跳过。
     * 例："蒜泥"→跳过'泥'，只搜'蒜'；"红油"→跳过'红'，只搜'油'；
     *     "芽菜"→跳过'菜'，只搜'芽'；"猪肉末"→跳过'末'和'肉'，只搜'猪'
     */
    private static final Set<Character> NON_INGREDIENT_CHARS = Set.of(
            // 加工/形态
            '泥', '末', '丝', '块', '片', '汁', '浆', '粉', '粒', '段', '茸', '糊', '碎',
            // 质地/状态
            '细', '粗', '嫩', '老', '干', '湿', '熟', '生', '鲜', '瘦', '肥',
            // 颜色
            '红', '白', '黑', '黄', '绿', '青',
            // 过于宽泛的类别字（单独搜索返回大量无关结果）
            '菜', '肉', '鱼'
    );

    /**
     * 提取名称中的"关键食材字"（跳过 NON_INGREDIENT_CHARS），
     * 对每个关键字做包含搜索，按字符重叠度选最佳结果。
     * 字符重叠度 = 候选名称与输入共有的不同汉字数；同分时取名称最短者。
     */
    private Optional<Food> findByKeyCharacters(String name) {
        Map<Food, Integer> scoreMap = new LinkedHashMap<>();
        for (char ch : name.toCharArray()) {
            if (NON_INGREDIENT_CHARS.contains(ch)) continue;
            List<Food> candidates = foodRepository.findByNameZhContaining(String.valueOf(ch));
            for (Food f : candidates) {
                int overlap = characterOverlap(f.getNameZh(), name);
                scoreMap.merge(f, overlap, (a, b) -> a > b ? a : b);
            }
        }
        return scoreMap.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<Food, Integer>comparingByValue().reversed()
                        .thenComparingInt(e -> e.getKey().getNameZh().length()))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /** 计算两个字符串共有的不同汉字数 */
    private int characterOverlap(String candidate, String input) {
        Set<Character> inputChars = new HashSet<>();
        for (char c : input.toCharArray()) inputChars.add(c);
        int count = 0;
        Set<Character> seen = new HashSet<>();
        for (char c : candidate.toCharArray()) {
            if (inputChars.contains(c) && seen.add(c)) count++;
        }
        return count;
    }

    // ── 小工具 ───────────────────────────────────────────────────────────────────

    private String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
    private String strOf(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? v.toString() : ""; }
    private boolean isWeightUnit(String unit) { return unit != null && (unit.equalsIgnoreCase("g") || unit.equalsIgnoreCase("ml")); }
    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private BigDecimal calc(BigDecimal per100, BigDecimal w) {
        if (per100 == null) return null;
        return per100.multiply(w).divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);
    }
    private Double nodeDouble(com.fasterxml.jackson.databind.JsonNode n, String f) {
        var v = n.path(f); return (v.isMissingNode() || v.isNull()) ? null : v.asDouble();
    }
}
