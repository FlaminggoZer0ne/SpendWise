import { Pie } from "react-chartjs-2";
import Card, { StatCard } from "../../components/ui/Card";
import EmptyState from "../../components/ui/EmptyState";
import { Table, Td, Th } from "../../components/ui/Table";
import { PIE_COLORS } from "../../utils/chart";
import { formatPersen, formatRupiah } from "../../utils/format";

export function SummaryCards({ report }) {
  return (
    <div className="grid gap-4 sm:grid-cols-3">
      <StatCard label="Total pemasukan" value={formatRupiah(report.totalIncome)} tone="text-emerald-600" />
      <StatCard label="Total pengeluaran" value={formatRupiah(report.totalExpense)} tone="text-rose-600" />
      <StatCard label="Saldo bersih" value={formatRupiah(report.netBalance)} tone={report.netBalance < 0 ? "text-red-600" : "text-slate-900"} />
    </div>
  );
}

export default function CategoryReportView({ report }) {
  const chart = {
    labels: report.rows.map((r) => r.category),
    datasets: [{ data: report.rows.map((r) => r.total), backgroundColor: report.rows.map((_, i) => PIE_COLORS[i % PIE_COLORS.length]) }],
  };
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: "bottom" },
      tooltip: { callbacks: { label: (c) => `${c.label}: ${formatRupiah(c.parsed)}` } },
    },
  };

  return (
    <>
      <SummaryCards report={report} />
      {report.rows.length === 0 ? (
        <EmptyState title="Tidak ada pengeluaran" description="Tidak ada pengeluaran pada rentang tanggal ini." />
      ) : (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <Card>
            <h2 className="mb-1 text-base font-semibold">Pengeluaran per kategori</h2>
            <p className="mb-3 text-sm text-slate-500">Terbesar: <span className="font-medium text-slate-800">{report.topExpenseCategory}</span></p>
            <div className="h-72"><Pie data={chart} options={options} /></div>
          </Card>
          <Table>
            <thead><tr><Th>Kategori</Th><Th right>Total</Th><Th right>Persentase</Th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {report.rows.map((r) => (
                <tr key={r.category}><Td>{r.category}</Td><Td right>{formatRupiah(r.total)}</Td><Td right>{formatPersen(r.percentage)}</Td></tr>
              ))}
            </tbody>
          </Table>
        </div>
      )}
    </>
  );
}
