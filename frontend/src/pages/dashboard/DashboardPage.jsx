import { Link } from "react-router-dom";
import { Bar } from "react-chartjs-2";
import { ArrowDownCircle, ArrowUpCircle, PiggyBank, Wallet } from "lucide-react";
import { getDashboard, getPeriodReport } from "../../api/reportApi";
import { getGoals } from "../../api/goalApi";
import { getTransactions } from "../../api/transactionApi";
import useFetch from "../../hooks/useFetch";
import Card, { StatCard } from "../../components/ui/Card";
import EmptyState from "../../components/ui/EmptyState";
import { ErrorState, Spinner } from "../../components/ui/Feedback";
import ProgressBar from "../../components/ui/ProgressBar";
import { EXPENSE_COLOR, INCOME_COLOR, compactRupiah } from "../../utils/chart";
import { akhirBulan, awalBulan, currentMonth, formatBulan, formatPersen, formatRupiah, formatTanggal, geserBulan } from "../../utils/format";

const MONTHS_SHOWN = 6;

export default function DashboardPage() {
  const from = awalBulan(geserBulan(currentMonth(), -(MONTHS_SHOWN - 1)));
  const to = akhirBulan(currentMonth());
  const summary = useFetch(getDashboard);
  const trend = useFetch(() => getPeriodReport({ type: "MONTHLY", from, to }), [from, to]);
  const goals = useFetch(getGoals);
  const recent = useFetch(() => getTransactions({ size: 5, from: "2000-01-01", to: akhirBulan(currentMonth()) }));

  const all = [summary, trend, goals, recent];
  if (all.some((f) => f.loading && !f.data)) return <Spinner />;
  const error = all.find((f) => f.error)?.error;
  if (error) return <ErrorState error={error} onRetry={() => all.forEach((f) => f.reload())} />;

  const s = summary.data;
  const rows = trend.data.rows;
  const activeGoals = goals.data.filter((g) => g.status === "ACTIVE").slice(0, 3);
  const chart = {
    labels: rows.map((r) => formatBulan(r.label).replace(/ \d{4}$/, "")),
    datasets: [
      { label: "Pemasukan", data: rows.map((r) => r.income), backgroundColor: INCOME_COLOR, borderRadius: 4 },
      { label: "Pengeluaran", data: rows.map((r) => r.expense), backgroundColor: EXPENSE_COLOR, borderRadius: 4 },
    ],
  };
  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { tooltip: { callbacks: { label: (c) => `${c.dataset.label}: ${formatRupiah(c.parsed.y)}` } } },
    scales: { y: { beginAtZero: true, ticks: { callback: compactRupiah } } },
  };

  return (
    <>
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Total saldo" value={formatRupiah(s.totalBalance)} icon={Wallet} />
        <StatCard label="Pemasukan bulan ini" value={formatRupiah(s.monthIncome)} tone="text-emerald-600" icon={ArrowDownCircle} />
        <StatCard label="Pengeluaran bulan ini" value={formatRupiah(s.monthExpense)} tone="text-rose-600" icon={ArrowUpCircle} />
        <StatCard label="Saving rate" value={formatPersen(s.savingRate)} icon={PiggyBank} hint="Bulan ini" />
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <h2 className="mb-3 text-base font-semibold">Pemasukan vs Pengeluaran ({MONTHS_SHOWN} bulan terakhir)</h2>
          <div className="h-64"><Bar data={chart} options={chartOptions} /></div>
        </Card>

        <Card>
          <h2 className="mb-3 text-base font-semibold">Anggaran bulan ini</h2>
          {s.activeBudgets === 0 ? (
            <p className="text-sm text-slate-500">Belum ada anggaran. <Link className="font-medium text-emerald-600 hover:underline" to="/budgets">Buat anggaran</Link></p>
          ) : (
            <div className="space-y-2 text-sm">
              <p><span className="text-2xl font-semibold text-slate-900">{s.activeBudgets}</span> anggaran aktif</p>
              <p className={s.overBudgetCount > 0 ? "font-medium text-red-600" : "text-emerald-600"}>
                {s.overBudgetCount > 0 ? `${s.overBudgetCount} anggaran terlampaui` : "Tidak ada yang terlampaui"}
              </p>
              <Link className="inline-block font-medium text-emerald-600 hover:underline" to="/budgets">Lihat detail</Link>
            </div>
          )}
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <h2 className="mb-3 text-base font-semibold">Target tabungan aktif</h2>
          {activeGoals.length === 0 ? (
            <EmptyState title="Belum ada target aktif" description="Buat target tabungan untuk mulai menabung." />
          ) : (
            <ul className="space-y-4">
              {activeGoals.map((g) => (
                <li key={g.planId}>
                  <div className="flex justify-between text-sm"><span className="font-medium text-slate-800">{g.name}</span><span className="tabular-nums">{formatPersen(g.progressPercentage)}</span></div>
                  <div className="mt-1"><ProgressBar percent={g.progressPercentage} label={`Progres ${g.name}`} /></div>
                  <p className="mt-1 text-xs text-slate-400">{formatRupiah(g.currentAmount)} dari {formatRupiah(g.targetAmount)}</p>
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card>
          <h2 className="mb-3 text-base font-semibold">Transaksi terbaru</h2>
          {recent.data.items.length === 0 ? (
            <EmptyState title="Belum ada transaksi" description="Catat transaksi pertamamu." />
          ) : (
            <ul className="divide-y divide-slate-100">
              {recent.data.items.map((t) => {
                const income = t.type === "INCOME";
                return (
                  <li key={t.transactionId} className="flex items-center justify-between py-2 text-sm">
                    <div>
                      <p className="font-medium text-slate-800">{t.category.name}</p>
                      <p className="text-xs text-slate-400">{formatTanggal(t.date)} · {t.wallet.name}</p>
                    </div>
                    <span className={`font-medium tabular-nums ${income ? "text-emerald-600" : "text-rose-600"}`}>
                      {income ? "+" : "−"} {formatRupiah(t.amount)}
                    </span>
                  </li>
                );
              })}
            </ul>
          )}
          <Link className="mt-3 inline-block text-sm font-medium text-emerald-600 hover:underline" to="/transactions">Semua transaksi</Link>
        </Card>
      </div>
    </>
  );
}
