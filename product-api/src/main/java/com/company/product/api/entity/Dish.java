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
    private BigDecimal proteinsPer100g;
    private BigDecimal fatsPer100g;
    private BigDecimal carbsPer100g;
    private BigDecimal caloriesPer100g;
    private String photoPath;

    @ManyToOne
    @JoinColumn(name = "created_by_chef_id")
    private AppUser createdByChef;
}
