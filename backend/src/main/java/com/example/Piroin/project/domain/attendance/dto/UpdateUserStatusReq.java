package com.example.Piroin.project.domain.attendance.dto;

import com.example.Piroin.project.domain.assignment.enums.AssignmentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserStatusReq {

    private Long attendanceId;
    private Boolean attendanceStatus;

    private Long assignmentItemId;
    private AssignmentStatus assignmentStatus;
}
