package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Rekap pengeluaran per kategori beserta persentasenya. */
public class CategoryReport extends Report {

    /** Satu baris rekap untuk tabel/grafik. */
    public record Row(String category, double total, double percentage) {
    }

    private Map<String, Double> totalPerCategory = new LinkedHashMap<>();

    public CategoryReport(int userId, LocalDate start, LocalDate end, List<Transaction> data) {
        super(userId, start, end, data);
        this.title = "Laporan Pengeluaran per Kategori";
    }

    @Override
    public void generate() {
        Map<String, Double> sums = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                sums.merge(t.getCategory().getName(), t.getAmount(), Double::sum);
            }
        }
        Map<String, Double> sorted = new LinkedHashMap<>();
        sums.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> sorted.put(e.getKey(), round2(e.getValue())));
        this.totalPerCategory = sorted;
    }

    /** @return nama kategori pengeluaran terbesar, atau null jika tidak ada pengeluaran */
    public String getTopExpenseCategory() {
        return totalPerCategory.keySet().stream().findFirst().orElse(null);
    }

    public double getPercentage(String category) {
        Double total = totalPerCategory.get(category);
        double all = getTotalExpense();
        if (total == null || all <= 0) {
            return 0;
        }
        return round2(total / all * 100);
    }

    public List<Row> getRows() {
        List<Row> rows = new ArrayList<>();
        totalPerCategory.forEach((cat, total) -> rows.add(new Row(cat, total, getPercentage(cat))));
        return rows;
    }

    @JsonIgnore
    public Map<String, Double> getTotalPerCategory() {
        return Map.copyOf(totalPerCategory);
    }

    @Override
    protected String csvHeader() {
        return csvJoin("Kategori", "Total", "Persentase");
    }

    @Override
    protected List<String> csvRows() {
        List<String> rows = new ArrayList<>();
        for (Row r : getRows()) {
            rows.add(csvJoin(csvText(r.category()), formatNumber(r.total()), formatNumber(r.percentage())));
        }
        return rows;
    }
}
