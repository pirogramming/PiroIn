package com.example.Piroin.project.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class StudentAssignmentResponse {

    private Integer assignmentId;

    private String title;

    private String day;

    private String submitted;
}
