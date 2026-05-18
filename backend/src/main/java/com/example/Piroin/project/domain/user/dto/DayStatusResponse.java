package com.example.Piroin.project.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class DayStatusResponse {

    private String day;

    private LocalDate sessionDate;

    private List<AssignmentStatusResponse> assignments;

    private List<AttendanceStatusResponse> attendances;
}
