package com.example.Piroin.project.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "출석 차시별 상태")
public class AttendanceSlotRes {

    @Schema(description = "출석 코드 ID")
    private Integer attendanceCodeId; // 변수명을 의미에 맞게 변경!

    private Boolean status;

    // 생성자 파라미터와 주입부도 변경
    public AttendanceSlotRes(Integer attendanceCodeId, Boolean status) {
        this.attendanceCodeId = attendanceCodeId;
        this.status = status;
    }
}