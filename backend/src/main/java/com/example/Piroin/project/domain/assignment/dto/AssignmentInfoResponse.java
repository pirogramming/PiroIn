package com.example.Piroin.project.domain.assignment.dto;

import com.example.Piroin.project.domain.assignment.enums.AssignmentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentInfoResponse {

    private Integer assignmentId;

    private String title;

    private String week;

    private String sessionDate;

    private String day;

    private AssignmentStatus submitted;
}