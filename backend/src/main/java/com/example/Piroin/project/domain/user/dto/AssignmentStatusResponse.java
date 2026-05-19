package com.example.Piroin.project.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AssignmentStatusResponse {
    private Integer assignmentItemId;
    private Integer assignmentId;
    private String title;
    private String submitted;
}
