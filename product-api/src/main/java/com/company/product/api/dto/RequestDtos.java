package com.company.product.api.dto;

import com.company.product.api.entity.MealSlot;
import com.company.product.api.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class RequestDtos {
    public record CreateUserRequest(@Email String email, @NotBlank String password, @NotBlank String fullName, @NotNull UserRole role) {}
    public record UpdateUserRequest(@NotNull Boolean active) {}
    public record CreateGroupRequest(@NotBlank String name, Long curatorId) {}
    public record AssignStudentRequest(@NotNull Long studentId) {}
    public record IssueVoucherRequest(@NotNull Long studentId, @NotNull LocalDate date, @NotEmpty List<MealSlot> slots) {}
    public record CreateDishRequest(@NotBlank String name, String description) {}
    public record AddMenuItemRequest(@NotNull LocalDate date, @NotNull MealSlot mealSlot, @NotNull Long dishId) {}
    public record RedeemRequest(@NotNull Long voucherId) {}
}
