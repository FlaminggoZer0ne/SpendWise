package com.spendwise.dto;

import jakarta.validation.constraints.NotNull;

public record ActiveRequest(@NotNull(message = "Status aktif wajib diisi") Boolean active) {
}
