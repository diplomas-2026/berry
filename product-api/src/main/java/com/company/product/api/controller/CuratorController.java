package com.company.product.api.controller;

import com.company.product.api.dto.RequestDtos;
import com.company.product.api.entity.*;
import com.company.product.api.repository.AppUserRepository;
import com.company.product.api.repository.GroupMemberRepository;
import com.company.product.api.repository.MealVoucherRepository;
import com.company.product.api.repository.StudentGroupRepository;
import com.company.product.api.service.CurrentUserService;
import com.company.product.api.service.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/curator")
@PreAuthorize("hasRole('CURATOR')")
public class CuratorController {
    private final CurrentUserService currentUserService;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final AppUserRepository userRepository;
    private final MealVoucherRepository voucherRepository;
    private final DtoMapper mapper;

    public CuratorController(CurrentUserService currentUserService, StudentGroupRepository groupRepository, GroupMemberRepository groupMemberRepository, AppUserRepository userRepository, MealVoucherRepository voucherRepository, DtoMapper mapper) {
        this.currentUserService = currentUserService;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.voucherRepository = voucherRepository;
        this.mapper = mapper;
    }

    @GetMapping("/groups")
    public List<?> groups() {
        Long curatorId = currentUserService.requireUser().getId();
        return groupRepository.findByCuratorId(curatorId).stream().map(mapper::toGroupDto).toList();
    }

    @GetMapping("/groups/{groupId}/students")
    public List<?> groupStudents(@PathVariable Long groupId) {
        Long curatorId = currentUserService.requireUser().getId();
        if (groupRepository.findByCuratorId(curatorId).stream().noneMatch(group -> group.getId().equals(groupId))) {
            throw new IllegalArgumentException("Группа не относится к куратору");
        }
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(GroupMember::getStudent)
                .map(mapper::toUserDto)
                .toList();
    }

    @GetMapping("/students")
    public List<?> students() {
        Long curatorId = currentUserService.requireUser().getId();
        return groupMemberRepository.findByGroupCuratorId(curatorId).stream()
                .map(GroupMember::getStudent)
                .map(mapper::toUserDto)
                .toList();
    }

    @GetMapping("/students/{studentId}/vouchers")
    public List<?> studentVouchers(@PathVariable Long studentId) {
        Long curatorId = currentUserService.requireUser().getId();
        if (!groupMemberRepository.existsByGroupCuratorIdAndStudentId(curatorId, studentId)) {
            throw new IllegalArgumentException("Студент не относится к группе куратора");
        }
        return voucherRepository.findByStudentIdOrderByIssueDateDesc(studentId).stream().map(mapper::toVoucherDto).toList();
    }

    @DeleteMapping("/vouchers/{voucherId}")
    public void deleteVoucher(@PathVariable Long voucherId) {
        Long curatorId = currentUserService.requireUser().getId();
        MealVoucher voucher = voucherRepository.findById(voucherId).orElseThrow();
        if (!groupMemberRepository.existsByGroupCuratorIdAndStudentId(curatorId, voucher.getStudent().getId())) {
            throw new IllegalArgumentException("Студент не относится к группе куратора");
        }
        if (!voucher.getIssueDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Можно удалить только будущий талон");
        }
        voucherRepository.delete(voucher);
    }

    @PostMapping("/vouchers/issue")
    public List<?> issue(@Valid @RequestBody RequestDtos.IssueVoucherRequest request) {
        Long curatorId = currentUserService.requireUser().getId();
        if (!groupMemberRepository.existsByGroupCuratorIdAndStudentId(curatorId, request.studentId())) {
            throw new IllegalArgumentException("Студент не относится к группе куратора");
        }

        AppUser student = userRepository.findById(request.studentId()).orElseThrow();
        AppUser curator = currentUserService.requireUser();

        for (MealSlot slot : request.slots()) {
            voucherRepository.findByStudentIdAndIssueDateAndMealSlot(student.getId(), request.date(), slot).ifPresentOrElse(
                    existing -> {},
                    () -> {
                        MealVoucher voucher = new MealVoucher();
                        voucher.setStudent(student);
                        voucher.setIssueDate(request.date());
                        voucher.setMealSlot(slot);
                        voucher.setStatus(VoucherStatus.ISSUED);
                        voucher.setIssuedByCurator(curator);
                        voucherRepository.save(voucher);
                    }
            );
        }
        return voucherRepository.findByStudentIdOrderByIssueDateDesc(student.getId()).stream().limit(10).map(mapper::toVoucherDto).toList();
    }
}
