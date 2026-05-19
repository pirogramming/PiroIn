package com.example.Piroin.project.domain.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class StudentAttendanceResponse {
    private Integer attendanceId;

    private String day;

    private String attendanceOrder;

    private Boolean attended;
}
