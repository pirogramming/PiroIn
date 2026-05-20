package com.example.Piroin.project.domain.deposit.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DepositErrorCode {

    DEPOSIT_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "DEPOSIT4001",
            "보증금 정보가 존재하지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}