package com.example.Piroin.project.domain.attendance.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class GetAttendanceByDateReq {
    private Long userId;
    private LocalDate date;
}
