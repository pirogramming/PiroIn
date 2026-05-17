package com.example.Piroin.project.domain.assignment.service;

import com.example.Piroin.project.domain.assignment.dto.CreateAssignmentRequest;
import com.example.Piroin.project.domain.assignment.dto.CreateAssignmentResponse;
import com.example.Piroin.project.domain.assignment.entity.Assignment;
import com.example.Piroin.project.domain.assignment.entity.AssignmentItem;
import com.example.Piroin.project.domain.assignment.enums.AssignmentStatus;
import com.example.Piroin.project.domain.assignment.repository.AssignmentItemRepository;
import com.example.Piroin.project.domain.assignment.repository.AssignmentRepository;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentItemRepository assignmentItemRepository;
    private final UserRepository userRepository;

    public CreateAssignmentResponse createAssignment(CreateAssignmentRequest request) {

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .week(request.getWeek())
                .sessionDate(request.getSessionDate())
                .build();

        assignmentRepository.save(assignment);

        List<User> users = userRepository.findAll();

        List<AssignmentItem> assignmentItems = users.stream()
                .map(user -> AssignmentItem.builder()
                        .user(user)
                        .assignment(assignment)
                        .submitted(AssignmentStatus.PENDING)
                        .build())
                .toList();

        assignmentItemRepository.saveAll(assignmentItems);

        return new CreateAssignmentResponse(assignment.getId());
    }
}
