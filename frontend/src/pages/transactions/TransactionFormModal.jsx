import { useState } from "react";
import { createTransaction, updateTransaction } from "../../api/transactionApi";
import { getCategories } from "../../api/categoryApi";
import useFetch from "../../hooks/useFetch";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import Modal from "../../components/ui/Modal";
import Select from "../../components/ui/Select";
import { FormError } from "../../components/ui/Feedback";
import { requestNotificationRefresh } from "../../utils/events";
import { formatRupiah, todayISO } from "../../utils/format";
import { TRANSACTION_TYPES } from "../../utils/labels";

/** `transaction` null = tambah; selain itu ubah. `wallets` dimuat oleh halaman induk. */
export default function TransactionFormModal({ transaction, wallets, onClose, onSaved }) {
  const editing = Boolean(transaction);
  const [form, setForm] = useState({
    type: transaction?.type ?? "EXPENSE",
    amount: transaction?.amount ?? "",
    date: transaction?.date ?? todayISO(),
    categoryId: transaction?.category.categoryId ?? "",
    walletId: transaction?.wallet.walletId ?? wallets[0]?.walletId ?? "",
    description: transaction?.description ?? "",
    source: transaction?.source ?? "",
    paymentMethod: transaction?.paymentMethod ?? "",
    isRecurring: transaction?.isRecurring ?? false,
  });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const fe = (k) => error?.errors?.[k];
  const { data: categories } = useFetch(() => getCategories(form.type), [form.type]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const chosenWallet = wallets.find((w) => String(w.walletId) === String(form.walletId));
  const categoryId = categories?.some((c) => String(c.categoryId) === String(form.categoryId))
    ? form.categoryId
    : categories?.[0]?.categoryId ?? "";

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const body = {
      type: form.type,
      amount: Number(form.amount),
      date: form.date,
      categoryId: Number(categoryId),
      walletId: Number(form.walletId),
      description: form.description,
      source: form.type === "INCOME" ? form.source : null,
      paymentMethod: form.type === "EXPENSE" ? form.paymentMethod : null,
      isRecurring: form.type === "EXPENSE" ? form.isRecurring : false,
    };
    try {
      const res = editing ? await updateTransaction(transaction.transactionId, body) : await createTransaction(body);
      requestNotificationRefresh();
      onSaved(res.message);
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  return (
    <Modal
      title={editing ? "Ubah Transaksi" : "Tambah Transaksi"}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>Batal</Button>
          <Button type="submit" form="trx-form" disabled={busy}>Simpan</Button>
        </>
      }
    >
      <form id="trx-form" onSubmit={submit} className="space-y-4">
        {!error?.errors && <FormError error={error} />}
        <div className="grid grid-cols-2 gap-2" role="group" aria-label="Jenis transaksi">
          {TRANSACTION_TYPES.slice().reverse().map((t) => (
            <button
              key={t.value}
              type="button"
              onClick={() => setForm({ ...form, type: t.value })}
              className={`rounded-lg border px-3 py-2 text-sm font-medium ${
                form.type === t.value
                  ? t.value === "INCOME" ? "border-emerald-600 bg-emerald-50 text-emerald-700" : "border-rose-600 bg-rose-50 text-rose-700"
                  : "border-slate-300 text-slate-600 hover:bg-slate-50"
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
        <Input label="Nominal" type="number" min="1" step="any" value={form.amount} error={fe("amount")} required onChange={set("amount")} />
        <Input label="Tanggal" type="date" max={todayISO()} value={form.date} error={fe("date")} required onChange={set("date")} />
        <Select label="Kategori" value={categoryId} error={fe("categoryId")} onChange={set("categoryId")}>
          {(categories ?? []).map((c) => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
        </Select>
        <Select label="Dompet" value={form.walletId} error={fe("walletId")} onChange={set("walletId")}
          hint={chosenWallet ? `Saldo: ${formatRupiah(chosenWallet.balance)}` : undefined}>
          {wallets.map((w) => <option key={w.walletId} value={w.walletId}>{w.name}</option>)}
        </Select>
        <Input label="Catatan (opsional)" maxLength={255} value={form.description} error={fe("description")} onChange={set("description")} />
        {form.type === "INCOME" ? (
          <Input label="Sumber (opsional)" maxLength={100} value={form.source} onChange={set("source")} />
        ) : (
          <>
            <Input label="Metode bayar (opsional)" maxLength={30} value={form.paymentMethod} onChange={set("paymentMethod")} />
            <label className="flex items-center gap-2 text-sm text-slate-700">
              <input type="checkbox" checked={form.isRecurring} className="rounded border-slate-300 text-emerald-600"
                onChange={(e) => setForm({ ...form, isRecurring: e.target.checked })} />
              Pengeluaran berulang
            </label>
          </>
        )}
      </form>
    </Modal>
  );
}
