import { useState } from "react";
import { PiggyBank, Plus } from "lucide-react";
import { cancelGoal, getGoals } from "../../api/goalApi";
import useFetch from "../../hooks/useFetch";
import Button from "../../components/ui/Button";
import Card from "../../components/ui/Card";
import ConfirmDialog from "../../components/ui/ConfirmDialog";
import EmptyState from "../../components/ui/EmptyState";
import { ErrorState, Spinner } from "../../components/ui/Feedback";
import ProgressBar from "../../components/ui/ProgressBar";
import { useToast } from "../../components/ui/Toast";
import { formatPersen, formatRupiah, formatTanggal } from "../../utils/format";
import { GOAL_STATUS } from "../../utils/labels";
import ContributeModal from "./ContributeModal";
import GoalFormModal from "./GoalFormModal";

export default function SavingGoalPage() {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(getGoals);
  const [modal, setModal] = useState(null); // { kind: "form" | "contribute" | "cancel", goal? }

  const saved = (message) => {
    setModal(null);
    toast.success(message);
    reload();
  };

  if (loading && !data) return <Spinner />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  return (
    <>
      <div className="flex justify-end">
        <Button onClick={() => setModal({ kind: "form" })}><Plus size={16} /> Target Baru</Button>
      </div>

      {data.length === 0 ? (
        <EmptyState
          icon={PiggyBank}
          title="Belum ada target tabungan"
          description="Buat target untuk membeli sesuatu yang kamu impikan."
          action={<Button onClick={() => setModal({ kind: "form" })}><Plus size={16} /> Buat target</Button>}
        />
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {data.map((g) => {
            const status = GOAL_STATUS[g.status];
            const active = g.status === "ACTIVE";
            return (
              <Card key={g.planId} className={active ? "" : "opacity-80"}>
                <div className="flex items-start justify-between">
                  <h3 className="font-semibold">{g.name}</h3>
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${status.style}`}>{status.label}</span>
                </div>
                <div className="mt-3"><ProgressBar percent={g.progressPercentage} label={`Progres ${g.name}`} /></div>
                <div className="mt-2 flex justify-between text-sm">
                  <span className="tabular-nums">{formatRupiah(g.currentAmount)} / {formatRupiah(g.targetAmount)}</span>
                  <span className="font-medium tabular-nums">{formatPersen(g.progressPercentage)}</span>
                </div>
                <dl className="mt-3 grid grid-cols-2 gap-2 text-xs text-slate-500">
                  <div><dt>Tenggat</dt><dd className="font-medium text-slate-800">{formatTanggal(g.deadline)}</dd></div>
                  <div><dt>Sisa hari</dt><dd className="font-medium text-slate-800">{g.daysRemaining} hari</dd></div>
                  {active && (
                    <div className="col-span-2">
                      <dt>Rekomendasi setoran per bulan</dt>
                      <dd className="font-medium text-slate-800">{formatRupiah(g.recommendedMonthlySaving)}</dd>
                    </div>
                  )}
                </dl>
                {active && (
                  <div className="mt-4 flex gap-2">
                    <Button size="sm" onClick={() => setModal({ kind: "contribute", goal: g })}>Setor</Button>
                    <Button size="sm" variant="secondary" onClick={() => setModal({ kind: "cancel", goal: g })}>Batalkan</Button>
                  </div>
                )}
              </Card>
            );
          })}
        </div>
      )}

      {modal?.kind === "form" && <GoalFormModal onClose={() => setModal(null)} onSaved={saved} />}
      {modal?.kind === "contribute" && <ContributeModal goal={modal.goal} onClose={() => setModal(null)} onSaved={saved} />}
      {modal?.kind === "cancel" && (
        <ConfirmDialog
          title="Batalkan target"
          message={`Batalkan "${modal.goal.name}"? Dana terkumpul ${formatRupiah(modal.goal.currentAmount)} dikembalikan ke dompet utama.`}
          confirmLabel="Batalkan target"
          onClose={() => setModal(null)}
          onConfirm={async () => {
            const res = await cancelGoal(modal.goal.planId);
            toast.success(res.message);
            reload();
          }}
        />
      )}
    </>
  );
}
