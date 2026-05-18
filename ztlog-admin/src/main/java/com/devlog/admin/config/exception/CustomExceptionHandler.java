package com.devlog.admin.config.exception;

import com.devlog.core.common.dto.Response;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.config.exception.DataConflictException;
import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.config.exception.InternalServerException;
import com.devlog.core.config.exception.ValidationException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Response<String>> handleDataNotFoundException(DataNotFoundException e) {
        log.warn("DataNotFoundException: {}", e.getMessage());
        return Response.error(ResponseCode.NOT_FOUND_DATA, e.getMessage());
    }

    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<Response<String>> handleDataConflictException(DataConflictException e) {
        log.warn("DataConflictException: {}", e.getMessage());
        return Response.error(ResponseCode.CONFLICT_USER_ERROR, e.getMessage());
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<Response<String>> handleInternalServerException(InternalServerException e) {
        log.error("InternalServerException: {}", e.getMessage());
        return Response.error(ResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Response<String>> handleJwtException(JwtException e) {
        log.warn("JwtException: {}", e.getMessage());
        ResponseCode responseCode = e.getMessage().contains("EXPIRED")
                ? ResponseCode.UNAUTHORIZED_EXPIRED_TOKEN
                : ResponseCode.UNAUTHORIZED_INVALID_TOKEN;
        return Response.error(responseCode);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Response<String>> handleValidationException(ValidationException e) {
        log.warn("ValidationException: {}", e.getMessage());
        return Response.error(e.getResponseCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("유효성 검사 실패");
        log.warn("ValidationException: {}", message);
        return Response.error(ResponseCode.INVALID_DATA_ERROR, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String errorMessage = "잘못된 JSON 형식입니다. UTF-8 인코딩을 확인하고 특수문자(스마트 따옴표 등)를 제거해주세요.";
        log.error("HttpMessageNotReadableException: {}", e.getMessage());
        if (e.getMessage() != null && e.getMessage().contains("UTF-8")) {
            log.error("UTF-8 encoding error detected. Check for smart quotes or non-UTF-8 characters in request body.");
        }
        return Response.error(ResponseCode.INVALID_DATA_ERROR, errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);
        return Response.error(ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
