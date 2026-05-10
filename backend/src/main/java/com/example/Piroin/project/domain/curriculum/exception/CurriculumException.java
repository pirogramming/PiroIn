package com.example.Piroin.project.domain.curriculum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CurriculumException extends RuntimeException {

    private final HttpStatus status;

    public CurriculumException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
