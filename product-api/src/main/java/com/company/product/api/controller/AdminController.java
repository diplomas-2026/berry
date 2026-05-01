package com.company.product.api.controller;

import com.company.product.api.dto.RequestDtos;
import com.company.product.api.entity.AppUser;
import com.company.product.api.entity.GroupMember;
import com.company.product.api.entity.StudentGroup;
import com.company.product.api.entity.UserRole;
import com.company.product.api.repository.AppUserRepository;
import com.company.product.api.repository.GroupMemberRepository;
import com.company.product.api.repository.StudentGroupRepository;
import com.company.product.api.service.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AppUserRepository userRepository;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper mapper;

    public AdminController(AppUserRepository userRepository, StudentGroupRepository groupRepository, GroupMemberRepository groupMemberRepository, PasswordEncoder passwordEncoder, DtoMapper mapper) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @GetMapping("/users")
    public List<?> users() {
        return userRepository.findAll().stream().map(mapper::toUserDto).toList();
    }

    @PostMapping("/users")
    public Object createUser(@Valid @RequestBody RequestDtos.CreateUserRequest request) {
        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setActive(true);
        return mapper.toUserDto(userRepository.save(user));
    }

    @PatchMapping("/users/{id}")
    public Object updateUser(@PathVariable Long id, @Valid @RequestBody RequestDtos.UpdateUserRequest request) {
        AppUser user = userRepository.findById(id).orElseThrow();
        user.setActive(request.active());
        return mapper.toUserDto(userRepository.save(user));
    }

    @PostMapping("/groups")
    public StudentGroup createGroup(@Valid @RequestBody RequestDtos.CreateGroupRequest request) {
        StudentGroup group = new StudentGroup();
        group.setName(request.name());
        if (request.curatorId() != null) {
            AppUser curator = userRepository.findById(request.curatorId()).orElseThrow();
            if (curator.getRole() != UserRole.CURATOR) {
                throw new IllegalArgumentException("Пользователь не является куратором");
            }
            group.setCurator(curator);
        }
        return groupRepository.save(group);
    }

    @PostMapping("/groups/{groupId}/students")
    public Object addStudent(@PathVariable Long groupId, @Valid @RequestBody RequestDtos.AssignStudentRequest request) {
        StudentGroup group = groupRepository.findById(groupId).orElseThrow();
        AppUser student = userRepository.findById(request.studentId()).orElseThrow();
        if (student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Пользователь не является студентом");
        }
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setStudent(student);
        return groupMemberRepository.save(member);
    }
}
