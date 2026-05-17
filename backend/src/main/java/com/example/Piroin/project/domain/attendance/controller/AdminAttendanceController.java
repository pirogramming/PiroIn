package com.example.Piroin.project.domain.attendance.controller;

import com.example.Piroin.project.domain.attendance.dto.*;
import com.example.Piroin.project.domain.attendance.entity.AttendanceCode;
import com.example.Piroin.project.domain.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "관리자 출석관리", description = "관리자용 출석 관리 API")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;

    // 1. 출석체크 시작
    @Operation(summary = "출석 체크 시작(출석코드 생성)", description = "새로운 출석 코드를 생성하고 출석 체크를 시작합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "출석 코드 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/admin/attendance/start")
    public AttendanceCodeResponse startAttendance(@PathVariable Long studySessionId) {
        AttendanceCode code = attendanceService.generateCodeAndCreateAttendances(Math.toIntExact(studySessionId));
        return AttendanceCodeResponse.from(code);
    }


    // 2. 현재 활성화된 출석코드 조회 API
    @Operation(summary = "현재 활성화된 출석 코드 조회", description = "현재 활성화된 출석 코드 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "활성화된 출석 코드 없음")
    })
    @GetMapping("/admin/attendance/active-code")
    public AttendanceCodeResponse getActiveCode() {
        return attendanceService.getActiveAttendanceCode() // 1. 서비스 호출
                .map(AttendanceCodeResponse::from)         // 2. 값이 있으면 DTO로 변환
                .orElseThrow(() -> new RuntimeException("현재 활성화된 출석코드가 없습니다")); // 3. 없으면 예외 발생
    }

    // 3. 출석체크 종료 새 url.
    @Operation(summary = "현재 활성화된 출석 코드 만료", description = "현재 활성화된 최신 출석 코드를 만료 처리합니다.")
    @PutMapping("/admin/attendance/active-code/expire")
    public String expireActiveAttendance() {
        return attendanceService.expireActiveAttendanceCode();
    }

    // 4. 출석 상태 변경 (관리자 전용)
    // 현재는 출석만 변경되지만 나중에 출석 & 과제 변경으로 바꿀 예정
    @Operation(summary = "출석 상태 변경", description = "관리자가 특정 사용자의 출석 상태를 변경합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "출석 상태 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "출석 기록을 찾을 수 없음")
    })

    @PutMapping("/admin/users/{userId}/status")
    public boolean updateUserStatus(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusReq req) {
        return attendanceService.updateUserStatus(userId, req);
    }

}
