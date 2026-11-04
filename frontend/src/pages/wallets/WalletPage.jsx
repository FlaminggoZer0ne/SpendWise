import { useState } from "react";
import { ArrowRightLeft, Banknote, Landmark, Pencil, Plus, Smartphone, Trash2, Wallet as WalletIcon } from "lucide-react";
import { deleteWallet, getWallets } from "../../api/walletApi";
import useFetch from "../../hooks/useFetch";
import Button from "../../components/ui/Button";
import Card, { HeroCard } from "../../components/ui/Card";
import ConfirmDialog from "../../components/ui/ConfirmDialog";
import EmptyState from "../../components/ui/EmptyState";
import { ErrorState, Spinner } from "../../components/ui/Feedback";
import { useToast } from "../../components/ui/Toast";
import { formatRupiah } from "../../utils/format";
import { walletTypeLabel } from "../../utils/labels";
import TransferModal from "./TransferModal";
import WalletFormModal from "./WalletFormModal";

const ICONS = { CASH: Banknote, BANK: Landmark, E_WALLET: Smartphone };

export default function WalletPage() {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(getWallets);
  const [modal, setModal] = useState(null); // { kind: "form" | "transfer" | "delete", wallet? }

  const saved = (message) => {
    setModal(null);
    toast.success(message);
    reload();
  };

  if (loading && !data) return <Spinner />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  const wallets = data.wallets;

  return (
    <>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <HeroCard label="Total saldo semua dompet" value={formatRupiah(data.totalBalance)} className="min-w-[260px] flex-1" />
        <div className="flex gap-2">
          <Button variant="secondary" disabled={wallets.length < 2} onClick={() => setModal({ kind: "transfer" })}>
            <ArrowRightLeft size={16} /> Transfer
          </Button>
          <Button onClick={() => setModal({ kind: "form" })}>
            <Plus size={16} /> Tambah Dompet
          </Button>
        </div>
      </div>

      {wallets.length === 0 ? (
        <EmptyState title="Belum ada dompet" description="Tambahkan dompet pertama Anda." />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {wallets.map((w) => {
            const Icon = ICONS[w.walletType] ?? WalletIcon;
            return (
              <Card key={w.walletId}>
                <div className="flex items-start justify-between">
                  <span className="rounded-lg bg-emerald-50 p-2 text-emerald-600"><Icon size={20} /></span>
                  <div className="flex gap-1">
                    <Button variant="ghost" size="sm" aria-label={`Ubah ${w.name}`} onClick={() => setModal({ kind: "form", wallet: w })}><Pencil size={14} /></Button>
                    <Button variant="ghost" size="sm" aria-label={`Hapus ${w.name}`} onClick={() => setModal({ kind: "delete", wallet: w })}><Trash2 size={14} className="text-red-600" /></Button>
                  </div>
                </div>
                <p className="mt-3 font-medium text-slate-900">{w.name}</p>
                <p className="text-xs text-slate-400">{walletTypeLabel(w.walletType)}</p>
                <p className="mt-2 text-xl font-semibold tabular-nums text-slate-900">{formatRupiah(w.balance)}</p>
              </Card>
            );
          })}
        </div>
      )}

      {modal?.kind === "form" && <WalletFormModal wallet={modal.wallet} onClose={() => setModal(null)} onSaved={saved} />}
      {modal?.kind === "transfer" && <TransferModal wallets={wallets} onClose={() => setModal(null)} onSaved={saved} />}
      {modal?.kind === "delete" && (
        <ConfirmDialog
          title="Hapus dompet"
          message={`Hapus dompet "${modal.wallet.name}"? Dompet yang masih memiliki transaksi tidak dapat dihapus.`}
          onClose={() => setModal(null)}
          onConfirm={async () => {
            const res = await deleteWallet(modal.wallet.walletId);
            toast.success(res.message);
            reload();
          }}
        />
      )}
    </>
  );
}
