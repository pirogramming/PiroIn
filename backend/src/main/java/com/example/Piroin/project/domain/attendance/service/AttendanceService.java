package com.example.Piroin.project.domain.attendance.service;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.repository.CurriculumRepository;
import com.example.Piroin.project.domain.deposit.entity.Deposit;
import com.example.Piroin.project.domain.deposit.repository.DepositRepository;
import com.example.Piroin.project.domain.deposit.service.DepositService;
import com.example.Piroin.project.domain.user.enums.Role;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import com.example.Piroin.project.domain.attendance.dto.AttendanceMarkResponse;
import com.example.Piroin.project.domain.attendance.dto.AttendanceSlotRes;
import com.example.Piroin.project.domain.attendance.dto.AttendanceStatusRes;
import com.example.Piroin.project.domain.attendance.dto.UserAttendanceStatusRes;
import com.example.Piroin.project.domain.attendance.entity.Attendance;
import com.example.Piroin.project.domain.attendance.entity.AttendanceCode;
import com.example.Piroin.project.domain.attendance.repository.AttendanceCodeRepository;
import com.example.Piroin.project.domain.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.Piroin.project.domain.assignment.entity.AssignmentItem;
import com.example.Piroin.project.domain.assignment.repository.AssignmentItemRepository;
import com.example.Piroin.project.domain.attendance.dto.UpdateUserStatusReq;


import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceCodeRepository attendanceCodeRepository;
    private final UserRepository userRepository;
    private final DepositService depositService;

    private final CurriculumRepository curriculumRepository;

    private final AssignmentItemRepository assignmentItemRepository;



    // 1. 출석 시작 코드 (출석코드 생성 함수)
    // 출석 시작은 이제 date/order가 아니라 studySessionId를 받아야 함.
    @Transactional
    public AttendanceCode generateCodeAndCreateAttendances(Long studySessionId) {
        StudySession studySession = curriculumRepository.findById(studySessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다."));

        LocalDate sessionDate = studySession.getDate();

        int codeCountOfDay = attendanceCodeRepository.countByStudySessionDate(sessionDate);

        if (codeCountOfDay >= 3) {
            throw new IllegalStateException("하루에 최대 3회까지만 출석 코드를 생성할 수 있습니다.");
        }

        List<AttendanceCode> activeCodes = attendanceCodeRepository.findByIsExpiredFalse();

        for (AttendanceCode activeCode : activeCodes) {
            activeCode.expire();
        }

        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));

        AttendanceCode attendanceCode = AttendanceCode.builder()
                .studySession(studySession)
                .code(code)
                .isExpired(false)
                .build();

        attendanceCodeRepository.save(attendanceCode);

        List<User> users = userRepository.findByRole(Role.MEMBER);

        for (User user : users) {
            if (!attendanceRepository.existsByUserIdAndStudySessionId(user.getId(), studySessionId)) {
                Attendance attendance = Attendance.builder()
                        .user(user)
                        .studySession(studySession)
                        .status(false)
                        .build();

                attendanceRepository.save(attendance);
            }
        }

        return attendanceCode;
    }

    // 2. 현재 활성화된 출석코드 조회 함수
    public Optional<AttendanceCode> getActiveAttendanceCode() {
        // 기존: List로 받아서 0번째 꺼내기 (비어있을 시 위험)
        // 수정: 레파지토리의 findFirst 기능을 사용하여 가장 최신 활성 코드 하나만 안전하게 조회
        return attendanceCodeRepository.findFirstByIsExpiredFalseOrderByIdDesc();
    }

    // 3. 출석 체크
    @Transactional
    public AttendanceMarkResponse markAttendance(Long userId, Long studySessionId, String inputCode) {
        // 사용자가 입력한 코드가 이 세션의 코드가 맞는지 확인
        AttendanceCode code = attendanceCodeRepository
                .findByCodeAndStudySessionId(inputCode, studySessionId)
                .orElse(null);

        // 입력한 출석 코드가 해당 세션의 출석 코드와 일치하지 않는 경우
        if (code == null) {
            return AttendanceMarkResponse.invalidCode();
        }

        // 출석 코드가 이미 만료된 경우
        if (Boolean.TRUE.equals(code.getIsExpired())) {
            return AttendanceMarkResponse.codeExpired();
        }

        Attendance attendance = attendanceRepository
                .findByUserIdAndStudySessionId(userId, studySessionId)
                .orElse(null);

        // 해당 사용자와 세션에 대한 출석 기록이 존재하지 않는 경우
        if (attendance == null) {
            return AttendanceMarkResponse.error("출석 정보를 찾을 수 없습니다.");
        }

        // 사용자가 이미 출석 체크를 완료한 경우
        if (Boolean.TRUE.equals(attendance.getStatus())) {
            return AttendanceMarkResponse.alreadyMarked();
        }
        // 출석 상태를 출석 완료(true)로 변경
        attendance.updateStatus(true);

        // 출석 상태 변경 후 보증금 재계산
        depositService.recalculateDeposit(userId); // 아직 recalculateDeposit 부분 생성 안 해서 오류 나는 게 정상.

        return AttendanceMarkResponse.success();
    }


    // 4. 출석 코드 만료시키기.
    @Transactional
    public String expireActiveAttendanceCode() {
        AttendanceCode activeCode = attendanceCodeRepository
                .findFirstByIsExpiredFalseOrderByIdDesc()
                .orElseThrow(() -> new IllegalStateException("현재 활성화된 출석 코드가 없습니다."));

        activeCode.expire();

        Long studySessionId = activeCode.getStudySession().getId();

        List<Attendance> absents =
                attendanceRepository.findByStudySessionIdAndStatusFalse(studySessionId);

        for (Attendance attendance : absents) {
            depositService.recalculateDeposit(attendance.getUser().getId());
        }

        return "출석 코드가 성공적으로 만료되었습니다.";
    }


    // 5. 유저의 특정 날짜의 출석 현황을 조회하는 함수
    public List<AttendanceSlotRes> findByUserIdAndDate(Long userId, LocalDate date) {

        List<Attendance> attendances =
                attendanceRepository.findByUserIdAndStudySessionSessionDate(userId, date);

        return attendances.stream()
                .map(attendance -> new AttendanceSlotRes(
                        attendance.getStudySession().getId(),   // 임시로 세션 ID를 슬롯 식별값으로 사용
                        attendance.getStatus()                  // Boolean getter는 isStatus()가 아니라 getStatus()
                ))
                .sorted(Comparator.comparing(AttendanceSlotRes::getStudySessionId))
                .toList();
    }

    public List<AttendanceStatusRes> findByUserId(Long userId) {
        List<Attendance> attendances = attendanceRepository.findByUserId(userId);

        Map<LocalDate, List<Attendance>> grouped = attendances.stream()
                .collect(Collectors.groupingBy(
                        attendance -> attendance.getStudySession().getSessionDate()
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();

                    List<AttendanceSlotRes> slots = entry.getValue().stream()
                            .map(attendance -> new AttendanceSlotRes(
                                    attendance.getStudySession().getId(),
                                    attendance.getStatus()
                            ))
                            .sorted(Comparator.comparing(AttendanceSlotRes::getStudySessionId))
                            .toList();

                    AttendanceStatusRes dto = new AttendanceStatusRes();
                    dto.setDate(date);
                    dto.setSlots(slots);

                    return dto;
                })
                .sorted(Comparator.comparing(AttendanceStatusRes::getDate).reversed())
                .toList();
    }

    // 6. 유저 상태 변경 (관리자)
    // 컨트롤러 부분은 출석만 받는데 여기는 출석&과제 둘 다 받아서 추후에 수정 예정
    @Transactional
    public boolean updateUserStatus(Long userId, UpdateUserStatusReq req) {
        boolean updated = false;

        // 출석 상태 변경 코드
        if (req.getAttendanceId() != null && req.getAttendanceStatus() != null) {
            Attendance attendance = attendanceRepository.findById(req.getAttendanceId())
                    .orElseThrow(() -> new IllegalArgumentException("출석 기록을 찾을 수 없습니다."));

            if (!attendance.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("요청된 사용자와 출석 기록의 사용자가 일치하지 않습니다.");
            }

            attendance.updateStatus(req.getAttendanceStatus());
            updated = true;
        }

        // 과제 상태 변경 코드
        if (req.getAssignmentItemId() != null && req.getAssignmentStatus() != null) {
            AssignmentItem assignmentItem = assignmentItemRepository.findById(req.getAssignmentItemId())
                    .orElseThrow(() -> new IllegalArgumentException("과제 기록을 찾을 수 없습니다."));

            if (!assignmentItem.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("요청된 사용자와 과제 기록의 사용자가 일치하지 않습니다.");
            }

            assignmentItem.updateSubmitted(req.getAssignmentStatus());
            updated = true;
        }

        // 출석 변경 → 보증금 재계산 (과제 변경도 포함이 되어 있나..?)
        if (updated) {
            depositService.recalculateDeposit(userId);
        }

        return updated;
    }


}

/*
    // 관리자가 유저의 출석 상태를 변경하는 함수(나중에 과제까지 같이 변경되도록 수정할 것)
    @Transactional
    public boolean updateAttendanceStatus(Long attendanceId, boolean status) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);

        if (attendanceOpt.isEmpty()) {
            return false;
        }

        // 출석 상태 변경
        Attendance attendance = attendanceOpt.get();
        attendance.setStatus(status);
        attendanceRepository.save(attendance);

        // 출석 변경 → 보증금 재계산
        depositService.recalculateDeposit(attendance.getUser().getId());

        return true;
    }

 */

