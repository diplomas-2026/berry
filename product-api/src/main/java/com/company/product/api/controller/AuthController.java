package com.company.product.api.controller;

import com.company.product.api.dto.AuthDtos;
import com.company.product.api.service.AuthService;
import com.company.product.api.service.CurrentUserService;
import com.company.product.api.service.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final DtoMapper mapper;

    public AuthController(AuthService authService, CurrentUserService currentUserService, DtoMapper mapper) {
        this.authService = authService;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public Object me() {
        return mapper.toUserDto(currentUserService.requireUser());
    }
}
