package com.spendwise.dto;

import com.spendwise.model.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Filter riwayat transaksi; semua field opsional, default dihitung lewat method pembantu. */
public record TransactionFilter(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        TransactionType type,
        Integer categoryId,
        Integer walletId,
        String q,
        Integer page,
        Integer size) {

    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;

    public LocalDate fromOrDefault() {
        return from != null ? from : LocalDate.now().withDayOfMonth(1);
    }

    public LocalDate toOrDefault() {
        return to != null ? to : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    }

    public int pageOrDefault() {
        return page == null || page < 1 ? 1 : page;
    }

    public int sizeOrDefault() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    public int offset() {
        return (pageOrDefault() - 1) * sizeOrDefault();
    }
}
