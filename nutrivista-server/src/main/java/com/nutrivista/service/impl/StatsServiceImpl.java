package com.nutrivista.service.impl;

import com.nutrivista.dto.stats.*;
import com.nutrivista.entity.FoodNutrition;
import com.nutrivista.entity.MealItem;
import com.nutrivista.entity.MealRecord;
import com.nutrivista.repository.MealItemRepository;
import com.nutrivista.repository.MealRecordRepository;
import com.nutrivista.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final MealRecordRepository mealRecordRepository;
    private final MealItemRepository   mealItemRepository;

    // ========================================================
    // 中国居民膳食参考摄入量 (DRI 2023，成年男性轻体力活动)
    // ========================================================
    private record DriEntry(String label, double value, String unit) {}

    private static final LinkedHashMap<String, DriEntry> DRI = new LinkedHashMap<>();
    static {
        DRI.put("energyKcal",   new DriEntry("能量",      2000, "kcal"));
        DRI.put("protein",      new DriEntry("蛋白质",     75,   "g"));
        DRI.put("fat",          new DriEntry("脂肪",       65,   "g"));
        DRI.put("carbohydrate", new DriEntry("碳水化合物", 275,  "g"));
        DRI.put("fiber",        new DriEntry("膳食纤维",   25,   "g"));
        DRI.put("vitaminC",     new DriEntry("维生素C",    100,  "mg"));
        DRI.put("calcium",      new DriEntry("钙",         800,  "mg"));
        DRI.put("iron",         new DriEntry("铁",         12,   "mg"));
        DRI.put("zinc",         new DriEntry("锌",         12.5, "mg"));
        DRI.put("vitaminA",     new DriEntry("维生素A",    800,  "μg"));
        DRI.put("vitaminB1",    new DriEntry("维生素B1",   1.4,  "mg"));
        DRI.put("vitaminB2",    new DriEntry("维生素B2",   1.4,  "mg"));
    }

    // ========================================================
    // getDashboard
    // ========================================================

    @Override
    public DashboardDto getDashboard(Long userId, LocalDate date, int days) {
        LocalDate startDate = date.minusDays(days - 1L);

        // 一次性加载该时段所有餐次 + 食物 + 营养
        List<MealRecord> records =
                mealRecordRepository.findByUserIdAndDateRangeEager(userId, startDate, date);

        // ---- 按日期聚合营养 ----
        Map<LocalDate, DailyNutritionDto> dailyMap = new TreeMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            dailyMap.put(d, new DailyNutritionDto(d));
        }
        for (MealRecord record : records) {
            DailyNutritionDto daily = dailyMap.get(record.getMealDate());
            if (daily == null) continue;
            for (MealItem item : record.getItems()) {
                accumulate(daily, item);
            }
        }

        List<DailyNutritionDto> trend = new ArrayList<>(dailyMap.values());
        DailyNutritionDto today = dailyMap.getOrDefault(date, new DailyNutritionDto(date));

        // ---- 宏量热量占比 ----
        BigDecimal pKcal = today.getProtein().multiply(BigDecimal.valueOf(4));
        BigDecimal fKcal = today.getFat().multiply(BigDecimal.valueOf(9));
        BigDecimal cKcal = today.getCarbohydrate().multiply(BigDecimal.valueOf(4));
        BigDecimal total = pKcal.add(fKcal).add(cKcal);
        BigDecimal proteinPct = BigDecimal.ZERO, fatPct = BigDecimal.ZERO, carbPct = BigDecimal.ZERO;
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            proteinPct = pKcal.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
            fatPct     = fKcal.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
            carbPct    = cKcal.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
        }

        // ---- 最近7天平均热量 ----
        long activeDaysLast7 = trend.stream()
                .filter(d -> !d.getDate().isBefore(date.minusDays(6)))
                .filter(d -> d.getEnergyKcal().compareTo(BigDecimal.ZERO) > 0)
                .count();
        BigDecimal sumLast7 = trend.stream()
                .filter(d -> !d.getDate().isBefore(date.minusDays(6)))
                .map(DailyNutritionDto::getEnergyKcal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weekAvgEnergy = activeDaysLast7 > 0
                ? sumLast7.divide(BigDecimal.valueOf(activeDaysLast7), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ---- 有记录天数 & 连续打卡 ----
        Set<LocalDate> loggedDates = new HashSet<>();
        for (DailyNutritionDto d : trend) {
            if (d.getEnergyKcal().compareTo(BigDecimal.ZERO) > 0) loggedDates.add(d.getDate());
        }
        int totalLoggedDays = loggedDates.size();
        int currentStreak = calcStreak(loggedDates, date);

        // ---- 营养充足度 ----
        List<NutrientAdequacyDto> adequacy = buildAdequacy(today);

        // ---- 餐次分布 ----
        List<MealDistributionDto> mealDistribution = buildMealDistribution(records);

        // ---- 高频食物 Top 10 ----
        List<TopFoodDto> topFoods = buildTopFoods(userId, startDate, date);

        // ---- 组装 ----
        DashboardDto dto = new DashboardDto();
        dto.setDate(date);
        dto.setTrendDays(days);
        dto.setToday(today);
        dto.setProteinPct(proteinPct);
        dto.setFatPct(fatPct);
        dto.setCarbPct(carbPct);
        dto.setWeekAvgEnergy(weekAvgEnergy);
        dto.setTotalLoggedDays(totalLoggedDays);
        dto.setCurrentStreak(currentStreak);
        dto.setTrend(trend);
        dto.setNutrientAdequacy(adequacy);
        dto.setMealDistribution(mealDistribution);
        dto.setTopFoods(topFoods);
        return dto;
    }

    // ========================================================
    // private helpers
    // ========================================================

    /** 将单个 MealItem 的营养累加到当天汇总 */
    private void accumulate(DailyNutritionDto d, MealItem item) {
        BigDecimal w = item.getWeightGram();
        FoodNutrition n = item.getFood().getNutrition();
        if (n == null) return;
        d.setEnergyKcal(d.getEnergyKcal().add(calc(n.getEnergyKcal(), w)));
        d.setProtein(d.getProtein().add(calc(n.getProtein(), w)));
        d.setFat(d.getFat().add(calc(n.getFat(), w)));
        d.setCarbohydrate(d.getCarbohydrate().add(calc(n.getCarbohydrate(), w)));
        d.setFiber(d.getFiber().add(calc(n.getFiber(), w)));
        d.setVitaminC(d.getVitaminC().add(calc(n.getVitaminC(), w)));
        d.setCalcium(d.getCalcium().add(calc(n.getCalcium(), w)));
        d.setIron(d.getIron().add(calc(n.getIron(), w)));
        d.setZinc(d.getZinc().add(calc(n.getZinc(), w)));
        d.setVitaminA(d.getVitaminA().add(calc(n.getVitaminA(), w)));
        d.setVitaminB1(d.getVitaminB1().add(calc(n.getVitaminB1(), w)));
        d.setVitaminB2(d.getVitaminB2().add(calc(n.getVitaminB2(), w)));
    }

    /** per100g × weight / 100，null 返回 ZERO */
    private BigDecimal calc(BigDecimal per100g, BigDecimal weight) {
        if (per100g == null) return BigDecimal.ZERO;
        return per100g.multiply(weight).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /** 从 date 向前连续打卡天数 */
    private int calcStreak(Set<LocalDate> loggedDates, LocalDate date) {
        int streak = 0;
        for (int i = 0; i <= 365; i++) {
            if (loggedDates.contains(date.minusDays(i))) {
                streak++;
            } else if (i > 0) {
                break; // 今天允许还没记录
            }
        }
        return streak;
    }

    /** 营养充足度列表 */
    private List<NutrientAdequacyDto> buildAdequacy(DailyNutritionDto today) {
        List<NutrientAdequacyDto> list = new ArrayList<>();
        for (var entry : DRI.entrySet()) {
            BigDecimal actual = getField(today, entry.getKey());
            BigDecimal drv    = BigDecimal.valueOf(entry.getValue().value());
            BigDecimal pct    = actual.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : actual.divide(drv, 4, RoundingMode.HALF_UP)
                             .multiply(BigDecimal.valueOf(100))
                             .min(BigDecimal.valueOf(200))
                             .setScale(1, RoundingMode.HALF_UP);
            list.add(new NutrientAdequacyDto(
                    entry.getValue().label(),
                    actual.setScale(1, RoundingMode.HALF_UP),
                    drv,
                    entry.getValue().unit(),
                    pct));
        }
        return list;
    }

    private BigDecimal getField(DailyNutritionDto d, String field) {
        return switch (field) {
            case "energyKcal"   -> d.getEnergyKcal();
            case "protein"      -> d.getProtein();
            case "fat"          -> d.getFat();
            case "carbohydrate" -> d.getCarbohydrate();
            case "fiber"        -> d.getFiber();
            case "vitaminC"     -> d.getVitaminC();
            case "calcium"      -> d.getCalcium();
            case "iron"         -> d.getIron();
            case "zinc"         -> d.getZinc();
            case "vitaminA"     -> d.getVitaminA();
            case "vitaminB1"    -> d.getVitaminB1();
            case "vitaminB2"    -> d.getVitaminB2();
            default             -> BigDecimal.ZERO;
        };
    }

    /** 餐次平均热量分布 */
    private List<MealDistributionDto> buildMealDistribution(List<MealRecord> records) {
        Map<String, List<BigDecimal>> map = new LinkedHashMap<>();
        List.of("BREAKFAST","MORNING_SNACK","LUNCH","AFTERNOON_TEA","DINNER","SUPPER")
                .forEach(t -> map.put(t, new ArrayList<>()));

        for (MealRecord r : records) {
            BigDecimal mealKcal = BigDecimal.ZERO;
            for (MealItem i : r.getItems()) {
                FoodNutrition n = i.getFood().getNutrition();
                if (n != null) mealKcal = mealKcal.add(calc(n.getEnergyKcal(), i.getWeightGram()));
            }
            if (mealKcal.compareTo(BigDecimal.ZERO) > 0) {
                map.get(r.getMealType().name()).add(mealKcal);
            }
        }

        List<MealDistributionDto> result = new ArrayList<>();
        map.forEach((type, totals) -> {
            if (!totals.isEmpty()) {
                BigDecimal avg = totals.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(totals.size()), 1, RoundingMode.HALF_UP);
                result.add(new MealDistributionDto(type, avg, totals.size()));
            }
        });
        return result;
    }

    /** 高频食物 Top 10 */
    private List<TopFoodDto> buildTopFoods(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = mealItemRepository.findTopFoodsNative(userId, startDate, endDate, 10);
        List<TopFoodDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            TopFoodDto dto = new TopFoodDto();
            dto.setFoodId(((Number) row[0]).longValue());
            dto.setNameZh((String) row[1]);
            dto.setNameEn((String) row[2]);
            dto.setTimes(((Number) row[3]).intValue());
            dto.setTotalWeight(new BigDecimal(row[4].toString()));
            result.add(dto);
        }
        return result;
    }
}
