package com.spendwise.dto;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        int totalItems,
        int totalPages,
        double totalIncome,
        double totalExpense) {

    public static <T> PageResult<T> of(List<T> items, int page, int size, int totalItems,
                                       double totalIncome, double totalExpense) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil(totalItems / (double) size);
        return new PageResult<>(items, page, size, totalItems, totalPages, totalIncome, totalExpense);
    }
}
