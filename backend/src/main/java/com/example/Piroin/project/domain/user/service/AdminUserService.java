package com.example.Piroin.project.domain.user.service;

import com.example.Piroin.project.domain.assignment.entity.Assignment;
import com.example.Piroin.project.domain.assignment.entity.AssignmentItem;
import com.example.Piroin.project.domain.assignment.enums.AssignmentStatus;
import com.example.Piroin.project.domain.assignment.repository.AssignmentItemRepository;
import com.example.Piroin.project.domain.assignment.repository.AssignmentRepository;
import com.example.Piroin.project.domain.attendance.entity.Attendance;
import com.example.Piroin.project.domain.attendance.repository.AttendanceCodeRepository;
import com.example.Piroin.project.domain.attendance.repository.AttendanceRepository;
import com.example.Piroin.project.domain.curriculum.repository.CurriculumRepository;
import com.example.Piroin.project.domain.deposit.entity.Deposit;
import com.example.Piroin.project.domain.deposit.repository.DepositRepository;
import com.example.Piroin.project.domain.user.dto.*;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.enums.Role;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.Piroin.project.domain.user.dto.UpdateStudentStatusRequest;
import com.example.Piroin.project.domain.user.dto.UpdateStudentStatusResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AssignmentItemRepository assignmentItemRepository;
    private final AttendanceRepository attendanceRepository;
    private final DepositRepository depositRepository;
    private final CurriculumRepository curriculumRepository;
    private final AssignmentRepository assignmentRepository;
    private final AttendanceCodeRepository attendanceCodeRepository;

    // 1. 전체 부원 목록 조회
    public List<StudentListResponse> getStudentList() {

        List<User> students = userRepository.findByRole(Role.MEMBER);

        return students.stream()
                .map(user -> new StudentListResponse(
                        user.getId(),
                        user.getName()
                ))
                .toList();
    }

    // 2. 부원 이름 검색
    public List<StudentListResponse> searchStudents(String name) {

        List<User> students =
                userRepository.findByRoleAndNameContaining(Role.MEMBER, name);

        return students.stream()
                .map(user -> new StudentListResponse(
                        user.getId(),
                        user.getName()
                ))
                .toList();
    }

    // 3. 특정 부원의 과제/출석 상태 수정 (운영진)
    @Transactional
    public UpdateStudentStatusResponse updateStudentWeekStatus(
            Long userId,
            Long week,
            UpdateStudentStatusRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        if (request.getAssignments() != null) {
            for (UpdateStudentStatusRequest.AssignmentStatusRequest dto
                    : request.getAssignments()) {

                // 해당 assignmentItem 존재하지 않을 때
                AssignmentItem assignmentItem =
                        assignmentItemRepository.findById(dto.getAssignmentItemId())
                                .orElseThrow(() ->
                                        new RuntimeException("과제 정보가 존재하지 않습니다.")
                                );

                // assignmentItem가 userId의 과제가 아닐 경우
                if (!assignmentItem.getUser().getId().equals(userId)) {
                    throw new RuntimeException("해당 유저의 과제가 아닙니다.");
                }

                if (dto.getSubmitted() != null) {
                    assignmentItem.updateSubmitted(dto.getSubmitted());
                }
            }
        }

        if (request.getAttendances() != null) {
            for (UpdateStudentStatusRequest.AttendanceStatusRequest dto
                    : request.getAttendances()) {

                Attendance attendance =
                        attendanceRepository.findById(dto.getAttendanceId())
                                .orElseThrow(() ->
                                        new RuntimeException("출석 정보가 존재하지 않습니다.")
                                );

                // assignmentId가 userId의 것이 아닐 때
                if (!attendance.getUser().getId().equals(userId)) {
                    throw new RuntimeException("해당 유저의 출석이 아닙니다.");
                }

                if (dto.getStatus() != null) {
                    attendance.updateStatus(dto.getStatus());
                }
            }
        }

        recalculateDeposit(userId);

        return new UpdateStudentStatusResponse(
                userId,
                week,
                "부원의 과제/출석 상태가 수정되었습니다."
        );
    }


    // 4. 보증금 재계산 메소드(출석 & 과제 공통)
    private void recalculateDeposit(Long userId) {

        Deposit deposit = depositRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("보증금 정보가 존재하지 않습니다."));

        List<AssignmentItem> assignmentItems =
                assignmentItemRepository.findByUserId(userId);

        int assignmentPenalty = assignmentItems.stream()
                .mapToInt(item -> calculateAssignmentPenalty(item.getSubmitted()))
                .sum();

        List<Attendance> attendances =
                attendanceRepository.findByUserId(userId);

        int attendancePenalty = attendances.stream()
                .mapToInt(attendance -> attendance.getStatus() ? 0 : 10_000)
                .sum();

        deposit.updateDepositAmount(
                assignmentPenalty,
                attendancePenalty
        );
    }

    // 5. 과제에 대한 보증금 계산 로직
    private int calculateAssignmentPenalty(AssignmentStatus status) {

        return switch (status) {

            case SUCCESS, PENDING -> 0;

            case INSUFFICIENT_MINOR -> 10_000;

            case INSUFFICIENT_MAJOR -> 20_000;

            case FAILURE -> 30_000;
        };
    }

    // 6. 출석에 대한 보증금 계산 로직
    // (AttendanceService에 있음!!)

}