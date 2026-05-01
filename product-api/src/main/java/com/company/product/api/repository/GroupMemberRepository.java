package com.company.product.api.repository;

import com.company.product.api.entity.AppUser;
import com.company.product.api.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupCuratorId(Long curatorId);
    Optional<GroupMember> findByStudentId(Long studentId);
    boolean existsByGroupCuratorIdAndStudentId(Long curatorId, Long studentId);
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByStudentIn(List<AppUser> students);
}
