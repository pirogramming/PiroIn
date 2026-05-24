package com.example.Piroin.project.domain.assignment.exception;

import com.example.Piroin.project.domain.assignment.exception.code.AssignmentErrorCode;

public class AssignmentException extends RuntimeException {
    public AssignmentException(AssignmentErrorCode message) {
        super(String.valueOf(message));
    }
}
