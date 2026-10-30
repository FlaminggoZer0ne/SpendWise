import { useState } from "react";
import { createGoal } from "../../api/goalApi";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import Modal from "../../components/ui/Modal";
import { FormError } from "../../components/ui/Feedback";

export default function GoalFormModal({ onClose, onSaved }) {
  const [form, setForm] = useState({ name: "", targetAmount: "", deadline: "" });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const fe = (k) => error?.errors?.[k];

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const res = await createGoal({ name: form.name, targetAmount: Number(form.targetAmount), deadline: form.deadline });
      onSaved(res.message);
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  return (
    <Modal
      title="Target Tabungan Baru"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>Batal</Button>
          <Button type="submit" form="goal-form" disabled={busy}>Simpan</Button>
        </>
      }
    >
      <form id="goal-form" onSubmit={submit} className="space-y-4">
        {!error?.errors && <FormError error={error} />}
        <Input label="Nama target" value={form.name} maxLength={100} required autoFocus error={fe("name")}
          onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <Input label="Nominal target" type="number" min="1" step="any" value={form.targetAmount} required
          onChange={(e) => setForm({ ...form, targetAmount: e.target.value })} />
        <Input label="Tenggat" type="date" value={form.deadline} required
          onChange={(e) => setForm({ ...form, deadline: e.target.value })} />
      </form>
    </Modal>
  );
}
