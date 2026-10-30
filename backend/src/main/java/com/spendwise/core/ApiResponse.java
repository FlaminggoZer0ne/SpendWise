package com.spendwise.core;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Pembungkus JSON standar: {@code {success, message, data, errors}}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private final boolean success;
    private final String message;
    private final Object data;
    private final Map<String, String> errors;

    private ApiResponse(boolean success, String message, Object data, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static ApiResponse success(Object data) {
        return new ApiResponse(true, null, data, null);
    }

    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data, null);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null, null);
    }

    public static ApiResponse error(String message, Map<String, String> errors) {
        return new ApiResponse(false, message, null, errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
