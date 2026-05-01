package com.company.product.api.controller;

import com.company.product.api.entity.AppUser;
import com.company.product.api.repository.AppUserRepository;
import com.company.product.api.service.CurrentUserService;
import com.company.product.api.service.DtoMapper;
import com.company.product.api.service.StorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final CurrentUserService currentUserService;
    private final StorageService storageService;
    private final AppUserRepository userRepository;
    private final DtoMapper mapper;

    public ProfileController(CurrentUserService currentUserService, StorageService storageService, AppUserRepository userRepository, DtoMapper mapper) {
        this.currentUserService = currentUserService;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadAvatar(@RequestPart("file") MultipartFile file) {
        AppUser user = currentUserService.requireUser();
        user.setAvatarPath(storageService.store(file, "avatars"));
        userRepository.save(user);
        return mapper.toUserDto(user);
    }

    @DeleteMapping("/avatar")
    public Object deleteAvatar() {
        AppUser user = currentUserService.requireUser();
        user.setAvatarPath(null);
        userRepository.save(user);
        return mapper.toUserDto(user);
    }
}
