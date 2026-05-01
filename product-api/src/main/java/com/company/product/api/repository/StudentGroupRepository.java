package com.company.product.api.repository;

import com.company.product.api.entity.StudentGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    @EntityGraph(attributePaths = {"curator"})
    List<StudentGroup> findByCuratorId(Long curatorId);
}
