package com.ch.switchwon.presentation.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    String code,
    String message,
    T returnObject
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", "정상적으로 처리되었습니다.", data);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
