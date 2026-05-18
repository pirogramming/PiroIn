package com.example.Piroin.project.domain.user.service;

import com.example.Piroin.project.domain.assignment.entity.Assignment;
import com.example.Piroin.project.domain.assignment.entity.AssignmentItem;
import com.example.Piroin.project.domain.assignment.repository.AssignmentItemRepository;
import com.example.Piroin.project.domain.assignment.repository.AssignmentRepository;
import com.example.Piroin.project.domain.attendance.entity.Attendance;
import com.example.Piroin.project.domain.attendance.entity.AttendanceCode;
import com.example.Piroin.project.domain.attendance.repository.AttendanceCodeRepository;
import com.example.Piroin.project.domain.attendance.repository.AttendanceRepository;
import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.repository.CurriculumRepository;
import com.example.Piroin.project.domain.user.dto.*;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.enums.Role;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AssignmentItemRepository assignmentItemRepository;
    private final AttendanceRepository attendanceRepository;
    private final CurriculumRepository curriculumRepository;
    private final AssignmentRepository assignmentRepository;
    private final AttendanceCodeRepository attendanceCodeRepository;

    // 전체 부원 목록 조회
    public List<StudentListResponse> getStudentList() {

        List<User> students = userRepository.findByRole(Role.MEMBER);

        return students.stream()
                .map(user -> new StudentListResponse(
                        user.getId(),
                        user.getName()
                ))
                .toList();
    }

    // 부원 이름 검색
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

//    // 5. 부원 출석/과제 상태 조회
//    @Transactional(readOnly = true)
//    public StudentWeeklyStatusResponse getStudentWeeklyStatus(
//            Long userId,
//            Long week
//    ) {
//
//        List<StudySession> sessions =
//                curriculumRepository.findByWeek(week);
//
//        List<DayStatusResponse> dayResponses = new ArrayList<>();
//
//        for (StudySession session : sessions) {
//
//            LocalDate sessionDate = session.getSessionDate();
//
//            String day =
//                    sessionDate.getDayOfWeek().toString();
//
//            /*
//             * 과제 조회
//             */
//            List<Assignment> assignments =
//                    assignmentRepository.findBySessionDate(sessionDate);
//
//            List<AssignmentStatusResponse> assignmentResponses =
//                    assignments.stream()
//                            .map(assignment -> {
//
//                                AssignmentItem item =
//                                        assignmentItemRepository
//                                                .findByUserIdAndAssignmentId(
//                                                        userId,
//                                                        assignment.getId()
//                                                )
//                                                .orElse(null);
//
//                                String submitted =
//                                        item == null
//                                                ? "PENDING"
//                                                : item.getSubmitted().name();
//
//                                return AssignmentStatusResponse.builder()
//                                        .assignmentId(assignment.getId())
//                                        .title(assignment.getTitle())
//                                        .submitted(submitted)
//                                        .build();
//                            })
//                            .toList();
//
//            /*
//             * 출석 조회
//             */
//            List<AttendanceCode> attendanceCodes =
//                    attendanceCodeRepository.findByAttendanceDate(sessionDate);
//
//            List<AttendanceStatusResponse> attendanceResponses =
//                    attendanceCodes.stream()
//                            .map(code -> {
//
//                                Attendance attendance =
//                                        attendanceRepository
//                                                .findByUserIdAndAttendanceCodeId(
//                                                        userId,
//                                                        code.getId()
//                                                )
//                                                .orElse(null);
//
//                                boolean attended =
//                                        attendance != null &&
//                                                attendance.getStatus();
//
//                                return AttendanceStatusResponse.builder()
//                                        .attendanceCodeId(code.getId())
//                                        .attendanceOrder(code.getAttendanceOrder())
//                                        .attended(attended)
//                                        .build();
//                            })
//                            .toList();
//
//            dayResponses.add(
//                    DayStatusResponse.builder()
//                            .day(day)
//                            .sessionDate(sessionDate)
//                            .assignments(assignmentResponses)
//                            .attendances(attendanceResponses)
//                            .build()
//            );
//        }
//
//        return StudentWeeklyStatusResponse.builder()
//                .week(week)
//                .days(dayResponses)
//                .build();
//    }
}