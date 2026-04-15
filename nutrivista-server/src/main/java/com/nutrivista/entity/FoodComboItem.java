package com.nutrivista.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "food_combo_item")
public class FoodComboItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    private FoodCombo combo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "default_weight_gram", nullable = false, precision = 8, scale = 2)
    private BigDecimal defaultWeightGram = BigDecimal.valueOf(100);

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
