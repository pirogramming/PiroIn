package com.example.Piroin.project.domain.deposit.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepositResponse {

    // 현재 남은 보증금
    private Integer amount;

    // 과제 차감 누적
    private Integer descentAssignment;

    // 출석 차감 누적
    private Integer descentAttendance;

    // 방어권 누적
    private Integer ascentDefence;
}