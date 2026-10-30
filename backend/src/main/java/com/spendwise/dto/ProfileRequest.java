package com.spendwise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @NotBlank(message = "Nama lengkap wajib diisi")
        @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
        String fullName,
        @NotBlank(message = "Email wajib diisi")
        @Email(message = "Format email tidak valid")
        @Size(max = 100, message = "Email maksimal 100 karakter")
        String email) {
}
