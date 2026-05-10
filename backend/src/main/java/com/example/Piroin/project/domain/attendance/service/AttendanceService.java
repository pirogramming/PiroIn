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

    private final StudySession studySession;
    private final CurriculumRepository curriculumRepository;


    // 1. 출석 시작 코드
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





    // 2. 출석 체크
    @Transactional
    public AttendanceMarkResponse markAttendance(Long userId, Long studySessionId, String inputCode) {
        AttendanceCode code = attendanceCodeRepository
                .findByCodeAndStudySessionId(inputCode, studySessionId)
                .orElse(null);

        if (code == null) {
            return AttendanceMarkResponse.invalidCode();
        }

        if (Boolean.TRUE.equals(code.getIsExpired())) {
            return AttendanceMarkResponse.codeExpired();
        }

        Attendance attendance = attendanceRepository
                .findByUserIdAndStudySessionId(userId, studySessionId)
                .orElse(null);

        if (attendance == null) {
            return AttendanceMarkResponse.error("출석 정보를 찾을 수 없습니다.");
        }

        if (Boolean.TRUE.equals(attendance.getStatus())) {
            return AttendanceMarkResponse.alreadyMarked();
        }

        attendance.updateStatus(true);

        depositService.recalculateDeposit(userId);

        return AttendanceMarkResponse.success();
    }

    // 3. 출석 코드 만료시키기.
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








    // 여기 아래부터는 기존 피로체크 코드
    // 출석코드 생성 함수
    /*
    @Transactional
    public AttendanceCode generateCodeAndCreateAttendances() {
        LocalDate today = LocalDate.now();

        // 만료되지 않은 출석 코드가 있는지 확인
        List<AttendanceCode> activeAttendanceCodes = attendanceCodeRepository.findByDateAndIsExpiredFalse(today);
        if (!activeAttendanceCodes.isEmpty()) {
            // 만료되지 않은 코드가 있으면 해당 코드 반환
            return activeAttendanceCodes.get(0);
        }

        // 오늘 생성된 출석코드 개수 = 현재까지 생성된 차시 수 + 1 (MAX=3)
        int currentOrder = attendanceCodeRepository.countByDate(today) + 1;

        // 하루 최대 3회 출석 체크만 허용
        if (currentOrder > 3) {
            throw new IllegalStateException("하루에 최대 3회까지만 출석 체크를 진행할 수 있습니다.");
        }

        // 1. 출석 코드 생성
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));

        AttendanceCode attendanceCode = new AttendanceCode();
        attendanceCode.setCode(code);
        attendanceCode.setDate(today);
        attendanceCode.setOrder(currentOrder);
        attendanceCodeRepository.save(attendanceCode);

        // 2. user 권한을 가진 학생 리스트 조회
        List<User> users = userRepository.findByRole(Role.MEMBER);

        // 3. 각 학생에 대해 출석 데이터 미리 생성
        for (User user : users) {
            Attendance attendance = new Attendance();
            attendance.setUser(user);
            attendance.setDate(LocalDate.now());
            attendance.setOrder(currentOrder);
            attendance.setStatus(false); // 기본은 false
            attendanceRepository.save(attendance);
        }
        return attendanceCode;
    } */

    // 현재 활성화된 출석코드 조회 함수
    public Optional<AttendanceCode> getActiveAttendanceCode() {
        LocalDate today = LocalDate.now();
        List<AttendanceCode> activeCodes = attendanceCodeRepository.findByDateAndIsExpiredFalse(today);

        if (activeCodes.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(activeCodes.get(0));
    }

    // 가장 최근 활성화된 출석코드 만료처리 함수
    @Transactional
    public String expireLatestAttendanceCode() {
        Optional<AttendanceCode> activeCodeOpt = getActiveAttendanceCode();

        if (activeCodeOpt.isEmpty()) {
            return "현재 활성화된 출석코드가 없습니다";
        }

        AttendanceCode code = activeCodeOpt.get();
        code.setExpired(true);
        attendanceCodeRepository.save(code);

        return "출석 코드가 성공적으로 만료되었습니다";
    }

    /*
    // 출석코드 만료처리 함수
    @Transactional
    public String expireAttendanceCode(String code) {
        Optional<AttendanceCode> codeOpt = attendanceCodeRepository.findByCodeAndDate(code, LocalDate.now());

        if (codeOpt.isEmpty()) {
            return "존재하지 않는 출석 코드입니다";
        }

        AttendanceCode attendanceCode = codeOpt.get();

        if (attendanceCode.isExpired()) {
            return "이미 만료된 출석 코드입니다";
        }

        attendanceCode.setExpired(true);
        attendanceCodeRepository.save(attendanceCode);

        // 보증금
        List<Attendance> absents = attendanceRepository.findByDateAndOrderAndStatusFalse(
                attendanceCode.getDate(), attendanceCode.getOrder());

        for (Attendance attendance : absents) {
            depositService.recalculateDeposit(attendance.getUser().getId());
        }
        return "출석 코드가 성공적으로 만료되었습니다";
    }

*/

    // 출석처리 함수
    @Transactional
    public AttendanceMarkResponse markAttendance(Long userId, String inputCode) {
        // 오늘 날짜
        LocalDate today = LocalDate.now();

        // 현재 활성화된 출석 코드가 있는지 확인
        List<AttendanceCode> activeCodes = attendanceCodeRepository.findByDateAndIsExpiredFalse(today);

        // 활성화된 출석 코드가 없는 경우
        if (activeCodes.isEmpty()) {
            return AttendanceMarkResponse.noActiveSession();
        }

        // 입력한 출석 코드와 일치하는 코드가 있는지 확인
        Optional<AttendanceCode> validCodeOpt = attendanceCodeRepository.findByCodeAndDate(inputCode, today);

        // 입력한 출석 코드가 존재하지 않는 경우
        if (validCodeOpt.isEmpty()) {
            return AttendanceMarkResponse.invalidCode();
        }

        AttendanceCode code = validCodeOpt.get();

        // 입력한 출석 코드가 만료된 경우
        if (code.isExpired()) {
            return AttendanceMarkResponse.codeExpired();
        }

        // 2. 해당 유저의 출석 레코드 조회
        Optional<Attendance> attendanceOpt = attendanceRepository.findByUserIdAndDateAndOrder(userId, code.getDate(), code.getOrder());

        if (attendanceOpt.isEmpty()) {
            return AttendanceMarkResponse.error("출석 정보를 찾을 수 없습니다");
        }

        // 3. 출석 처리
        Attendance attendance = attendanceOpt.get();

        // 이미 출석한 경우
        if (attendance.isStatus()) {
            return AttendanceMarkResponse.alreadyMarked();
        }

        attendance.setStatus(true);
        attendanceRepository.save(attendance);

        //보증금 재계산
        depositService.recalculateDeposit(userId);

        return AttendanceMarkResponse.success();
    }

    // 유저의 전체 출석 현황을 조회하는 함수
    public List<AttendanceStatusRes> findByUserId(Long userId) {
        List<Attendance> attendances = attendanceRepository.findByUserId(userId);

        // 날짜별로 그룹화
        Map<LocalDate, List<Attendance>> grouped = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getDate));

        // 날짜별로 DTO 변환
        return grouped.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<AttendanceSlotRes> slots = entry.getValue().stream()
                            .map(a -> new AttendanceSlotRes(a.getOrder(), a.isStatus()))
                            .sorted(Comparator.comparingInt(AttendanceSlotRes::getOrder))
                            .toList();

                    AttendanceStatusRes dto = new AttendanceStatusRes();
                    dto.setDate(date);
                    dto.setSlots(slots);
                    return dto;
                })
                .sorted(Comparator.comparing(AttendanceStatusRes::getDate).reversed())
                .toList();
    }

    // 유저의 특정 날짜의 출석 현황을 조회하는 함수
    public List<AttendanceSlotRes> findByUserIdAndDate(Long userId, LocalDate date) {
        List<Attendance> attendances = attendanceRepository.findByUserIdAndDate(userId, date);

        return attendances.stream()
                .map(a -> new AttendanceSlotRes(a.getOrder(), a.isStatus()))
                .sorted(Comparator.comparingInt(AttendanceSlotRes::getOrder))
                .toList();
    }

    // 관리자가 유저의 출석 상태를 변경하는 함수
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

    // 특정 날짜와 차수의 모든 학생 출석 현황 조회
    public List<UserAttendanceStatusRes> findAllByDateAndOrder(LocalDate date, int order) {
        // 해당 날짜와 차수에 대한 모든 출석 기록 조회
        List<Attendance> attendances = attendanceRepository.findByDateAndOrder(date, order);

        // 사용자별로 DTO 변환
        return attendances.stream()
                .map(attendance -> {
                    User user = attendance.getUser();
                    return UserAttendanceStatusRes.builder()
                            .userId(user.getId())
                            .username(user.getName())
                            .date(attendance.getDate())
                            .order(attendance.getOrder())
                            .status(attendance.isStatus())
                            .attendanceId(attendance.getId())  // 출석 기록 ID 추가
                            .build();
                })
                .sorted(Comparator.comparing(UserAttendanceStatusRes::getUsername))
                .toList();
    }

    // 특정 학생의 모든 출석 현황 조회
    public List<UserAttendanceStatusRes> findAllByUserId(Long userId) {
        // 해당 사용자의 모든 출석 기록 조회
        List<Attendance> attendances = attendanceRepository.findByUserId(userId);

        // DTO 변환
        return attendances.stream()
                .map(attendance -> {
                    User user = attendance.getUser();
                    return UserAttendanceStatusRes.builder()
                            .userId(user.getId())
                            .username(user.getName())
                            .date(attendance.getDate())
                            .order(attendance.getOrder())
                            .status(attendance.isStatus())
                            .attendanceId(attendance.getId())
                            .build();
                })
                .sorted(Comparator.comparing(UserAttendanceStatusRes::getDate).reversed()
                        .thenComparing(UserAttendanceStatusRes::getOrder))
                .toList();
    }

    // 특정 사용자의 특정 출석 기록 삭제
    @Transactional
    public boolean deleteAttendance(Long attendanceId) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);

        if (attendanceOpt.isEmpty()) {
            return false;
        }

        Attendance attendance = attendanceOpt.get(); // 변수로 저장
        Long userId = attendance.getUser().getId();

        attendanceRepository.delete(attendance);

        // 출석 삭제 후 보증금 재계산
        depositService.recalculateDeposit(userId);
        return true;
    }

    // 특정 사용자의 특정 날짜와 차수 출석 기록 조회
    public UserAttendanceStatusRes findByUserIdAndDateAndOrder(Long userId, LocalDate date, int order) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findByUserIdAndDateAndOrder(userId, date, order);

        if (attendanceOpt.isEmpty()) {
            return null;
        }

        Attendance attendance = attendanceOpt.get();
        User user = attendance.getUser();

        return UserAttendanceStatusRes.builder()
                .userId(user.getId())
                .username(user.getName())
                .date(attendance.getDate())
                .order(attendance.getOrder())
                .status(attendance.isStatus())
                .attendanceId(attendance.getId())
                .build();
    }

    // 특정 출석 ID로 출석 기록 조회
    public UserAttendanceStatusRes findById(Long attendanceId) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);

        if (attendanceOpt.isEmpty()) {
            return null;
        }

        Attendance attendance = attendanceOpt.get();
        User user = attendance.getUser();

        return UserAttendanceStatusRes.builder()
                .userId(user.getId())
                .username(user.getName())
                .date(attendance.getDate())
                .order(attendance.getOrder())
                .status(attendance.isStatus())
                .attendanceId(attendance.getId())
                .build();
    }
}
