import { useState } from "react";
import { transfer } from "../../api/walletApi";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import Modal from "../../components/ui/Modal";
import Select from "../../components/ui/Select";
import { FormError } from "../../components/ui/Feedback";
import { formatRupiah } from "../../utils/format";

export default function TransferModal({ wallets, onClose, onSaved }) {
  const [form, setForm] = useState({
    fromWalletId: wallets[0]?.walletId ?? "",
    toWalletId: wallets[1]?.walletId ?? "",
    amount: "",
  });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const fe = (k) => error?.errors?.[k];

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const res = await transfer({
        fromWalletId: Number(form.fromWalletId),
        toWalletId: Number(form.toWalletId),
        amount: Number(form.amount),
      });
      onSaved(res.message);
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  const options = wallets.map((w) => (
    <option key={w.walletId} value={w.walletId}>{w.name} ({formatRupiah(w.balance)})</option>
  ));

  return (
    <Modal
      title="Transfer Antar Dompet"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>Batal</Button>
          <Button type="submit" form="transfer-form" disabled={busy}>Transfer</Button>
        </>
      }
    >
      <form id="transfer-form" onSubmit={submit} className="space-y-4">
        {!error?.errors && <FormError error={error} />}
        <Select label="Dari dompet" value={form.fromWalletId} error={fe("fromWalletId")} onChange={(e) => setForm({ ...form, fromWalletId: e.target.value })}>{options}</Select>
        <Select label="Ke dompet" value={form.toWalletId} error={fe("toWalletId")} onChange={(e) => setForm({ ...form, toWalletId: e.target.value })}>{options}</Select>
        <Input label="Nominal" type="number" min="1" step="any" value={form.amount} error={fe("amount")} required
          onChange={(e) => setForm({ ...form, amount: e.target.value })} />
      </form>
    </Modal>
  );
}
