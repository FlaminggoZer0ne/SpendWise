package com.spendwise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordRequest(
        @NotBlank(message = "Password lama wajib diisi") String oldPassword,
        @NotBlank(message = "Password baru wajib diisi")
        @Size(min = 8, max = 72, message = "Password baru minimal 8 karakter")
        String newPassword,
        @NotBlank(message = "Konfirmasi password wajib diisi") String confirmPassword) {
}
