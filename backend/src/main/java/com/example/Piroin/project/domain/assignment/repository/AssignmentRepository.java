package com.example.Piroin.project.domain.assignment.repository;

import com.example.Piroin.project.domain.assignment.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
}
