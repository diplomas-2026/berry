package com.company.product.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "proteins_per_100g")
    private BigDecimal proteinsPer100g;

    @Column(name = "fats_per_100g")
    private BigDecimal fatsPer100g;

    @Column(name = "carbs_per_100g")
    private BigDecimal carbsPer100g;

    @Column(name = "calories_per_100g")
    private BigDecimal caloriesPer100g;

    @Column(name = "photo_path")
    private String photoPath;

    @ManyToOne
    @JoinColumn(name = "created_by_chef_id")
    private AppUser createdByChef;
}
