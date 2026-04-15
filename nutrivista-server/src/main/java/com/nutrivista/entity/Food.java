package com.nutrivista.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "food")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private FoodCategory category;

    @Column(name = "name_zh", nullable = false, length = 200)
    private String nameZh;

    @Column(name = "name_en", nullable = false, length = 350)
    private String nameEn;

    @Column(name = "alias", length = 500)
    private String alias;

    @Column(name = "data_source", nullable = false, length = 30)
    private String dataSource = "CNF";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * 一对一营养成分（共享主键，延迟加载）
     * cascade ALL：保存 Food 时自动级联保存 FoodNutrition
     */
    @OneToOne(mappedBy = "food", cascade = CascadeType.ALL,
              fetch = FetchType.LAZY, optional = false)
    private FoodNutrition nutrition;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
