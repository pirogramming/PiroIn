package com.example.Piroin.project.domain.attendance.service;

import com.example.Piroin.project.domain.assignment.repository.AssignmentRepository;
import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.exception.CurriculumException;
import com.example.Piroin.project.domain.curriculum.exception.code.CurriculumErrorCode;
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
import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
import com.example.Piroin.project.domain.attendance.dto.AttendanceDayStatusRes;


import java.time.LocalDate;
import java.util.*;
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



    // 1. 출석 시작 코드 (출석코드 생성 함수)
    @Transactional
    public AttendanceCode generateCodeAndCreateAttendances(LocalDate date) { // [수정] 세션 ID 대신 날짜를 직접 받음

        // 1-1) 해당 날짜에 커리큘럼이 있는지 확인
        if (!curriculumRepository.existsBySessionDate(date)) {
            throw new CurriculumException(
                    CurriculumErrorCode.ATTENDANCE_DATE_NOT_AVAILABLE
            );
        }

        // 1-2) 해당 날짜에 생성된 출석 코드 개수 조회.
        long codeCountOfDay = attendanceCodeRepository.countByAttendanceDate(date);

        if (codeCountOfDay >= 3) {
            throw new IllegalStateException("하루에 최대 3회까지만 출석 코드를 생성할 수 있습니다.");
        }

        // 1-3) 기존 활성화된 코드들 만료 처리
        List<AttendanceCode> activeCodes = attendanceCodeRepository.findByIsExpiredFalse();
        for (AttendanceCode activeCode : activeCodes) {
            activeCode.expire();
        }

        for (AttendanceCode activeCode : activeCodes) {
            activeCode.expire();

            List<Attendance> attendances =
                    attendanceRepository.findByAttendanceCodeId(activeCode.getId());

            for (Attendance attendance : attendances) {
                depositService.recalculateDeposit(attendance.getUser().getId());
            }
        }


        // 1-4) 4자리 랜덤 코드 생성 및 차수(Order) 계산
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
        String attendanceOrder = String.valueOf(codeCountOfDay + 1); // 1회차, 2회차, 3회차

        // 1-5) 새로운 AttendanceCode 생성 및 저장
        AttendanceCode attendanceCode = AttendanceCode.builder()
                .attendanceDate(date) // [수정] 파라미터로 받은 날짜 주입
                .attendanceOrder(attendanceOrder)
                .code(code)
                .isExpired(false)
                .build();

        attendanceCodeRepository.save(attendanceCode);

        // 1-6) 모든 MEMBER 유저에 대해 '현재 생성된 출석 코드' 기준 초기 출석 데이터 생성
        List<User> users = userRepository.findByRole(Role.MEMBER);

        for (User user : users) {
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

    // Q&A 이해도 체크 화면의 분모(13/29 중 29)를 계산한다.
    @Transactional(readOnly = true)
    public int countAttendedBySession(StudySession session) {
        if (session == null) {
            throw new IllegalArgumentException("세션 정보는 필수입니다.");
        }

        String attendanceOrder = resolveAttendanceOrder(session.getDayPart());
        long attendedCount = attendanceRepository.countAttendedByDateAndOrder(
                session.getSessionDate(),
                attendanceOrder
        );

        return Math.toIntExact(attendedCount);
    }

    // 현재 정책: 오전 세션은 1회차, 오후 세션은 2회차 출석 인원을 이해도 체크 분모로 사용한다.
    private String resolveAttendanceOrder(SessionDayPart dayPart) {
        if (dayPart == null) {
            throw new IllegalArgumentException("세션 오전/오후 정보는 필수입니다.");
        }

        return switch (dayPart) {
            case AM -> "1";
            case PM -> "2";
        };
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
                attendanceRepository.findByUserIdAndDate(userId, date);

        return attendances.stream()
                .map(attendance -> new AttendanceSlotRes(
                        attendance.getAttendanceCode().getId(),   // 세션 ID 대신 출석 코드 ID를 슬롯 식별값으로 사용
                        attendance.getStatus()
                ))
                .sorted(Comparator.comparing(AttendanceSlotRes::getAttendanceCodeId)) // 정렬 기준 변경
                .toList();
    }

    // 6. 나의 전체 출석 현황 조회 서비스
    public List<AttendanceStatusRes> findByUserId(Integer userId) {

        List<Attendance> attendances =
                attendanceRepository.findByUserId(Long.valueOf(userId));

        // 날짜별 그룹화
        Map<LocalDate, List<Attendance>> dateGrouped =
                attendances.stream()
                        .collect(Collectors.groupingBy(
                                attendance ->
                                        attendance.getAttendanceCode().getAttendanceDate()
                        ));

        // 주차별 그룹화
        Map<Integer, List<AttendanceDayStatusRes>> weekGrouped =
                new HashMap<>();

        for (Map.Entry<LocalDate, List<Attendance>> entry : dateGrouped.entrySet()) {

            LocalDate date = entry.getKey();

            StudySession studySession =
                    curriculumRepository
                            .findFirstBySessionDate(date)
                            .orElseThrow(() ->
                                    new RuntimeException("세션이 존재하지 않습니다.")
                            );

            int week = studySession.getWeek().intValue();

            List<AttendanceSlotRes> slots =
                    entry.getValue().stream()
                            .map(attendance ->
                                    new AttendanceSlotRes(
                                            attendance.getAttendanceCode().getId(),
                                            attendance.getStatus()
                                    )
                            )
                            .sorted(
                                    Comparator.comparing(
                                            AttendanceSlotRes::getAttendanceCodeId
                                    )
                            )
                            .toList();

            AttendanceDayStatusRes dayRes = new AttendanceDayStatusRes();
            dayRes.setDate(date);
            dayRes.setDay(date.getDayOfWeek().toString());
            dayRes.setSlots(slots);


            weekGrouped
                    .computeIfAbsent(week, k -> new ArrayList<>())
                    .add(dayRes);
        }

        return weekGrouped.entrySet().stream()
                .map(entry -> {

                    AttendanceStatusRes dto =
                            new AttendanceStatusRes();

                    dto.setWeek(entry.getKey());

                    dto.setDays(
                            entry.getValue().stream()
                                    .sorted(
                                            Comparator.comparing(
                                                    AttendanceDayStatusRes::getDate
                                            )
                                    )
                                    .toList()
                    );

                    return dto;
                })
                .sorted(
                        Comparator.comparing(
                                AttendanceStatusRes::getWeek
                        )
                )
                .toList();
    }



}

