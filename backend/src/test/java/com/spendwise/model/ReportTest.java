package com.spendwise.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportTest {

    private final Category salary = new Category(1, "Gaji", TransactionType.INCOME, true, 0);
    private final Category food = new Category(6, "Makan & Minum", TransactionType.EXPENSE, true, 0);
    private final Category rent = new Category(11, "Kos & Sewa", TransactionType.EXPENSE, true, 0);
    private final Wallet wallet = new Wallet(1, 2, "BCA", WalletType.BANK, 0);

    private Transaction in(double amount, String date) {
        return new Income(amount, LocalDate.parse(date), "gaji", salary, wallet, "Kantor");
    }

    private Transaction out(double amount, String date, Category c) {
        return new Expense(amount, LocalDate.parse(date), "x", c, wallet, "QRIS", false);
    }

    private List<Transaction> sample() {
        return List.of(
                in(5_000_000, "2026-10-01"),
                out(1_300_000, "2026-10-02", rent),
                out(400_000, "2026-10-05", food),
                out(600_000, "2026-10-14", food),
                in(500_000, "2026-10-20"));
    }

    private CategoryReport categoryReport() {
        CategoryReport r = new CategoryReport(2, LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"), sample());
        r.generate();
        return r;
    }

    @Test
    void totalsAndNetBalance() {
        CategoryReport r = categoryReport();
        assertEquals(5_500_000, r.getTotalIncome(), 0.001);
        assertEquals(2_300_000, r.getTotalExpense(), 0.001);
        assertEquals(3_200_000, r.getNetBalance(), 0.001);
    }

    @Test
    void categoryReportSortsByTotalDescendingAndCountsExpensesOnly() {
        CategoryReport r = categoryReport();
        assertEquals(List.of("Kos & Sewa", "Makan & Minum"), List.copyOf(r.getTotalPerCategory().keySet().stream()
                .sorted((a, b) -> Double.compare(r.getTotalPerCategory().get(b), r.getTotalPerCategory().get(a))).toList()));
        assertEquals("Kos & Sewa", r.getTopExpenseCategory());
        assertEquals(2, r.getRows().size());
        assertEquals("Kos & Sewa", r.getRows().get(0).category());
        assertEquals(56.52, r.getPercentage("Kos & Sewa"), 0.001);
        assertEquals(43.48, r.getPercentage("Makan & Minum"), 0.001);
        assertEquals(0, r.getPercentage("Tidak ada"), 0.0);
    }

    @Test
    void categoryReportWithoutExpenses() {
        CategoryReport r = new CategoryReport(2, LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"),
                List.of(in(100, "2026-10-01")));
        r.generate();
        assertNull(r.getTopExpenseCategory());
        assertTrue(r.getRows().isEmpty());
    }

    @Test
    void categoryCsvFormat() {
        String csv = categoryReport().toCSV();
        assertTrue(csv.startsWith("﻿Kategori;Total;Persentase\r\n"));
        assertTrue(csv.contains("Kos & Sewa;1300000;56,52\r\n"));
        assertTrue(csv.contains("Total Pemasukan;5500000;\r\n"));
        assertTrue(csv.endsWith("Total Pengeluaran;2300000;\r\n"));
    }

    @Test
    void periodReportMonthlyGroupsAndFillsEmptyPeriods() {
        PeriodReport r = new PeriodReport(2, PeriodType.MONTHLY, LocalDate.parse("2026-09-01"),
                LocalDate.parse("2026-11-30"), sample());
        r.generate();
        assertEquals(3, r.getRows().size());
        assertEquals("2026-09", r.getRows().get(0).label());
        assertEquals(0, r.getRows().get(0).expense(), 0.0);
        assertEquals(5_500_000, r.getRows().get(1).income(), 0.001);
        assertEquals(2_300_000, r.getRows().get(1).expense(), 0.001);
    }

    @Test
    void periodReportWeeklyUsesIsoWeekLabels() {
        PeriodReport r = new PeriodReport(2, PeriodType.WEEKLY, LocalDate.parse("2026-10-01"),
                LocalDate.parse("2026-10-31"), sample());
        r.generate();
        List<String> labels = r.getRows().stream().map(PeriodReport.Row::label).toList();
        assertEquals(List.of("2026-W40", "2026-W41", "2026-W42", "2026-W43", "2026-W44"), labels);
        assertEquals(5_000_000, r.getRows().get(0).income(), 0.001);
        assertEquals(1_300_000, r.getRows().get(0).expense(), 0.001);
        assertEquals(400_000, r.getRows().get(1).expense(), 0.001);
    }

    @Test
    void periodReportWeekLabelAcrossYearBoundary() {
        PeriodReport r = new PeriodReport(2, PeriodType.WEEKLY, LocalDate.parse("2026-12-31"),
                LocalDate.parse("2027-01-04"), List.of());
        r.generate();
        assertEquals("2026-W53", r.getRows().get(0).label());
    }

    @Test
    void periodReportDailyAndYearlyLabels() {
        PeriodReport d = new PeriodReport(2, PeriodType.DAILY, LocalDate.parse("2026-10-01"),
                LocalDate.parse("2026-10-03"), sample());
        d.generate();
        assertEquals(List.of("2026-10-01", "2026-10-02", "2026-10-03"),
                d.getRows().stream().map(PeriodReport.Row::label).toList());
        PeriodReport y = new PeriodReport(2, PeriodType.YEARLY, LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-12-31"), sample());
        y.generate();
        assertEquals(1, y.getRows().size());
        assertEquals("2026", y.getRows().get(0).label());
    }

    @Test
    void averageDailyExpenseDividesByDaysInRange() {
        PeriodReport r = new PeriodReport(2, PeriodType.MONTHLY, LocalDate.parse("2026-10-01"),
                LocalDate.parse("2026-10-31"), sample());
        r.generate();
        assertEquals(74193.55, r.getAverageDailyExpense(), 0.001);
    }

    @Test
    void comparisonWithPreviousPeriod() {
        PeriodReport r = new PeriodReport(2, PeriodType.MONTHLY, LocalDate.parse("2026-10-01"),
                LocalDate.parse("2026-10-31"), sample());
        r.generate();
        assertTrue(Double.isNaN(r.compareWithPreviousPeriod()));
        assertNull(r.getComparisonWithPrevious());
        r.setPreviousPeriodExpense(2_000_000);
        assertEquals(15.0, r.compareWithPreviousPeriod(), 0.001);
        r.setPreviousPeriodExpense(2_875_000);
        assertEquals(-20.0, r.compareWithPreviousPeriod(), 0.001);
    }

    @Test
    void periodCsvFormat() {
        PeriodReport r = new PeriodReport(2, PeriodType.MONTHLY, LocalDate.parse("2026-10-01"),
                LocalDate.parse("2026-10-31"), sample());
        r.generate();
        String csv = r.toCSV();
        assertTrue(csv.contains("Periode;Pemasukan;Pengeluaran\r\n"));
        assertTrue(csv.contains("2026-10;5500000;2300000\r\n"));
    }

    @Test
    void csvQuotesTextWithSeparator() {
        Category odd = new Category(20, "A;B \"x\"", TransactionType.EXPENSE, false, 2);
        CategoryReport r = new CategoryReport(2, LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"),
                List.of(out(100, "2026-10-01", odd)));
        r.generate();
        assertTrue(r.toCSV().contains("\"A;B \"\"x\"\"\";100;100\r\n"));
    }

    @Test
    void dashboardSavingRate() {
        DashboardSummary s = new DashboardSummary(1_333_000, 5_500_000, 3_582_000, 4, 1, 1);
        assertEquals(34.87, s.getSavingRate(), 0.001);
        assertEquals(34.87, s.savingRateOrNull(), 0.001);
        DashboardSummary none = new DashboardSummary(0, 0, 100, 0, 0, 0);
        assertTrue(Double.isNaN(none.getSavingRate()));
        assertNull(none.savingRateOrNull());
    }
}
