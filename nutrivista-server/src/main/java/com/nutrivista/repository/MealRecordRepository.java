package com.nutrivista.repository;

import com.nutrivista.common.constant.MealType;
import com.nutrivista.entity.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    Optional<MealRecord> findByUserIdAndMealDateAndMealType(
            Long userId, LocalDate mealDate, MealType mealType);

    @Query("SELECT m FROM MealRecord m WHERE m.userId = :userId AND m.mealDate = :date ORDER BY m.mealType ASC")
    List<MealRecord> findByUserIdAndMealDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT m FROM MealRecord m WHERE m.userId = :userId AND m.mealDate BETWEEN :from AND :to ORDER BY m.mealDate ASC, m.mealType ASC")
    List<MealRecord> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /** 一次性加载当天所有餐次 + 明细 + 食物 + 营养，避免 N+1 */
    @Query("""
            SELECT DISTINCT m FROM MealRecord m
            LEFT JOIN FETCH m.items i
            LEFT JOIN FETCH i.food f
            LEFT JOIN FETCH f.nutrition
            WHERE m.userId = :userId AND m.mealDate = :date
            ORDER BY m.mealType ASC
            """)
    List<MealRecord> findByUserIdAndMealDateEager(
            @Param("userId") Long userId,
            @Param("date") LocalDate date);

    /** 日期范围内所有餐次 + 明细 + 食物 + 营养（供统计使用） */
    @Query("""
            SELECT DISTINCT m FROM MealRecord m
            LEFT JOIN FETCH m.items i
            LEFT JOIN FETCH i.food f
            LEFT JOIN FETCH f.nutrition
            WHERE m.userId = :userId
              AND m.mealDate BETWEEN :from AND :to
            """)
    List<MealRecord> findByUserIdAndDateRangeEager(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
