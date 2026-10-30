package com.spendwise.core;

/** Pengguna belum login atau session habis (HTTP 401). */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
