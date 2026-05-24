package com.example.Piroin.project.domain.user.dto;

import com.example.Piroin.project.domain.assignment.enums.AssignmentStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateStudentStatusRequest {

    private List<AssignmentStatusRequest> assignments;

    private List<AttendanceStatusRequest> attendances;

    @Getter
    public static class AssignmentStatusRequest {

        private Integer assignmentItemId;

        private AssignmentStatus submitted;
    }

    @Getter
    public static class AttendanceStatusRequest {

        private Integer attendanceId;

        private Boolean status;
    }
}