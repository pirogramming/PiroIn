package com.example.Piroin.project.domain.assignment.repository;

import com.example.Piroin.project.domain.assignment.entity.AssignmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentItemRepository extends JpaRepository<AssignmentItem, Integer> {
}
