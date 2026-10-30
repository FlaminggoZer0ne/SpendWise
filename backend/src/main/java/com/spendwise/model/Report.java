package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Superclass laporan. Tidak mengakses DB; hanya merekap daftar transaksi yang diberikan. */
public abstract class Report implements Exportable {

    private static final String BOM = "﻿";
    private static final String SEP = ";";
    private static final String EOL = "\r\n";

    protected String title;
    protected int userId;
    protected LocalDate startDate;
    protected LocalDate endDate;
    protected List<Transaction> transactions;
    protected LocalDateTime generatedAt;

    public Report(int userId, LocalDate start, LocalDate end, List<Transaction> data) {
        this.userId = userId;
        this.startDate = start;
        this.endDate = end;
        this.transactions = data == null ? new ArrayList<>() : new ArrayList<>(data);
        this.generatedAt = LocalDateTime.now();
    }

    /** Mengisi struktur rekap milik subclass dari {@link #transactions}. */
    public abstract void generate();

    public double getTotalIncome() {
        return sumOf(TransactionType.INCOME);
    }

    public double getTotalExpense() {
        return sumOf(TransactionType.EXPENSE);
    }

    public double getNetBalance() {
        return round2(getTotalIncome() - getTotalExpense());
    }

    /** Template: BOM + header + baris detail (subclass) + ringkasan total. */
    @Override
    public String toCSV() {
        StringBuilder sb = new StringBuilder(BOM);
        sb.append(csvHeader()).append(EOL);
        for (String row : csvRows()) {
            sb.append(row).append(EOL);
        }
        sb.append("Total Pemasukan").append(SEP).append(formatNumber(getTotalIncome())).append(SEP).append(EOL);
        sb.append("Total Pengeluaran").append(SEP).append(formatNumber(getTotalExpense())).append(SEP).append(EOL);
        return sb.toString();
    }

    protected abstract String csvHeader();

    protected abstract List<String> csvRows();

    protected static String csvJoin(String... cells) {
        return String.join(SEP, cells);
    }

    /** Angka bulat tanpa desimal, selain itu 2 desimal dengan koma (terbaca Excel lokal Indonesia). */
    protected static String formatNumber(double value) {
        double v = round2(value);
        if (v == Math.rint(v)) {
            return String.valueOf((long) v);
        }
        return String.format(java.util.Locale.ROOT, "%.2f", v).replace('.', ',');
    }

    /** Sel teks aman untuk CSV: dikutip jika memuat pemisah/kutip/baris baru. */
    protected static String csvText(String text) {
        if (text == null) {
            return "";
        }
        if (text.contains(SEP) || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    protected static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    private double sumOf(TransactionType type) {
        double sum = 0;
        for (Transaction t : transactions) {
            if (t.getType() == type) {
                sum += t.getAmount();
            }
        }
        return round2(sum);
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @JsonIgnore
    public int getUserId() {
        return userId;
    }

    @JsonIgnore
    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

    @JsonIgnore
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}
