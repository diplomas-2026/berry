package com.company.product.api.controller;

import com.company.product.api.dto.CommonDtos;
import com.company.product.api.repository.MealVoucherRepository;
import com.company.product.api.repository.MenuItemRepository;
import com.company.product.api.service.CurrentUserService;
import com.company.product.api.service.DtoMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {
    private final CurrentUserService currentUserService;
    private final MealVoucherRepository voucherRepository;
    private final MenuItemRepository menuItemRepository;
    private final DtoMapper mapper;

    public StudentController(CurrentUserService currentUserService, MealVoucherRepository voucherRepository, MenuItemRepository menuItemRepository, DtoMapper mapper) {
        this.currentUserService = currentUserService;
        this.voucherRepository = voucherRepository;
        this.menuItemRepository = menuItemRepository;
        this.mapper = mapper;
    }

    @GetMapping("/vouchers")
    public List<?> vouchers() {
        return voucherRepository.findByStudentIdOrderByIssueDateDesc(currentUserService.requireUser().getId())
                .stream().map(mapper::toVoucherDto).toList();
    }

    @GetMapping("/menu/today")
    public List<?> todayMenu() {
        LocalDate today = LocalDate.now();
        return menuItemRepository.findByMenuDateOrderByMealSlotAsc(today).stream().map(mapper::toMenuItemDto).toList();
    }

    @GetMapping("/qr")
    public CommonDtos.QrPayload qr() {
        Long studentId = currentUserService.requireUser().getId();
        return new CommonDtos.QrPayload(studentId, LocalDate.now());
    }
}
