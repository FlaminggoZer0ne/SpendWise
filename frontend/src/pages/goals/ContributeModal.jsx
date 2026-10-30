import { useState } from "react";
import { contribute } from "../../api/goalApi";
import { getWallets } from "../../api/walletApi";
import useFetch from "../../hooks/useFetch";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import Modal from "../../components/ui/Modal";
import Select from "../../components/ui/Select";
import { FormError, Spinner } from "../../components/ui/Feedback";
import { formatRupiah } from "../../utils/format";

export default function ContributeModal({ goal, onClose, onSaved }) {
  const { data, loading } = useFetch(getWallets);
  const [walletId, setWalletId] = useState("");
  const [amount, setAmount] = useState("");
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const fe = (k) => error?.errors?.[k];
  const wallets = data?.wallets ?? [];
  const chosen = walletId || wallets[0]?.walletId || "";

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const res = await contribute(goal.planId, { walletId: Number(chosen), amount: Number(amount) });
      onSaved(res.message);
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  return (
    <Modal
      title={`Setor ke "${goal.name}"`}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>Batal</Button>
          <Button type="submit" form="contribute-form" disabled={busy || loading}>Setor</Button>
        </>
      }
    >
      {loading ? <Spinner /> : (
        <form id="contribute-form" onSubmit={submit} className="space-y-4">
          {!error?.errors && <FormError error={error} />}
          <p className="text-sm text-slate-500">Sisa target: <span className="font-medium text-slate-900">{formatRupiah(goal.remainingAmount)}</span></p>
          <Select label="Dompet sumber" value={chosen} error={fe("walletId")} onChange={(e) => setWalletId(e.target.value)}>
            {wallets.map((w) => <option key={w.walletId} value={w.walletId}>{w.name} ({formatRupiah(w.balance)})</option>)}
          </Select>
          <Input label="Nominal" type="number" min="1" step="any" value={amount} required autoFocus error={fe("amount")}
            onChange={(e) => setAmount(e.target.value)} />
        </form>
      )}
    </Modal>
  );
}
