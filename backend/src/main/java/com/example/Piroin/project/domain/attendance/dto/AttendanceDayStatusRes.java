package com.example.Piroin.project.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Schema(description = "요일별 출석 상태")
public class AttendanceDayStatusRes {

    @Schema(description = "출석 날짜", example = "2026-06-23")
    private LocalDate date;

    @Schema(description = "요일", example = "TUESDAY")
    private String day;

    @Schema(description = "출석 차시별 상태 목록")
    private List<AttendanceSlotRes> slots;
}
