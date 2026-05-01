package com.company.product.api.service;

import com.company.product.api.dto.AuthDtos;
import com.company.product.api.entity.AppUser;
import com.company.product.api.entity.UserRole;
import com.company.product.api.repository.AppUserRepository;
import com.company.product.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class AuthServiceTest {
    @Test
    void loginSuccess() {
        AppUserRepository repo = Mockito.mock(AppUserRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        JwtService jwt = Mockito.mock(JwtService.class);
        AuthService service = new AuthService(repo, encoder, jwt);

        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("student@pgk.local");
        user.setPasswordHash("hash");
        user.setFullName("Student");
        user.setRole(UserRole.STUDENT);
        user.setActive(true);

        Mockito.when(repo.findByEmail("student@pgk.local")).thenReturn(Optional.of(user));
        Mockito.when(encoder.matches(anyString(), anyString())).thenReturn(true);
        Mockito.when(jwt.generateToken(1L, "student@pgk.local", "STUDENT")).thenReturn("token");

        AuthDtos.AuthResponse response = service.login(new AuthDtos.LoginRequest("student@pgk.local", "pass"));
        assertEquals("token", response.token());
        assertEquals(UserRole.STUDENT, response.role());
    }

    @Test
    void loginFail() {
        AppUserRepository repo = Mockito.mock(AppUserRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        JwtService jwt = Mockito.mock(JwtService.class);
        AuthService service = new AuthService(repo, encoder, jwt);

        Mockito.when(repo.findByEmail("x@x.com")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> service.login(new AuthDtos.LoginRequest("x@x.com", "123")));
    }
}
