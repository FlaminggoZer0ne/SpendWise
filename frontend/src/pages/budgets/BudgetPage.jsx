import { useState } from "react";
import { ChevronLeft, ChevronRight, Pencil, PieChart, Plus, Trash2 } from "lucide-react";
import { deleteBudget, getBudgets } from "../../api/budgetApi";
import useFetch from "../../hooks/useFetch";
import Button from "../../components/ui/Button";
import Card, { StatCard } from "../../components/ui/Card";
import ConfirmDialog from "../../components/ui/ConfirmDialog";
import EmptyState from "../../components/ui/EmptyState";
import { ErrorState, Spinner } from "../../components/ui/Feedback";
import ProgressBar from "../../components/ui/ProgressBar";
import { useToast } from "../../components/ui/Toast";
import { currentMonth, formatBulan, formatPersen, formatRupiah, geserBulan } from "../../utils/format";
import { budgetTone } from "../../utils/labels";
import BudgetFormModal from "./BudgetFormModal";

const STATUS_LABEL = { SAFE: "Aman", WARNING: "Hampir habis", EXCEEDED: "Terlampaui" };
const STATUS_STYLE = { SAFE: "text-emerald-600", WARNING: "text-amber-600", EXCEEDED: "text-red-600" };

export default function BudgetPage() {
  const toast = useToast();
  const [month, setMonth] = useState(currentMonth());
  const [modal, setModal] = useState(null); // { kind: "form" | "delete", budget? }
  const { data, loading, error, reload } = useFetch(() => getBudgets(month), [month]);

  const saved = (message) => {
    setModal(null);
    toast.success(message);
    reload();
  };

  return (
    <>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm" aria-label="Bulan sebelumnya" onClick={() => setMonth(geserBulan(month, -1))}><ChevronLeft size={16} /></Button>
          <span className="min-w-36 text-center text-base font-semibold text-slate-900">{formatBulan(month)}</span>
          <Button variant="secondary" size="sm" aria-label="Bulan berikutnya" onClick={() => setMonth(geserBulan(month, 1))}><ChevronRight size={16} /></Button>
        </div>
        <Button onClick={() => setModal({ kind: "form" })}><Plus size={16} /> Tambah Anggaran</Button>
      </div>

      {loading && !data ? <Spinner /> : error ? <ErrorState error={error} onRetry={reload} /> : (
        <>
          <div className="grid gap-3 sm:grid-cols-2">
            <StatCard label="Total batas anggaran" value={formatRupiah(data.totalLimit)} icon={PieChart} />
            <StatCard label="Total terpakai" value={formatRupiah(data.totalSpent)} tone="text-rose-600" />
          </div>

          {data.budgets.length === 0 ? (
            <EmptyState
              title="Belum ada anggaran bulan ini"
              description="Batasi pengeluaran per kategori agar keuanganmu terkontrol."
              action={<Button onClick={() => setModal({ kind: "form" })}><Plus size={16} /> Tambah anggaran</Button>}
            />
          ) : (
            <div className="grid gap-4 md:grid-cols-2">
              {data.budgets.map((b) => (
                <Card key={b.planId}>
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="font-semibold">{b.category.name}</h3>
                      <p className={`text-xs font-medium ${STATUS_STYLE[b.status]}`}>{STATUS_LABEL[b.status]}</p>
                    </div>
                    <div className="flex gap-1">
                      <Button variant="ghost" size="sm" aria-label={`Ubah ${b.name}`} onClick={() => setModal({ kind: "form", budget: b })}><Pencil size={14} /></Button>
                      <Button variant="ghost" size="sm" aria-label={`Hapus ${b.name}`} onClick={() => setModal({ kind: "delete", budget: b })}><Trash2 size={14} className="text-red-600" /></Button>
                    </div>
                  </div>
                  <div className="mt-3"><ProgressBar percent={b.progressPercentage} tone={budgetTone(b.status)} label={`Progres ${b.name}`} /></div>
                  <div className="mt-2 flex justify-between text-sm">
                    <span className="tabular-nums">{formatRupiah(b.spentAmount)} / {formatRupiah(b.targetAmount)}</span>
                    <span className="font-medium tabular-nums">{formatPersen(b.progressPercentage)}</span>
                  </div>
                  <p className="mt-1 text-xs text-slate-400">
                    {b.remainingAmount >= 0 ? `Sisa ${formatRupiah(b.remainingAmount)}` : `Lebih ${formatRupiah(-b.remainingAmount)}`}
                  </p>
                </Card>
              ))}
            </div>
          )}
        </>
      )}

      {modal?.kind === "form" && <BudgetFormModal budget={modal.budget} month={month} onClose={() => setModal(null)} onSaved={saved} />}
      {modal?.kind === "delete" && (
        <ConfirmDialog
          title="Hapus anggaran"
          message={`Hapus "${modal.budget.name}" untuk ${formatBulan(month)}?`}
          onClose={() => setModal(null)}
          onConfirm={async () => {
            const res = await deleteBudget(modal.budget.planId);
            toast.success(res.message);
            reload();
          }}
        />
      )}
    </>
  );
}
