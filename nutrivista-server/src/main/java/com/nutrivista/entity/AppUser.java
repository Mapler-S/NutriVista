package com.nutrivista.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "avatar", length = 500)
    private String avatar;

    /** 性别：1=男，2=女 */
    @Column(name = "gender")
    private Integer gender;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weightKg;

    @Column(name = "activity_level", nullable = false, length = 20)
    private String activityLevel = "MODERATE";

    @Column(name = "dietary_goal", nullable = false, length = 20)
    private String dietaryGoal = "MAINTAIN";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
