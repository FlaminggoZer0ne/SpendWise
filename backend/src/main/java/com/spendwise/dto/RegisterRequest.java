package com.spendwise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username wajib diisi")
        @Size(min = 4, max = 30, message = "Username 4-30 karakter")
        @Pattern(regexp = "[A-Za-z0-9_]*", message = "Username hanya boleh huruf, angka, dan garis bawah")
        String username,
        @NotBlank(message = "Nama lengkap wajib diisi")
        @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
        String fullName,
        @NotBlank(message = "Email wajib diisi")
        @Email(message = "Format email tidak valid")
        @Size(max = 100, message = "Email maksimal 100 karakter")
        String email,
        @NotBlank(message = "Password wajib diisi")
        @Size(min = 8, max = 72, message = "Password minimal 8 karakter")
        String password,
        @NotBlank(message = "Konfirmasi password wajib diisi")
        String confirmPassword) {
}
