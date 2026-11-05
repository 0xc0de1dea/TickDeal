package com.example.tickdeal.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {

    private final HttpStatus status;

    public ServiceException(ErrorCode errorcode) {
        super(errorcode.getMessage());

        this.status = errorcode.getStatus();
    }
}
