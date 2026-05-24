package com.example.Piroin.project.domain.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AssignmentWeekViewResponse {

    private String week;

    private List<DayAssignmentResponse> days;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DayAssignmentResponse {
        private String day;
        private LocalDate sessionDate;
        private List<AssignmentInfo> assignments;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AssignmentInfo {
        private Integer assignmentId;
        private String title;
    }
}
