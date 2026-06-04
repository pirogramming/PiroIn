package com.example.Piroin.project.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Schema(description = "사용자 주차별 출석 상태")
public class AttendanceStatusRes {

    @Schema(description = "주차", example = "1")
    private int week;

    @Schema(description = "요일별 출석 상태 목록")
    private List<AttendanceDayStatusRes> days;
}