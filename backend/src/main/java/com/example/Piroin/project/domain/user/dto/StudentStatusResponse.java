package com.example.Piroin.project.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;


@Builder
@AllArgsConstructor
public class StudentStatusResponse {

    private String week;

    private String studentName;

    private List<StudentAssignmentResponse> assignments;

    private List<StudentAttendanceResponse> attendances;
}
