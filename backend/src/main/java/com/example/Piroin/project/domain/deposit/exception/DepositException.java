package com.example.Piroin.project.domain.deposit.exception;

import com.example.Piroin.project.domain.deposit.exception.code.DepositErrorCode;
import lombok.Getter;

@Getter
public class DepositException extends RuntimeException {

    private final DepositErrorCode errorCode;

    public DepositException(DepositErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}