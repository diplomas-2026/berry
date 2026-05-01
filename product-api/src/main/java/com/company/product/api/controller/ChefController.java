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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@RestController
@Validated
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

    @PostMapping(value = "/dishes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createDish(@Valid @RequestBody RequestDtos.CreateDishRequest request) {
        Dish dish = new Dish();
        dish.setName(request.name());
        dish.setDescription(request.description());
        dish.setCreatedByChef(currentUserService.requireUser());
        return mapper.toDishDto(dishRepository.save(dish));
    }

    @PostMapping(value = "/dishes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object createDishMultipart(
            @RequestParam @jakarta.validation.constraints.NotBlank String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal proteinsPer100g,
            @RequestParam(required = false) BigDecimal fatsPer100g,
            @RequestParam(required = false) BigDecimal carbsPer100g,
            @RequestParam(required = false) BigDecimal caloriesPer100g,
            @RequestPart(required = false) MultipartFile file
    ) {
        Dish dish = new Dish();
        return mapper.toDishDto(saveDish(
                dish,
                name,
                description,
                proteinsPer100g,
                fatsPer100g,
                carbsPer100g,
                caloriesPer100g,
                file
        ));
    }

    @GetMapping("/dishes")
    public List<?> dishes() {
        return dishRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(mapper::toDishDto)
                .toList();
    }

    @PatchMapping(value = "/dishes/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object updateDishMultipart(
            @PathVariable Long id,
            @RequestParam @jakarta.validation.constraints.NotBlank String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal proteinsPer100g,
            @RequestParam(required = false) BigDecimal fatsPer100g,
            @RequestParam(required = false) BigDecimal carbsPer100g,
            @RequestParam(required = false) BigDecimal caloriesPer100g,
            @RequestPart(required = false) MultipartFile file
    ) {
        Dish dish = dishRepository.findById(id).orElseThrow();
        return mapper.toDishDto(saveDish(
                dish,
                name,
                description,
                proteinsPer100g,
                fatsPer100g,
                carbsPer100g,
                caloriesPer100g,
                file
        ));
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
        return saveMenuItem(new MenuItem(), request.date(), request.mealSlot(), request.dishId());
    }

    @GetMapping("/menu/current")
    public List<?> menuCurrent() {
        return menu(LocalDate.now());
    }

    @GetMapping("/menu")
    public List<?> menu(@RequestParam(required = false) LocalDate date) {
        LocalDate menuDate = date == null ? LocalDate.now() : date;
        return menuItemRepository.findByMenuDateOrderByMealSlotAsc(menuDate).stream().map(mapper::toMenuItemDto).toList();
    }

    @GetMapping("/menu/dates")
    public List<LocalDate> menuDates() {
        return menuItemRepository.findDistinctMenuDates();
    }

    @PatchMapping("/menu/items/{id}")
    public Object updateMenuItem(@PathVariable Long id, @Valid @RequestBody RequestDtos.UpdateMenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(id).orElseThrow();
        return saveMenuItem(menuItem, request.date(), request.mealSlot(), request.dishId());
    }

    @DeleteMapping("/menu/items/{id}")
    public void deleteMenuItem(@PathVariable Long id) {
        menuItemRepository.deleteById(id);
    }

    @PostMapping("/scan")
    public CommonDtos.ScanResultDto scan(@Valid @RequestBody CommonDtos.QrPayload payload) {
        AppUser student = userRepository.findById(payload.studentId()).orElseThrow();
        List<MealVoucher> vouchers = voucherRepository.findByStudentIdAndIssueDateAndStatus(student.getId(), payload.date(), VoucherStatus.ISSUED);
        List<MenuItem> menu = menuItemRepository.findByMenuDateOrderByMealSlotAsc(payload.date());
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

    private Object saveMenuItem(MenuItem menuItem, LocalDate date, MealSlot mealSlot, Long dishId) {
        Dish dish = dishRepository.findById(dishId).orElseThrow();
        menuItem.setMenuDate(date);
        menuItem.setMealSlot(mealSlot);
        menuItem.setDish(dish);
        return mapper.toMenuItemDto(menuItemRepository.save(menuItem));
    }

    private Dish saveDish(
            Dish dish,
            String name,
            String description,
            BigDecimal proteinsPer100g,
            BigDecimal fatsPer100g,
            BigDecimal carbsPer100g,
            BigDecimal caloriesPer100g,
            MultipartFile file
    ) {
        dish.setName(name);
        dish.setDescription(description);
        dish.setProteinsPer100g(proteinsPer100g);
        dish.setFatsPer100g(fatsPer100g);
        dish.setCarbsPer100g(carbsPer100g);
        dish.setCaloriesPer100g(caloriesPer100g);
        if (file != null && !file.isEmpty()) {
            dish.setPhotoPath(storageService.store(file, "dishes"));
        }
        if (dish.getCreatedByChef() == null) {
            dish.setCreatedByChef(currentUserService.requireUser());
        }
        return dishRepository.save(dish);
    }
}
