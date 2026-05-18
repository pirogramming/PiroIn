package com.example.Piroin.project.domain.assignment.service;

import com.example.Piroin.project.domain.assignment.dto.*;
import com.example.Piroin.project.domain.assignment.entity.Assignment;
import com.example.Piroin.project.domain.assignment.entity.AssignmentItem;
import com.example.Piroin.project.domain.assignment.entity.DeleteAssignmentResponse;
import com.example.Piroin.project.domain.assignment.enums.AssignmentStatus;
import com.example.Piroin.project.domain.assignment.exception.AssignmentException;
import com.example.Piroin.project.domain.assignment.exception.code.AssignmentErrorCode;
import com.example.Piroin.project.domain.assignment.repository.AssignmentItemRepository;
import com.example.Piroin.project.domain.assignment.repository.AssignmentRepository;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentItemRepository assignmentItemRepository;
    private final UserRepository userRepository;

    // 1. 과제 생성
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

    // 2. 과제 수정
    public ModifyAssignmentResponse modifyAssignment(Integer assignmentId,
                                                     ModifyAssignmentRequest request) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException(
                        AssignmentErrorCode.ASSIGNMENT_NOT_FOUND
                ));

        assignment.update(
                request.getTitle(),
                request.getWeek(),
                request.getSessionDate()
        );

        assignmentRepository.save(assignment);

        return new ModifyAssignmentResponse(assignment.getId());
    }

    // 3. 과제 삭제
    public DeleteAssignmentResponse deleteAssignment(Integer assignmentId) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() ->
                        new AssignmentException(
                                AssignmentErrorCode.ASSIGNMENT_NOT_FOUND
                        )
                );

        // assignment_item 먼저 삭제
        assignmentItemRepository.deleteAllByAssignmentId(assignmentId);

        // assignment 삭제
        assignmentRepository.delete(assignment);

        return new DeleteAssignmentResponse(assignmentId);
    }

    // 4-1. 나의 과제 조회 (부원)
    public GetMyAssignmentsResponse getMyAssignments(
            Long userId,
            String week
    ) {

        List<Assignment> assignments =
                assignmentRepository.findByWeekOrderBySessionDateAsc(week);

        List<AssignmentInfoResponse> responses =
                assignments.stream()
                        .map(assignment -> {

                            AssignmentStatus submittedStatus =
                                    assignmentItemRepository
                                            .findByUserIdAndAssignmentId(
                                                    userId,
                                                    assignment.getId()
                                            )
                                            .map(AssignmentItem::getSubmitted)
                                            .orElse(AssignmentStatus.PENDING);

                            return AssignmentInfoResponse.builder()
                                    .assignmentId(assignment.getId())
                                    .title(assignment.getTitle())
                                    .week(assignment.getWeek())
                                    .sessionDate(assignment.getSessionDate().toString())
                                    .day(convertDay(
                                            assignment.getSessionDate().getDayOfWeek()
                                    ))
                                    .submitted(submittedStatus)
                                    .build();
                        })
                        .toList();

        return GetMyAssignmentsResponse.builder()
                .week(week)
                .assignments(responses)
                .build();
    }

    // 4-2. 날짜를 요일로 전환 함수
    private String convertDay(DayOfWeek dayOfWeek) {

        return switch (dayOfWeek) {
            case MONDAY -> "MONDAY";
            case TUESDAY -> "TUESDAY";
            case WEDNESDAY -> "WEDNESDAY";
            case THURSDAY -> "THURSDAY";
            case FRIDAY -> "FRIDAY";
            case SATURDAY -> "SATURDAY";
            case SUNDAY -> "SUNDAY";
        };
    }

}
