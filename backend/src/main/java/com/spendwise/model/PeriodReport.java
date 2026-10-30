package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Rekap pemasukan dan pengeluaran per hari/minggu/bulan/tahun. */
public class PeriodReport extends Report {

    /** Satu baris rekap untuk tabel/grafik. */
    public record Row(String label, double income, double expense) {
    }

    private final PeriodType periodType;
    private Map<String, Double> totalPerPeriod = new LinkedHashMap<>();
    private Map<String, Double> incomePerPeriod = new LinkedHashMap<>();
    private double previousPeriodExpense;

    public PeriodReport(int userId, PeriodType periodType, LocalDate start, LocalDate end,
                        List<Transaction> data) {
        super(userId, start, end, data);
        this.periodType = periodType;
        this.title = "Laporan per Periode";
    }

    @Override
    public void generate() {
        Map<String, Double> expense = new LinkedHashMap<>();
        Map<String, Double> income = new LinkedHashMap<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            String label = labelOf(d);
            expense.putIfAbsent(label, 0.0);
            income.putIfAbsent(label, 0.0);
        }
        for (Transaction t : transactions) {
            if (t.getDate().isBefore(startDate) || t.getDate().isAfter(endDate)) {
                continue;
            }
            Map<String, Double> target = t.getType() == TransactionType.EXPENSE ? expense : income;
            target.merge(labelOf(t.getDate()), t.getAmount(), Double::sum);
        }
        expense.replaceAll((k, v) -> round2(v));
        income.replaceAll((k, v) -> round2(v));
        this.totalPerPeriod = expense;
        this.incomePerPeriod = income;
    }

    /**
     * Perubahan total pengeluaran terhadap periode sebelumnya dalam persen (BR-10).
     *
     * @return NaN jika pengeluaran periode sebelumnya 0 atau belum diisi
     */
    public double compareWithPreviousPeriod() {
        if (previousPeriodExpense <= 0) {
            return Double.NaN;
        }
        return round2((getTotalExpense() - previousPeriodExpense) / previousPeriodExpense * 100);
    }

    /** BR-11: total pengeluaran dibagi jumlah hari dalam rentang. */
    public double getAverageDailyExpense() {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return days <= 0 ? 0 : round2(getTotalExpense() / days);
    }

    /** Diisi service dari rentang sebelumnya yang sama panjang. */
    public void setPreviousPeriodExpense(double previousPeriodExpense) {
        this.previousPeriodExpense = previousPeriodExpense;
    }

    /** Null jika tidak ada pembanding (JSON: comparisonWithPrevious). */
    @JsonProperty("comparisonWithPrevious")
    public Double getComparisonWithPrevious() {
        double v = compareWithPreviousPeriod();
        return Double.isNaN(v) ? null : v;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public List<Row> getRows() {
        List<Row> rows = new ArrayList<>();
        totalPerPeriod.forEach((label, expense) ->
                rows.add(new Row(label, incomePerPeriod.getOrDefault(label, 0.0), expense)));
        return rows;
    }

    @JsonIgnore
    public Map<String, Double> getTotalPerPeriod() {
        return Map.copyOf(totalPerPeriod);
    }

    private String labelOf(LocalDate d) {
        return switch (periodType) {
            case DAILY -> d.toString();
            case WEEKLY -> String.format("%d-W%02d", d.get(IsoFields.WEEK_BASED_YEAR),
                    d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case MONTHLY -> String.format("%d-%02d", d.getYear(), d.getMonthValue());
            case YEARLY -> String.valueOf(d.getYear());
        };
    }

    @Override
    protected String csvHeader() {
        return csvJoin("Periode", "Pemasukan", "Pengeluaran");
    }

    @Override
    protected List<String> csvRows() {
        List<String> rows = new ArrayList<>();
        for (Row r : getRows()) {
            rows.add(csvJoin(r.label(), formatNumber(r.income()), formatNumber(r.expense())));
        }
        return rows;
    }
}
