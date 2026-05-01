package com.company.product.api.dto;

import com.company.product.api.entity.MealSlot;
import com.company.product.api.entity.UserRole;
import com.company.product.api.entity.VoucherStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CommonDtos {
    public record UserDto(Long id, String email, String firstName, String lastName, String middleName, String fullName, UserRole role, boolean active, String avatarUrl) {}
    public record DishDto(Long id, String name, String description, BigDecimal proteinsPer100g, BigDecimal fatsPer100g, BigDecimal carbsPer100g, BigDecimal caloriesPer100g, String photoUrl) {}
    public record MenuItemDto(Long id, LocalDate date, MealSlot mealSlot, DishDto dish) {}
    public record VoucherDto(Long id, Long studentId, String studentName, LocalDate issueDate, MealSlot mealSlot, VoucherStatus status, LocalDateTime redeemedAt) {}
    public record QrPayload(Long studentId, LocalDate date) {}
    public record ScanResultDto(UserDto student, List<VoucherDto> activeVouchers, List<MenuItemDto> menuItems) {}
    public record GroupDto(Long id, String name, Long curatorId, String curatorName) {}
}
