import { useState } from "react";
import { createBudget, updateBudget } from "../../api/budgetApi";
import { getCategories } from "../../api/categoryApi";
import useFetch from "../../hooks/useFetch";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import Modal from "../../components/ui/Modal";
import Select from "../../components/ui/Select";
import { FormError } from "../../components/ui/Feedback";
import { formatBulan } from "../../utils/format";

/** `budget` null = tambah (pilih kategori); selain itu ubah (hanya batas). */
export default function BudgetFormModal({ budget, month, onClose, onSaved }) {
  const editing = Boolean(budget);
  const [targetAmount, setTargetAmount] = useState(budget?.targetAmount ?? "");
  const [categoryId, setCategoryId] = useState("");
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const fe = (k) => error?.errors?.[k];
  const { data: categories } = useFetch(() => getCategories("EXPENSE"));
  const chosen = categoryId || categories?.[0]?.categoryId || "";

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const res = editing
        ? await updateBudget(budget.planId, { targetAmount: Number(targetAmount) })
        : await createBudget({ categoryId: Number(chosen), month, targetAmount: Number(targetAmount) });
      onSaved(res.message);
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  return (
    <Modal
      title={editing ? `Ubah ${budget.name}` : `Tambah Anggaran ${formatBulan(month)}`}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>Batal</Button>
          <Button type="submit" form="budget-form" disabled={busy}>Simpan</Button>
        </>
      }
    >
      <form id="budget-form" onSubmit={submit} className="space-y-4">
        {!error?.errors && <FormError error={error} />}
        {!editing && (
          <Select label="Kategori pengeluaran" value={chosen} onChange={(e) => setCategoryId(e.target.value)} error={fe("categoryId")}>
            {(categories ?? []).map((c) => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
          </Select>
        )}
        <Input label="Batas anggaran" type="number" min="1" step="any" value={targetAmount} required autoFocus
          error={fe("targetAmount")} onChange={(e) => setTargetAmount(e.target.value)} />
      </form>
    </Modal>
  );
}
