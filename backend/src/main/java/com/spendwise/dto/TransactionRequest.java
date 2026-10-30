package com.spendwise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spendwise.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Jenis transaksi wajib dipilih") TransactionType type,
        @NotNull(message = "Nominal wajib diisi")
        @Positive(message = "Nominal harus lebih dari 0")
        Double amount,
        @NotNull(message = "Tanggal wajib diisi") LocalDate date,
        @NotNull(message = "Kategori wajib dipilih") Integer categoryId,
        @NotNull(message = "Dompet wajib dipilih") Integer walletId,
        @Size(max = 255, message = "Deskripsi maksimal 255 karakter") String description,
        @Size(max = 100, message = "Sumber maksimal 100 karakter") String source,
        @Size(max = 30, message = "Metode pembayaran maksimal 30 karakter") String paymentMethod,
        @JsonProperty("isRecurring") Boolean isRecurring) {
}
