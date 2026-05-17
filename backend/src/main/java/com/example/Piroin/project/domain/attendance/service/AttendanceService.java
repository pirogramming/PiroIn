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
    @Transactional
    public AttendanceCode generateCodeAndCreateAttendances(String dateStr) { // [수정] 세션 ID 대신 날짜를 직접 받음

        // 1. [삭제] 더 이상 세션을 조회해서 날짜를 파싱할 필요가 없습니다. (curriculumRepository 조회 제거)

        // 2. 해당 날짜에 생성된 출석 코드 개수 조회
        long codeCountOfDay = attendanceCodeRepository.countByAttendanceDate(dateStr);

        if (codeCountOfDay >= 3) {
            throw new IllegalStateException("하루에 최대 3회까지만 출석 코드를 생성할 수 있습니다.");
        }

        // 3. 기존 활성화된 코드들 만료 처리
        List<AttendanceCode> activeCodes = attendanceCodeRepository.findByIsExpiredFalse();
        for (AttendanceCode activeCode : activeCodes) {
            activeCode.expire();
        }

        // 4. 4자리 랜덤 코드 생성 및 차수(Order) 계산
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
        String attendanceOrder = String.valueOf(codeCountOfDay + 1); // 1회차, 2회차, 3회차

        // 5. 새로운 AttendanceCode 생성 및 저장
        AttendanceCode attendanceCode = AttendanceCode.builder()
                .attendanceDate(dateStr) // [수정] 파라미터로 받은 날짜 주입
                .attendanceOrder(attendanceOrder)
                .code(code)
                .isExpired(false)
                .build();

        attendanceCodeRepository.save(attendanceCode);

        // 6. 모든 MEMBER 유저에 대해 '현재 생성된 출석 코드' 기준 초기 출석 데이터 생성
        List<User> users = userRepository.findByRole(Role.MEMBER);

        for (User user : users) {
            // [확인] 이미 완벽하게 studySession 대신 attendanceCode를 주입하도록 잘 짜두셨습니다!
            Attendance attendance = Attendance.builder()
                    .user(user)
                    .attendanceCode(attendanceCode)
                    .status(false)
                    .build();

            attendanceRepository.save(attendance);
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
    public AttendanceMarkResponse markAttendance(Long userId, String inputCode) {
        // 1. [수정] 오직 사용자가 입력한 코드를 기반으로 출석 코드 정보를 조회합니다.
        AttendanceCode code = attendanceCodeRepository
                .findByCode(inputCode)
                .orElse(null);

        // 입력한 출석 코드가 DB에 존재하지 않는 경우
        if (code == null) {
            return AttendanceMarkResponse.invalidCode();
        }

        // 출석 코드가 이미 만료된 경우
        if (Boolean.TRUE.equals(code.getIsExpired())) {
            return AttendanceMarkResponse.codeExpired();
        }

        // 2. [수정] 이제 Attendance도 studySessionId 대신 AttendanceCode와의 연관관계(예: attendanceCodeId)
        // 혹은 조회된 code의 날짜/차수 정보를 기반으로 기존 출석 기록을 찾아야 합니다.
        // (여기서는 이전 답변 시나리오 1인 'attendanceCodeId'로 매핑했다고 가정했을 때의 예시입니다.)
        Attendance attendance = attendanceRepository
                .findByUserIdAndAttendanceCodeId(userId, Long.valueOf(code.getId()))
                .orElse(null);

        // 해당 사용자와 출석 코드에 대한 출석 기록이 존재하지 않는 경우
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
        depositService.recalculateDeposit(userId); // 아직 생성 안 하신 부분 오류 패스!

        return AttendanceMarkResponse.success();
    }


    // 4. 출석 코드 만료시키기.
    @Transactional
    public String expireActiveAttendanceCode() {
        // 1. 활성화된 최신 출석 코드 조회
        AttendanceCode activeCode = attendanceCodeRepository
                .findFirstByIsExpiredFalseOrderByIdDesc()
                .orElseThrow(() -> new IllegalStateException("현재 활성화된 출석 코드가 없습니다."));

        // 2. 코드 만료 처리
        activeCode.expire();

        // 3. 변경된 구조: 만료된 '출석 코드의 ID'를 기반으로 결석자(status = false) 조회
        Integer attendanceCodeId = activeCode.getId();
        List<Attendance> absents =
                attendanceRepository.findByAttendanceCodeIdAndStatusFalse(attendanceCodeId);

        // 4. 결석자 대상 보증금 재계산 (User ID 타입 Integer 반영)
        for (Attendance attendance : absents) {
            depositService.recalculateDeposit(attendance.getUser().getId());
        }

        return "출석 코드가 성공적으로 만료되었습니다.";
    }


    // 5. 유저의 특정 날짜의 출석 현황을 조회하는 함수
    public List<AttendanceSlotRes> findByUserIdAndDate(Integer userId, LocalDate date) { // Long -> Integer
        // DB의 VARCHAR(255) 날짜 포맷과 맞추기 위해 String으로 변환 (예: "2026-05-17")
        String dateStr = date.toString();

        // 변경된 구조: User ID와 AttendanceCode의 날짜 조건으로 조회
        List<Attendance> attendances =
                attendanceRepository.findByUserIdAndDate(userId, dateStr);

        return attendances.stream()
                .map(attendance -> new AttendanceSlotRes(
                        attendance.getAttendanceCode().getId(),   // 세션 ID 대신 출석 코드 ID를 슬롯 식별값으로 사용
                        attendance.getStatus()
                ))
                .sorted(Comparator.comparing(AttendanceSlotRes::getAttendanceCodeId)) // 정렬 기준 변경
                .toList();
    }

    // 6. 유저의 전체 출석 현황을 날짜별로 묶어서 조회하는 함수
    public List<AttendanceStatusRes> findByUserId(Integer userId) { // Long -> Integer
        List<Attendance> attendances = attendanceRepository.findByUserId(Long.valueOf(userId));

        // 변경된 구조: AttendanceCode에 저장된 String 날짜를 기준으로 그룹화(groupingBy)
        Map<String, List<Attendance>> grouped = attendances.stream()
                .collect(Collectors.groupingBy(
                        attendance -> attendance.getAttendanceCode().getAttendanceDate()
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    // String으로 정렬/그룹화된 키를 다시 LocalDate 객체로 변환하여 DTO에 주입
                    LocalDate date = LocalDate.parse(entry.getKey());

                    List<AttendanceSlotRes> slots = entry.getValue().stream()
                            .map(attendance -> new AttendanceSlotRes(
                                    attendance.getAttendanceCode().getId(),
                                    attendance.getStatus()
                            ))
                            .sorted(Comparator.comparing(AttendanceSlotRes::getAttendanceCodeId))
                            .toList();

                    AttendanceStatusRes dto = new AttendanceStatusRes();
                    dto.setDate(date);
                    dto.setSlots(slots);

                    return dto;
                })
                .sorted(Comparator.comparing(AttendanceStatusRes::getDate).reversed()) // 최신날짜 순 정렬
                .toList();
    }

    // 6. 유저 상태 변경 (관리자)
    // 컨트롤러 부분은 출석만 받는데 여기는 출석&과제 둘 다 받아서 추후에 수정 예정
    @Transactional
    public boolean updateUserStatus(Integer userId, UpdateUserStatusReq req) {
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
            AssignmentItem assignmentItem = assignmentItemRepository.findById(Math.toIntExact(req.getAssignmentItemId()))
                    .orElseThrow(() -> new IllegalArgumentException("과제 기록을 찾을 수 없습니다."));

            if (!assignmentItem.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("요청된 사용자와 과제 기록의 사용자가 일치하지 않습니다.");
            }

            assignmentItem.updateSubmitted(req.getAssignmentStatus());
            updated = true;
        }

        // 출석 변경 → 보증금 재계산 (과제 변경도 포함이 되어 있나..?)
        if (updated) {
            depositService.recalculateDeposit(Long.valueOf(userId));
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

