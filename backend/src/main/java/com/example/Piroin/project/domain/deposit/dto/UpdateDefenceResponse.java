package com.example.Piroin.project.domain.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateDefenceResponse {

    private Long userId;

    private Integer amount;

    private Integer ascentDefence;
}
