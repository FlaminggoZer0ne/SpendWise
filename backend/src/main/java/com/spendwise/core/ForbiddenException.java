package com.spendwise.core;

/** Role pengguna tidak berhak mengakses fitur (HTTP 403). */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
