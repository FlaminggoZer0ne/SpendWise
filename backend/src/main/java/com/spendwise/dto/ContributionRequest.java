package com.spendwise.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ContributionRequest(
        @NotNull(message = "Dompet sumber wajib dipilih") Integer walletId,
        @NotNull(message = "Nominal wajib diisi")
        @Positive(message = "Nominal harus lebih dari 0")
        Double amount) {
}
