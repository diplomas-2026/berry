package com.company.product.api.dto;

import com.company.product.api.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, Long userId, String email, String fullName, UserRole role, String avatarUrl) {}
}
