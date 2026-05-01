package com.company.product.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class MealVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private AppUser student;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealSlot mealSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "issued_by_curator_id")
    private AppUser issuedByCurator;

    @ManyToOne
    @JoinColumn(name = "redeemed_by_chef_id")
    private AppUser redeemedByChef;

    private LocalDateTime redeemedAt;
}
