package com.spendwise.core;

import org.springframework.http.HttpStatus;

/** Pelanggaran aturan bisnis; status HTTP-nya dipakai oleh {@link GlobalExceptionHandler}. */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message) {
        this(HttpStatus.BAD_REQUEST, message);
    }

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
