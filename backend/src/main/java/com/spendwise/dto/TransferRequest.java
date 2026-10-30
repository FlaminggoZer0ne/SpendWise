package com.spendwise.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
        @NotNull(message = "Dompet asal wajib dipilih") Integer fromWalletId,
        @NotNull(message = "Dompet tujuan wajib dipilih") Integer toWalletId,
        @NotNull(message = "Nominal wajib diisi")
        @Positive(message = "Nominal harus lebih dari 0")
        Double amount) {
}
