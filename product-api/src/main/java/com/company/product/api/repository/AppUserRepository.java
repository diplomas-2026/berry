package com.company.product.api.repository;

import com.company.product.api.entity.AppUser;
import com.company.product.api.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    List<AppUser> findByRole(UserRole role);
}
