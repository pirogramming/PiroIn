package com.example.Piroin.project.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class StudentWeeklyStatusResponse {

    private Long week;

    private List<DayStatusResponse> days;
}
