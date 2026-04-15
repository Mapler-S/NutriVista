package com.nutrivista.service.impl;

import com.nutrivista.common.constant.MealType;
import com.nutrivista.common.exception.BusinessException;
import com.nutrivista.dto.data.ExportRowDto;
import com.nutrivista.dto.data.ImportResultDto;
import com.nutrivista.entity.Food;
import com.nutrivista.entity.FoodNutrition;
import com.nutrivista.entity.MealItem;
import com.nutrivista.entity.MealRecord;
import com.nutrivista.repository.FoodRepository;
import com.nutrivista.repository.MealItemRepository;
import com.nutrivista.repository.MealRecordRepository;
import com.nutrivista.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataServiceImpl implements DataService {

    private final MealRecordRepository mealRecordRepository;
    private final MealItemRepository   mealItemRepository;
    private final FoodRepository       foodRepository;

    // ================================================================
    //  Export
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public List<ExportRowDto> exportData(Long userId, LocalDate from, LocalDate to) {
        List<MealRecord> records =
                mealRecordRepository.findByUserIdAndDateRangeEager(userId, from, to);

        records.sort(Comparator.comparing(MealRecord::getMealDate)
                               .thenComparingInt(r -> r.getMealType().getSortOrder()));

        List<ExportRowDto> result = new ArrayList<>();
        for (MealRecord record : records) {
            String mealTypeName = record.getMealType().getDisplayName();
            for (MealItem item : record.getItems()) {
                FoodNutrition n  = item.getFood().getNutrition();
                BigDecimal weight = item.getWeightGram();
                result.add(new ExportRowDto(
                        record.getMealDate(),
                        mealTypeName,
                        item.getFood().getNameZh(),
                        weight,
                        calc(n != null ? n.getEnergyKcal()   : null, weight),
                        calc(n != null ? n.getProtein()       : null, weight),
                        calc(n != null ? n.getFat()           : null, weight),
                        calc(n != null ? n.getCarbohydrate()  : null, weight),
                        calc(n != null ? n.getFiber()         : null, weight)
                ));
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public String exportAsCsv(Long userId, LocalDate from, LocalDate to) {
        List<ExportRowDto> rows = exportData(userId, from, to);
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // UTF-8 BOM — Excel 直接识别中文
        sb.append("日期,餐次,食物名称,重量(g),能量(kcal),蛋白质(g),脂肪(g),碳水化合物(g),膳食纤维(g)\n");
        for (ExportRowDto row : rows) {
            sb.append(row.getDate()).append(',')
              .append(row.getMealType()).append(',')
              .append(csvCell(row.getFoodName())).append(',')
              .append(fmt(row.getWeightGram())).append(',')
              .append(fmt(row.getEnergyKcal())).append(',')
              .append(fmt(row.getProtein())).append(',')
              .append(fmt(row.getFat())).append(',')
              .append(fmt(row.getCarbohydrate())).append(',')
              .append(fmt(row.getFiber())).append('\n');
        }
        return sb.toString();
    }

    // ================================================================
    //  Import
    // ================================================================

    @Override
    @Transactional
    public ImportResultDto importFromCsv(Long userId, MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int total = 0, success = 0, skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            boolean firstLine = true;
            String  line;
            int     lineNo = 0;

            while ((line = reader.readLine()) != null) {
                lineNo++;
                // 剥离 BOM
                if (firstLine) {
                    if (line.startsWith("\uFEFF")) line = line.substring(1);
                    firstLine = false;
                    continue; // 跳过表头行
                }
                if (line.isBlank()) continue;

                total++;
                try {
                    processRow(userId, line);
                    success++;
                } catch (Exception e) {
                    errors.add("第 " + lineNo + " 行: " + e.getMessage());
                    skipped++;
                }
            }
        } catch (IOException e) {
            throw new BusinessException("文件读取失败: " + e.getMessage());
        }

        ImportResultDto result = new ImportResultDto();
        result.setTotal(total);
        result.setSuccess(success);
        result.setSkipped(skipped);
        result.setErrors(errors);
        return result;
    }

    private void processRow(Long userId, String line) {
        String[] cols = line.split(",", -1);
        if (cols.length < 4) throw new RuntimeException("列数不足（至少需要：日期,餐次,食物名称,重量）");

        // 1. 日期
        LocalDate date;
        try {
            date = LocalDate.parse(cols[0].trim());
        } catch (DateTimeParseException e) {
            throw new RuntimeException("日期格式无效，请使用 YYYY-MM-DD: " + cols[0].trim());
        }

        // 2. 餐次
        String mealTypeName = cols[1].trim();
        MealType mealType = Arrays.stream(MealType.values())
                .filter(t -> t.getDisplayName().equals(mealTypeName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("无效餐次: " + mealTypeName));

        // 3. 食物名称
        String foodName = cols[2].trim().replace("\"", ""); // 去除 CSV 引号
        if (foodName.isEmpty()) throw new RuntimeException("食物名称不能为空");

        // 4. 重量
        BigDecimal weight;
        try {
            weight = new BigDecimal(cols[3].trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("重量格式无效: " + cols[3].trim());
        }
        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("重量必须大于 0");
        }

        // 5. 查找食物
        Food food = foodRepository.findByNameZhAndIsActiveTrue(foodName)
                .orElseThrow(() -> new RuntimeException("食物库中未找到: " + foodName));

        // 6. 找到或创建 MealRecord
        MealRecord meal = mealRecordRepository
                .findByUserIdAndMealDateAndMealType(userId, date, mealType)
                .orElseGet(() -> {
                    MealRecord m = new MealRecord();
                    m.setUserId(userId);
                    m.setMealDate(date);
                    m.setMealType(mealType);
                    return mealRecordRepository.save(m);
                });

        // 7. 创建 MealItem
        MealItem item = new MealItem();
        item.setMealRecord(meal);
        item.setFood(food);
        item.setWeightGram(weight);
        item.setSortOrder(meal.getItems().size());
        mealItemRepository.save(item);
    }

    // ================================================================
    //  Helpers
    // ================================================================

    private BigDecimal calc(BigDecimal per100g, BigDecimal weight) {
        if (per100g == null) return BigDecimal.ZERO;
        return per100g.multiply(weight)
                      .divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);
    }

    private String fmt(BigDecimal val) {
        return val != null ? val.toPlainString() : "0";
    }

    /** 对含逗号或双引号的字段加 CSV 引号 */
    private String csvCell(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
