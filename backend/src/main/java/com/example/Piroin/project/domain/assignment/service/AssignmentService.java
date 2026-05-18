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
import com.example.Piroin.project.domain.attendance.entity.Attendance;
import com.example.Piroin.project.domain.attendance.entity.AttendanceCode;
import com.example.Piroin.project.domain.attendance.repository.AttendanceCodeRepository;
import com.example.Piroin.project.domain.attendance.repository.AttendanceRepository;
import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.repository.CurriculumRepository;
import com.example.Piroin.project.domain.curriculum.service.CurriculumService;
import com.example.Piroin.project.domain.user.dto.*;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.enums.Role;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import com.example.Piroin.project.domain.user.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentItemRepository assignmentItemRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final CurriculumRepository curriculumRepository;
    private final AttendanceCodeRepository attendanceCodeRepository;

    // 1. 과제 생성
    @Transactional
    public CreateAssignmentResponse createAssignment(
            CreateAssignmentRequest request
    ) {

        // week 문자열 -> Long 변환
        Long week = Long.valueOf(request.getWeek());

        // 해당 주차 세션들 조회
        List<StudySession> sessions =
                curriculumRepository.findByWeek(week);

        // 요청한 요일과 일치하는 세션 찾기
        StudySession matchedSession = sessions.stream()
                .filter(session ->
                        session.getSessionDate().getDayOfWeek()
                                == request.getDay()
                )
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("해당 요일의 세션이 존재하지 않습니다."));

        // 실제 날짜 추출
        LocalDate sessionDate = matchedSession.getSessionDate();

        // Assignment 생성
        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .week(request.getWeek())
                .sessionDate(sessionDate)
                .build();

        assignmentRepository.save(assignment);

        // ADMIN 제외 MEMBER만 조회 추천
        List<User> users =
                userRepository.findByRole(Role.MEMBER);

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


    @Transactional(readOnly = true)
    public StudentWeeklyStatusResponse getStudentWeeklyStatus(
            Long userId,
            Long week
    ) {

        List<StudySession> sessions =
                curriculumRepository.findByWeek(week);

        List<DayStatusResponse> dayResponses = new ArrayList<>();

        for (StudySession session : sessions) {

            LocalDate sessionDate = session.getSessionDate();

            String day =
                    sessionDate.getDayOfWeek().toString();

            /*
             * 과제 조회
             */
            List<Assignment> assignments =
                    assignmentRepository.findBySessionDate(sessionDate);

            List<AssignmentStatusResponse> assignmentResponses =
                    assignments.stream()
                            .map(assignment -> {

                                AssignmentItem item =
                                        assignmentItemRepository
                                                .findByUserIdAndAssignmentId(
                                                        userId,
                                                        assignment.getId()
                                                )
                                                .orElse(null);

                                String submitted =
                                        item == null
                                                ? "PENDING"
                                                : item.getSubmitted().name();

                                return AssignmentStatusResponse.builder()
                                        .assignmentId(assignment.getId())
                                        .title(assignment.getTitle())
                                        .submitted(submitted)
                                        .build();
                            })
                            .toList();

            /*
             * 출석 조회
             */
            List<AttendanceCode> attendanceCodes =
                    attendanceCodeRepository.findByAttendanceDate(sessionDate);

            List<AttendanceStatusResponse> attendanceResponses =
                    attendanceCodes.stream()
                            .map(code -> {

                                Attendance attendance =
                                        attendanceRepository
                                                .findByUserIdAndAttendanceCodeId(
                                                        userId,
                                                        code.getId()
                                                )
                                                .orElse(null);

                                boolean attended =
                                        attendance != null &&
                                                attendance.getStatus();

                                return AttendanceStatusResponse.builder()
                                        .attendanceCodeId(code.getId())
                                        .attendanceOrder(code.getAttendanceOrder())
                                        .attended(attended)
                                        .build();
                            })
                            .toList();

            dayResponses.add(
                    DayStatusResponse.builder()
                            .day(day)
                            .sessionDate(sessionDate)
                            .assignments(assignmentResponses)
                            .attendances(attendanceResponses)
                            .build()
            );
        }

        return StudentWeeklyStatusResponse.builder()
                .week(week)
                .days(dayResponses)
                .build();
    }
}
