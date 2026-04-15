package com.nutrivista.repository;

import com.nutrivista.entity.MealItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealItemRepository extends JpaRepository<MealItem, Long> {

    /** Count items belonging to a meal record (avoids lazy-load of items collection) */
    long countByMealRecordId(Long mealRecordId);

    /** Fetch item with food + nutrition in one query */
    @Query("""
            SELECT i FROM MealItem i
            JOIN FETCH i.food f
            JOIN FETCH f.nutrition
            WHERE i.id = :id
            """)
    Optional<MealItem> findByIdWithFood(@Param("id") Long id);

    /**
     * 统计指定时段内该用户吃得最多的食物。
     * 返回 Object[]：[food_id, name_zh, name_en, times(Long), total_weight(BigDecimal)]
     */
    @Query(value = """
            SELECT f.id, f.name_zh, f.name_en,
                   COUNT(*) AS times,
                   COALESCE(SUM(mi.weight_gram), 0) AS total_weight
            FROM meal_item mi
            JOIN meal_record mr ON mr.id = mi.meal_id
            JOIN food f         ON f.id  = mi.food_id
            WHERE mr.user_id = :userId
              AND mr.meal_date BETWEEN :startDate AND :endDate
            GROUP BY f.id, f.name_zh, f.name_en
            ORDER BY times DESC
            LIMIT :lim
            """, nativeQuery = true)
    List<Object[]> findTopFoodsNative(
            @Param("userId")    Long      userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate,
            @Param("lim")       int       lim);
}
