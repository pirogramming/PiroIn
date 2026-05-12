package com.example.Piroin.project.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "출석 차시별 상태")
public class AttendanceSlotRes {
    private Long studySessionId;
    private Boolean status;

    public AttendanceSlotRes(Long studySessionId, Boolean status) {
        this.studySessionId = studySessionId;
        this.status = status;
    }
    
}

