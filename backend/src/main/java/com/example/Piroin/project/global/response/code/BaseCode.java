package com.example.Piroin.project.global.response.code;

import org.springframework.http.HttpStatus;

public interface BaseCode {
    HttpStatus getStatus();

    String getCode();

    String getMessage();
}
