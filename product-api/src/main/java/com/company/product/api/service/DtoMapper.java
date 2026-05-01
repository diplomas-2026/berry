package com.company.product.api.service;

import com.company.product.api.dto.CommonDtos;
import com.company.product.api.entity.*;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    public CommonDtos.UserDto toUserDto(AppUser user) {
        return new CommonDtos.UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getAvatarPath()
        );
    }

    public CommonDtos.DishDto toDishDto(Dish dish) {
        return new CommonDtos.DishDto(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getProteinsPer100g(),
                dish.getFatsPer100g(),
                dish.getCarbsPer100g(),
                dish.getCaloriesPer100g(),
                dish.getPhotoPath()
        );
    }

    public CommonDtos.MenuItemDto toMenuItemDto(MenuItem item) {
        return new CommonDtos.MenuItemDto(item.getId(), item.getMenuDate(), item.getMealSlot(), toDishDto(item.getDish()));
    }

    public CommonDtos.VoucherDto toVoucherDto(MealVoucher voucher) {
        return new CommonDtos.VoucherDto(
                voucher.getId(),
                voucher.getStudent().getId(),
                voucher.getStudent().getFullName(),
                voucher.getIssueDate(),
                voucher.getMealSlot(),
                voucher.getStatus(),
                voucher.getRedeemedAt()
        );
    }

    public CommonDtos.GroupDto toGroupDto(StudentGroup group) {
        return new CommonDtos.GroupDto(
                group.getId(),
                group.getName(),
                group.getCurator() == null ? null : group.getCurator().getId(),
                group.getCurator() == null ? null : group.getCurator().getFullName()
        );
    }
}
