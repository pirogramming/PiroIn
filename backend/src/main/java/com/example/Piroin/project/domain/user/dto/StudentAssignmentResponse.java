package com.example.Piroin.project.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

// 학생이 본인의 과제 조회
@Builder
@AllArgsConstructor
public class StudentAssignmentResponse {

    private Integer assignmentId;

    private String title;

    private String day;

    private String submitted;
}
