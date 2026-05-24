package com.example.Piroin.project.domain.curriculum.exception;

import com.example.Piroin.project.domain.curriculum.exception.code.CurriculumErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CurriculumException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public CurriculumException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.code = null;
    }

    public CurriculumException(CurriculumErrorCode curriculumErrorCode) {
        super(curriculumErrorCode.getMessage());
        this.status = curriculumErrorCode.getStatus();
        this.code = curriculumErrorCode.getCode();
    }
}