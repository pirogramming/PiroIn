package com.example.Piroin.project.global.response;

import com.example.Piroin.project.global.response.code.BaseCode;

public record ApiResponse<T>(
        Boolean isSuccess,
        String code,
        String message,
        T result
) {
    public static <T> ApiResponse<T> onSuccess(BaseCode code, T result) {
        return new ApiResponse<>(true, code.getCode(), code.getMessage(), result);
    }

    public static ApiResponse<Void> onFailure(BaseCode code) {
        return new ApiResponse<>(false, code.getCode(), code.getMessage(), null);
    }

    public static ApiResponse<Void> onFailure(BaseCode code, String message) {
        return new ApiResponse<>(false, code.getCode(), message, null);
    }
}
