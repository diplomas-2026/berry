package com.company.product.api.controller;

import com.company.product.api.dto.CommonDtos;
import com.company.product.api.dto.RequestDtos;
import com.company.product.api.entity.*;
import com.company.product.api.repository.DishRepository;
import com.company.product.api.repository.MealVoucherRepository;
import com.company.product.api.repository.MenuItemRepository;
import com.company.product.api.repository.AppUserRepository;
import com.company.product.api.service.CurrentUserService;
import com.company.product.api.service.DtoMapper;
import com.company.product.api.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chef")
@PreAuthorize("hasRole('CHEF')")
public class ChefController {
    private final DishRepository dishRepository;
    private final MenuItemRepository menuItemRepository;
    private final MealVoucherRepository voucherRepository;
    private final AppUserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final DtoMapper mapper;
    private final StorageService storageService;

    public ChefController(DishRepository dishRepository, MenuItemRepository menuItemRepository, MealVoucherRepository voucherRepository, AppUserRepository userRepository, CurrentUserService currentUserService, DtoMapper mapper, StorageService storageService) {
        this.dishRepository = dishRepository;
        this.menuItemRepository = menuItemRepository;
        this.voucherRepository = voucherRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
        this.storageService = storageService;
    }

    @PostMapping("/dishes")
    public Object createDish(@Valid @RequestBody RequestDtos.CreateDishRequest request) {
        Dish dish = new Dish();
        dish.setName(request.name());
        dish.setDescription(request.description());
        dish.setCreatedByChef(currentUserService.requireUser());
        return mapper.toDishDto(dishRepository.save(dish));
    }

    @PostMapping(value = "/dishes/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadDishPhoto(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        Dish dish = dishRepository.findById(id).orElseThrow();
        dish.setPhotoPath(storageService.store(file, "dishes"));
        return mapper.toDishDto(dishRepository.save(dish));
    }

    @DeleteMapping("/dishes/{id}/photo")
    public Object deleteDishPhoto(@PathVariable Long id) {
        Dish dish = dishRepository.findById(id).orElseThrow();
        dish.setPhotoPath(null);
        return mapper.toDishDto(dishRepository.save(dish));
    }

    @PostMapping("/menu/items")
    public Object addMenuItem(@Valid @RequestBody RequestDtos.AddMenuItemRequest request) {
        Dish dish = dishRepository.findById(request.dishId()).orElseThrow();
        MenuItem menuItem = new MenuItem();
        menuItem.setMenuDate(request.date());
        menuItem.setMealSlot(request.mealSlot());
        menuItem.setDish(dish);
        return mapper.toMenuItemDto(menuItemRepository.save(menuItem));
    }

    @GetMapping("/menu/current")
    public List<?> menuCurrent() {
        return menuItemRepository.findByMenuDate(LocalDate.now()).stream().map(mapper::toMenuItemDto).toList();
    }

    @PostMapping("/scan")
    public CommonDtos.ScanResultDto scan(@Valid @RequestBody CommonDtos.QrPayload payload) {
        AppUser student = userRepository.findById(payload.studentId()).orElseThrow();
        List<MealVoucher> vouchers = voucherRepository.findByStudentIdAndIssueDateAndStatus(student.getId(), payload.date(), VoucherStatus.ISSUED);
        List<MenuItem> menu = menuItemRepository.findByMenuDate(payload.date());
        List<MenuItem> allowedMenu = menu.stream().filter(mi ->
                vouchers.stream().anyMatch(v -> v.getMealSlot() == mi.getMealSlot())
        ).toList();
        return new CommonDtos.ScanResultDto(
                mapper.toUserDto(student),
                vouchers.stream().map(mapper::toVoucherDto).toList(),
                allowedMenu.stream().map(mapper::toMenuItemDto).toList()
        );
    }

    @PostMapping("/redeem")
    public Object redeem(@Valid @RequestBody RequestDtos.RedeemRequest request) {
        MealVoucher voucher = voucherRepository.findById(request.voucherId()).orElseThrow();
        if (voucher.getStatus() != VoucherStatus.ISSUED) {
            throw new IllegalStateException("Талон уже погашен");
        }
        voucher.setStatus(VoucherStatus.REDEEMED);
        voucher.setRedeemedByChef(currentUserService.requireUser());
        voucher.setRedeemedAt(LocalDateTime.now());
        return mapper.toVoucherDto(voucherRepository.save(voucher));
    }
}
