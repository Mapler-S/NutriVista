package com.nutrivista.service.impl;

import com.nutrivista.common.exception.BusinessException;
import com.nutrivista.dto.meal.AddItemRequest;
import com.nutrivista.dto.meal.DailySummaryDto;
import com.nutrivista.dto.meal.MealItemDto;
import com.nutrivista.dto.meal.MealRecordDto;
import com.nutrivista.dto.meal.UpdateItemRequest;
import com.nutrivista.entity.Food;
import com.nutrivista.entity.MealItem;
import com.nutrivista.entity.MealRecord;
import com.nutrivista.repository.FoodRepository;
import com.nutrivista.repository.MealItemRepository;
import com.nutrivista.repository.MealRecordRepository;
import com.nutrivista.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealRecordRepository mealRecordRepository;
    private final MealItemRepository   mealItemRepository;
    private final FoodRepository       foodRepository;

    // ===== getDailySummary =====

    @Override
    @Transactional(readOnly = true)
    public DailySummaryDto getDailySummary(Long userId, LocalDate date) {
        List<MealRecord> records =
                mealRecordRepository.findByUserIdAndMealDateEager(userId, date);

        BigDecimal totalEnergy = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalCarb = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;

        List<MealRecordDto> mealDtos = new ArrayList<>();
        for (MealRecord record : records) {
            MealRecordDto dto = buildMealRecordDto(record);
            mealDtos.add(dto);
            totalEnergy  = totalEnergy.add(nvl(dto.getTotalEnergyKcal()));
            totalProtein = totalProtein.add(nvl(dto.getTotalProtein()));
            totalFat     = totalFat.add(nvl(dto.getTotalFat()));
            totalCarb    = totalCarb.add(nvl(dto.getTotalCarbohydrate()));
            totalFiber   = totalFiber.add(nvl(dto.getTotalFiber()));
        }

        DailySummaryDto summary = new DailySummaryDto();
        summary.setDate(date);
        summary.setMeals(mealDtos);
        summary.setTotalEnergyKcal(totalEnergy);
        summary.setTotalProtein(totalProtein);
        summary.setTotalFat(totalFat);
        summary.setTotalCarbohydrate(totalCarb);
        summary.setTotalFiber(totalFiber);
        return summary;
    }

    // ===== addItem =====

    @Override
    @Transactional
    public MealItemDto addItem(Long userId, AddItemRequest req) {
        // find-or-create MealRecord
        MealRecord meal = mealRecordRepository
                .findByUserIdAndMealDateAndMealType(userId, req.getMealDate(), req.getMealType())
                .orElseGet(() -> {
                    MealRecord m = new MealRecord();
                    m.setUserId(userId);
                    m.setMealDate(req.getMealDate());
                    m.setMealType(req.getMealType());
                    return mealRecordRepository.save(m);
                });

        Food food = foodRepository.findByIdWithNutrition(req.getFoodId())
                .orElseThrow(() -> BusinessException.notFound("食物"));

        MealItem item = new MealItem();
        item.setMealRecord(meal);
        item.setFood(food);
        item.setWeightGram(req.getWeightGram());
        item.setSortOrder(meal.getItems().size());
        item = mealItemRepository.save(item);

        return buildItemDto(item);
    }

    // ===== updateItem =====

    @Override
    @Transactional
    public MealItemDto updateItem(Long itemId, UpdateItemRequest req) {
        MealItem item = mealItemRepository.findByIdWithFood(itemId)
                .orElseThrow(() -> BusinessException.notFound("饮食记录明细"));
        item.setWeightGram(req.getWeightGram());
        item = mealItemRepository.save(item);
        return buildItemDto(item);
    }

    // ===== removeItem =====

    @Override
    @Transactional
    public void removeItem(Long itemId) {
        if (!mealItemRepository.existsById(itemId)) {
            throw BusinessException.notFound("饮食记录明细");
        }
        mealItemRepository.deleteById(itemId);
    }

    // ===== removeMeal =====

    @Override
    @Transactional
    public void removeMeal(Long mealId) {
        if (!mealRecordRepository.existsById(mealId)) {
            throw BusinessException.notFound("饮食记录");
        }
        mealRecordRepository.deleteById(mealId);
    }

    // ===== private helpers =====

    private MealRecordDto buildMealRecordDto(MealRecord record) {
        List<MealItemDto> itemDtos = record.getItems().stream()
                .map(this::buildItemDto)
                .toList();

        BigDecimal energy = BigDecimal.ZERO, protein = BigDecimal.ZERO,
                   fat = BigDecimal.ZERO, carb = BigDecimal.ZERO, fiber = BigDecimal.ZERO;
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

    /** 按实际克重换算营养值（per100g × weight / 100），保留1位小数 */
    private BigDecimal calc(BigDecimal per100g, BigDecimal weightGram) {
        if (per100g == null) return null;
        return per100g.multiply(weightGram)
                      .divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
