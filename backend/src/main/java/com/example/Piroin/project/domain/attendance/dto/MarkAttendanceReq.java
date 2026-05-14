package com.example.Piroin.project.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "출석 체크 요청")
public class MarkAttendanceReq {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "스터디 세션 ID", example = "1")
    private Long studySessionId;

    @Schema(description = "출석 코드", example = "1234")
    private String code;
}
