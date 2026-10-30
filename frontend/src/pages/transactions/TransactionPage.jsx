import { useState } from "react";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { deleteTransaction, getTransactions } from "../../api/transactionApi";
import { getCategories } from "../../api/categoryApi";
import { getWallets } from "../../api/walletApi";
import useFetch from "../../hooks/useFetch";
import Button from "../../components/ui/Button";
import { StatCard } from "../../components/ui/Card";
import ConfirmDialog from "../../components/ui/ConfirmDialog";
import EmptyState from "../../components/ui/EmptyState";
import { ErrorState, Spinner } from "../../components/ui/Feedback";
import Pagination from "../../components/ui/Pagination";
import { Table, Td, Th } from "../../components/ui/Table";
import { useToast } from "../../components/ui/Toast";
import { requestNotificationRefresh } from "../../utils/events";
import { akhirBulan, awalBulan, currentMonth, formatRupiah, formatTanggal } from "../../utils/format";
import TransactionFilterBar from "./TransactionFilterBar";
import TransactionFormModal from "./TransactionFormModal";

const PAGE_SIZE = 10;

export default function TransactionPage() {
  const toast = useToast();
  const [filter, setFilter] = useState({
    from: awalBulan(currentMonth()),
    to: akhirBulan(currentMonth()),
    type: "",
    categoryId: "",
    walletId: "",
    q: "",
    page: 1,
  });
  const [modal, setModal] = useState(null); // { kind: "form" | "delete", transaction? }

  const params = Object.fromEntries(Object.entries({ ...filter, size: PAGE_SIZE }).filter(([, v]) => v !== ""));
  const trx = useFetch(() => getTransactions(params), [JSON.stringify(params)]);
  const cats = useFetch(() => getCategories());
  const wallets = useFetch(() => getWallets());

  const saved = (message) => {
    setModal(null);
    toast.success(message);
    trx.reload();
    wallets.reload();
  };

  if (cats.loading || wallets.loading || (trx.loading && !trx.data)) return <Spinner />;
  const error = trx.error ?? cats.error ?? wallets.error;
  if (error) return <ErrorState error={error} onRetry={() => { trx.reload(); cats.reload(); wallets.reload(); }} />;

  const page = trx.data;
  const walletList = wallets.data.wallets;

  return (
    <>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="grid flex-1 gap-3 sm:grid-cols-3">
          <StatCard label="Total pemasukan" value={formatRupiah(page.totalIncome)} tone="text-emerald-600" />
          <StatCard label="Total pengeluaran" value={formatRupiah(page.totalExpense)} tone="text-rose-600" />
          <StatCard label="Jumlah transaksi" value={page.totalItems} />
        </div>
        <Button onClick={() => setModal({ kind: "form" })}><Plus size={16} /> Transaksi</Button>
      </div>

      <TransactionFilterBar filter={filter} onChange={setFilter} categories={cats.data} wallets={walletList} />

      {page.items.length === 0 ? (
        <EmptyState
          title="Belum ada transaksi"
          description="Tidak ada transaksi pada filter ini. Catat transaksi pertamamu."
          action={<Button onClick={() => setModal({ kind: "form" })}><Plus size={16} /> Catat transaksi</Button>}
        />
      ) : (
        <>
          <Table>
            <thead>
              <tr><Th>Tanggal</Th><Th>Kategori</Th><Th>Catatan</Th><Th>Dompet</Th><Th right>Nominal</Th><Th right>Aksi</Th></tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {page.items.map((t) => {
                const income = t.type === "INCOME";
                return (
                  <tr key={t.transactionId}>
                    <Td className="whitespace-nowrap">{formatTanggal(t.date)}</Td>
                    <Td>{t.category.name}</Td>
                    <Td className="max-w-xs truncate">{t.description ?? "-"}</Td>
                    <Td>{t.wallet.name}</Td>
                    <Td right className={`whitespace-nowrap font-medium ${income ? "text-emerald-600" : "text-rose-600"}`}>
                      {income ? "+" : "−"} {formatRupiah(t.amount)}
                    </Td>
                    <Td right className="whitespace-nowrap">
                      <Button variant="ghost" size="sm" aria-label="Ubah transaksi" onClick={() => setModal({ kind: "form", transaction: t })}><Pencil size={14} /></Button>
                      <Button variant="ghost" size="sm" aria-label="Hapus transaksi" onClick={() => setModal({ kind: "delete", transaction: t })}><Trash2 size={14} className="text-red-600" /></Button>
                    </Td>
                  </tr>
                );
              })}
            </tbody>
          </Table>
          <Pagination page={page.page} totalPages={page.totalPages} totalItems={page.totalItems} onChange={(p) => setFilter({ ...filter, page: p })} />
        </>
      )}

      {modal?.kind === "form" && (
        <TransactionFormModal transaction={modal.transaction} wallets={walletList} onClose={() => setModal(null)} onSaved={saved} />
      )}
      {modal?.kind === "delete" && (
        <ConfirmDialog
          title="Hapus transaksi"
          message="Hapus transaksi ini? Saldo dompet akan dikembalikan."
          onClose={() => setModal(null)}
          onConfirm={async () => {
            const res = await deleteTransaction(modal.transaction.transactionId);
            toast.success(res.message);
            requestNotificationRefresh();
            trx.reload();
            wallets.reload();
          }}
        />
      )}
    </>
  );
}
