package com.example.Piroin.project.global.response;

import com.example.Piroin.project.global.response.code.BaseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResponseUtil {
    private ResponseUtil() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(BaseCode code, T result) {
        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.onSuccess(code, result));
    }

    public static ResponseEntity<ApiResponse<Void>> failure(BaseCode code) {
        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.onFailure(code));
    }

    public static ResponseEntity<ApiResponse<Void>> failure(BaseCode code, String message) {
        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.onFailure(code, message));
    }

    public static ResponseEntity<ApiResponse<Void>> failure(HttpStatus status, String code, String message) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(false, code, message, null));
    }
}
