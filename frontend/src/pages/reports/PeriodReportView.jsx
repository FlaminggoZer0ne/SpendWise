import { Bar } from "react-chartjs-2";
import Card, { StatCard } from "../../components/ui/Card";
import EmptyState from "../../components/ui/EmptyState";
import { Table, Td, Th } from "../../components/ui/Table";
import { EXPENSE_COLOR, INCOME_COLOR, compactRupiah } from "../../utils/chart";
import { formatPersen, formatRupiah } from "../../utils/format";
import { SummaryCards } from "./CategoryReportView";

export default function PeriodReportView({ report }) {
  const chart = {
    labels: report.rows.map((r) => r.label),
    datasets: [
      { label: "Pemasukan", data: report.rows.map((r) => r.income), backgroundColor: INCOME_COLOR, borderRadius: 4 },
      { label: "Pengeluaran", data: report.rows.map((r) => r.expense), backgroundColor: EXPENSE_COLOR, borderRadius: 4 },
    ],
  };
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { tooltip: { callbacks: { label: (c) => `${c.dataset.label}: ${formatRupiah(c.parsed.y)}` } } },
    scales: { y: { beginAtZero: true, ticks: { callback: compactRupiah } } },
  };
  const cmp = report.comparisonWithPrevious;

  return (
    <>
      <SummaryCards report={report} />
      <div className="grid gap-4 sm:grid-cols-2">
        <StatCard label="Rata-rata pengeluaran harian" value={formatRupiah(report.averageDailyExpense)} />
        <StatCard
          label="Dibanding periode sebelumnya"
          value={cmp == null ? "—" : `${cmp > 0 ? "+" : ""}${formatPersen(cmp)}`}
          tone={cmp == null ? "text-slate-900" : cmp > 0 ? "text-rose-600" : "text-emerald-600"}
          hint="Total pengeluaran, periode sebelumnya sama panjang"
        />
      </div>
      {report.rows.length === 0 ? (
        <EmptyState title="Tidak ada data" description="Tidak ada transaksi pada rentang tanggal ini." />
      ) : (
        <>
          <Card><div className="h-72"><Bar data={chart} options={options} /></div></Card>
          <Table>
            <thead><tr><Th>Periode</Th><Th right>Pemasukan</Th><Th right>Pengeluaran</Th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {report.rows.map((r) => (
                <tr key={r.label}><Td>{r.label}</Td><Td right>{formatRupiah(r.income)}</Td><Td right>{formatRupiah(r.expense)}</Td></tr>
              ))}
            </tbody>
          </Table>
        </>
      )}
    </>
  );
}
