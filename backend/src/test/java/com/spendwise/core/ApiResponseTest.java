package com.spendwise.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void successOmitsNullFields() throws Exception {
        String json = mapper.writeValueAsString(ApiResponse.success("Berhasil", Map.of("id", 1)));
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"message\":\"Berhasil\""));
        assertFalse(json.contains("errors"));
    }

    @Test
    void errorWithFieldsIncludesErrorsAndNoData() throws Exception {
        String json = mapper.writeValueAsString(
                ApiResponse.error("Validasi gagal", Map.of("amount", "Nominal harus lebih dari 0")));
        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("\"amount\":\"Nominal harus lebih dari 0\""));
        assertFalse(json.contains("\"data\""));
    }
}
