package com.devlog.api.config.exception;

import com.devlog.core.common.dto.Response;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.config.exception.CoreException;
import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.config.exception.ValidationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Response<String>> handleDataNotFoundException(DataNotFoundException e) {
        log.warn("DataNotFoundException: {}", e.getMessage());
        return Response.error(ResponseCode.NOT_FOUND_DATA, e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Response<String>> handleValidationException(ValidationException e) {
        log.warn("ValidationException: {}", e.getMessage());
        return Response.error(e.getResponseCode(), e.getMessage());
    }

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<Response<String>> handleCoreException(CoreException e) {
        log.error("CoreException: {}", e.getMessage());
        return Response.error(e.getResponseCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<Response<String>> handleBadRequestException(Exception e) {
        log.warn("BadRequestException: {}", e.getMessage());
        return Response.error(ResponseCode.INVALID_DATA_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<String>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("ConstraintViolationException: {}", e.getMessage());
        return Response.error(ResponseCode.INVALID_DATA_ERROR, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<String>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return Response.error(ResponseCode.INVALID_DATA_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response<String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("HttpRequestMethodNotSupportedException: {}", e.getMessage());
        return Response.error(ResponseCode.METHOD_NOT_ALLOWED_ERROR);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response<String>> handleNoResource(NoResourceFoundException e) {
        log.warn("NoResourceFoundException: {}", e.getResourcePath());
        return Response.error(ResponseCode.NOT_FOUND_DATA);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);
        return Response.error(ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
