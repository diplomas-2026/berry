package com.company.product.api.controller;

import com.company.product.api.dto.CommonDtos;
import com.company.product.api.repository.DishRepository;
import com.company.product.api.service.DtoMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dishes")
@PreAuthorize("isAuthenticated()")
public class DishController {
    private final DishRepository dishRepository;
    private final DtoMapper mapper;

    public DishController(DishRepository dishRepository, DtoMapper mapper) {
        this.dishRepository = dishRepository;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public CommonDtos.DishDto dish(@PathVariable Long id) {
        return mapper.toDishDto(dishRepository.findById(id).orElseThrow());
    }
}
