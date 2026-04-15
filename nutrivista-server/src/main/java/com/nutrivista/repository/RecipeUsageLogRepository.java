package com.nutrivista.repository;

import com.nutrivista.entity.RecipeUsageLog;
import com.nutrivista.entity.RecipeUsageLog.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RecipeUsageLogRepository extends JpaRepository<RecipeUsageLog, Long> {

    /** 某时段内某类型的事件总数 */
    long countByUserIdAndEventTypeAndMealDateBetween(
            Long userId, EventType eventType, LocalDate start, LocalDate end);

    /**
     * 按菜系 + 事件类型聚合：[cuisine, eventType, count]
     * 只统计有菜系信息的记录
     */
    @Query(value = """
            SELECT r.cuisine, r.event_type, COUNT(*) AS cnt
            FROM recipe_usage_log r
            WHERE r.user_id   = :userId
              AND r.meal_date BETWEEN :start AND :end
              AND r.cuisine   IS NOT NULL
            GROUP BY r.cuisine, r.event_type
            ORDER BY cnt DESC
            """, nativeQuery = true)
    List<Object[]> countByCuisineAndEventType(
            @Param("userId") Long userId,
            @Param("start")  LocalDate start,
            @Param("end")    LocalDate end);

    /**
     * Top 采用菜谱：[recipeId, recipeName, cuisine, count]
     */
    @Query(value = """
            SELECT r.recipe_id, r.recipe_name, r.cuisine, COUNT(*) AS cnt
            FROM recipe_usage_log r
            WHERE r.user_id    = :userId
              AND r.event_type = 'ADOPTED'
              AND r.meal_date  BETWEEN :start AND :end
            GROUP BY r.recipe_id, r.recipe_name, r.cuisine
            ORDER BY cnt DESC
            LIMIT :lim
            """, nativeQuery = true)
    List<Object[]> findTopAdoptedRecipes(
            @Param("userId") Long userId,
            @Param("start")  LocalDate start,
            @Param("end")    LocalDate end,
            @Param("lim")    int lim);

    /**
     * 按日期 + 事件类型聚合：[date, eventType, count]
     * 用于折线图趋势
     */
    @Query(value = """
            SELECT r.meal_date, r.event_type, COUNT(*) AS cnt
            FROM recipe_usage_log r
            WHERE r.user_id   = :userId
              AND r.meal_date BETWEEN :start AND :end
            GROUP BY r.meal_date, r.event_type
            ORDER BY r.meal_date
            """, nativeQuery = true)
    List<Object[]> countByDateAndEventType(
            @Param("userId") Long userId,
            @Param("start")  LocalDate start,
            @Param("end")    LocalDate end);
}
