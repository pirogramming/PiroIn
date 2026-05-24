package com.example.Piroin.project.domain.deposit.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDepositViewResponse {

    private Long userId;

    private String name;

    private Integer amount;

    private Integer descentAssignment;

    private Integer descentAttendance;

    private Integer ascentDefence;
}
