package com.company.product.api.repository;

import com.company.product.api.entity.MealSlot;
import com.company.product.api.entity.MealVoucher;
import com.company.product.api.entity.VoucherStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealVoucherRepository extends JpaRepository<MealVoucher, Long> {
    @EntityGraph(attributePaths = {"student"})
    List<MealVoucher> findByStudentIdOrderByIssueDateDesc(Long studentId);

    @EntityGraph(attributePaths = {"student"})
    Optional<MealVoucher> findByStudentIdAndIssueDateAndMealSlot(Long studentId, LocalDate issueDate, MealSlot mealSlot);

    @EntityGraph(attributePaths = {"student"})
    List<MealVoucher> findByStudentIdAndIssueDateAndStatus(Long studentId, LocalDate issueDate, VoucherStatus status);
}
