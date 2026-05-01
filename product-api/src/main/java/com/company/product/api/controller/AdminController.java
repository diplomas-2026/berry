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
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setMiddleName(request.middleName());
        user.setRole(request.role());
        user.setActive(true);
        return mapper.toUserDto(userRepository.save(user));
    }

    @PatchMapping("/users/{id}")
    public Object updateUser(@PathVariable Long id, @Valid @RequestBody RequestDtos.UpdateUserRequest request) {
        AppUser user = userRepository.findById(id).orElseThrow();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setMiddleName(request.middleName());
        user.setActive(request.active());
        user.setRole(request.role());
        return mapper.toUserDto(userRepository.save(user));
    }

    @GetMapping("/groups")
    public List<?> groups() {
        return groupRepository.findAll().stream().map(mapper::toGroupDto).toList();
    }

    @PostMapping("/groups")
    public Object createGroup(@Valid @RequestBody RequestDtos.CreateGroupRequest request) {
        StudentGroup group = new StudentGroup();
        group.setName(request.name());
        if (request.curatorId() != null) {
            AppUser curator = userRepository.findById(request.curatorId()).orElseThrow();
            if (curator.getRole() != UserRole.CURATOR) {
                throw new IllegalArgumentException("Пользователь не является куратором");
            }
            group.setCurator(curator);
        }
        return mapper.toGroupDto(groupRepository.save(group));
    }

    @PatchMapping("/groups/{id}")
    public Object updateGroup(@PathVariable Long id, @Valid @RequestBody RequestDtos.UpdateGroupRequest request) {
        StudentGroup group = groupRepository.findById(id).orElseThrow();
        group.setName(request.name());
        if (request.curatorId() != null) {
            AppUser curator = userRepository.findById(request.curatorId()).orElseThrow();
            if (curator.getRole() != UserRole.CURATOR) {
                throw new IllegalArgumentException("Пользователь не является куратором");
            }
            group.setCurator(curator);
        } else {
            group.setCurator(null);
        }
        return mapper.toGroupDto(groupRepository.save(group));
    }

    @DeleteMapping("/groups/{id}")
    public void deleteGroup(@PathVariable Long id) {
        groupRepository.deleteById(id);
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
