package com.example.Piroin.project.domain.assignment.enums;

public enum AssignmentStatus {

    SUCCESS,              // 정상 제출 (0원)

    INSUFFICIENT_MINOR,   // 경미한 불충분 (-10000)

    INSUFFICIENT_15000,

    INSUFFICIENT_MAJOR,   // 심각한 불충분 (-20000)

    INSUFFICIENT_25000,

    FAILURE,              // 미제출 (-20000)

    PENDING               // 아직 채점 안 됨
}
