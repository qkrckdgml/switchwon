package com.ch.switchwon.presentation.web;

import com.ch.switchwon.domain.exception.CurrencyNotFoundException;
import com.ch.switchwon.domain.exception.ExchangeRateNotFoundException;
import com.ch.switchwon.domain.exception.InvalidOrderRequestException;
import com.ch.switchwon.presentation.web.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CurrencyNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleCurrencyNotFound(CurrencyNotFoundException e) {
        return ApiResponse.error("INVALID_CURRENCY", e.getMessage());
    }

    @ExceptionHandler(ExchangeRateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleExchangeRateNotFound(ExchangeRateNotFoundException e) {
        return ApiResponse.error("EXCHANGE_RATE_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(InvalidOrderRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleInvalidOrder(InvalidOrderRequestException e) {
        return ApiResponse.error("INVALID_ORDER_REQUEST", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        return ApiResponse.error("VALIDATION_ERROR", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return ApiResponse.error("INVALID_REQUEST_BODY", "요청 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ApiResponse.error("INVALID_PARAMETER", "파라미터 형식이 올바르지 않습니다: " + e.getName());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneral(Exception e) {
        log.error("[서버 오류]", e);
        return ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
    }
}