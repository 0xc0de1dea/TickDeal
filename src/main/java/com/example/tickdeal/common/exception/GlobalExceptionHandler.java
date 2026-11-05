package com.example.tickdeal.common.exception;

import com.example.tickdeal.common.dto.ApiResponse;
import com.example.tickdeal.common.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        FieldError fieldError =
                ex.getBindingResult().getFieldErrors().stream()
                        .findFirst().orElse(null);

        String message =
                fieldError == null
                        ? ErrorCode.VALIDATION_ERROR.getMessage()
                        : fieldError.getDefaultMessage();

        ErrorResponse body = new ErrorResponse(ErrorCode.VALIDATION_ERROR.name(), message);

        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.VALIDATION_ERROR.getStatus(), body));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleConstraintViolationException(
            ConstraintViolationException ex
    ) {
        ConstraintViolation<?> violation = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .orElse(null);

        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse body = new ErrorResponse(
                ErrorCode.VALIDATION_ERROR.name(),
                message
        );

        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiResponse.fail(
                        ErrorCode.VALIDATION_ERROR.getStatus(),
                        body
                ));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ExceptionResponse> handleServiceException(
            ServiceException e, HttpServletRequest request
    ) {
        return ResponseEntity.status(e.getStatus())
                .body(ExceptionResponse.from(
                        e.getStatus().value(),
                        e.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(
            Exception e, HttpServletRequest request
    ) {
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse.from(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "알 수 없는 에러가 발생했습니다. 다시 시도해주세요.",
                        request.getRequestURI()
                ));
    }
}
