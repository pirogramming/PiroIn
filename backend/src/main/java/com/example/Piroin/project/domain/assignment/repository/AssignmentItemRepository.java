package com.example.Piroin.project.domain.assignment.repository;

import com.example.Piroin.project.domain.assignment.entity.AssignmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentItemRepository extends JpaRepository<AssignmentItem, Integer> {

    void deleteAllByAssignmentId(Integer assignmentId);

    Optional<AssignmentItem> findByUserIdAndAssignmentId(Long userId, Integer assignmentId);

    List<AssignmentItem> findByUserIdAndAssignmentWeek(
            Long userId,
            String week
    );


}
