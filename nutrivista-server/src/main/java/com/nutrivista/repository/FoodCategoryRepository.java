package com.nutrivista.repository;

import com.nutrivista.entity.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FoodCategoryRepository extends JpaRepository<FoodCategory, Integer> {

    /** 查询所有顶级分类（含子分类预加载） */
    @Query("SELECT c FROM FoodCategory c WHERE c.parent IS NULL ORDER BY c.sortOrder ASC")
    List<FoodCategory> findAllRootCategories();

    /** 按父分类查询子分类 */
    List<FoodCategory> findByParentIdOrderBySortOrderAsc(Integer parentId);
}
