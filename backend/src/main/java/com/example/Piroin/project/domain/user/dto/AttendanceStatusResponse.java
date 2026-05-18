package com.example.Piroin.project.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AttendanceStatusResponse {

    private Integer attendanceCodeId;

    private String attendanceOrder;

    private Boolean attended;
}
