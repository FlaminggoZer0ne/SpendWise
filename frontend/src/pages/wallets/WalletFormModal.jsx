import { useState } from "react";
import { createWallet, updateWallet } from "../../api/walletApi";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import Modal from "../../components/ui/Modal";
import Select from "../../components/ui/Select";
import { FormError } from "../../components/ui/Feedback";
import { WALLET_TYPES } from "../../utils/labels";

/** `wallet` null = tambah baru (ada saldo awal); selain itu ubah (hanya nama & jenis, saldo tidak bisa diubah). */
export default function WalletFormModal({ wallet, onClose, onSaved }) {
  const editing = Boolean(wallet);
  const [form, setForm] = useState({
    name: wallet?.name ?? "",
    walletType: wallet?.walletType ?? "CASH",
    initialBalance: "",
  });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const body = { name: form.name, walletType: form.walletType };
      const res = editing
        ? await updateWallet(wallet.walletId, body)
        : await createWallet({ ...body, initialBalance: form.initialBalance === "" ? 0 : Number(form.initialBalance) });
      onSaved(res.message);
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  return (
    <Modal
      title={editing ? "Ubah Dompet" : "Tambah Dompet"}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>Batal</Button>
          <Button type="submit" form="wallet-form" disabled={busy}>Simpan</Button>
        </>
      }
    >
      <form id="wallet-form" onSubmit={submit} className="space-y-4">
        <FormError error={error} />
        <Input label="Nama dompet" value={form.name} maxLength={50} required autoFocus
          onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <Select label="Jenis" value={form.walletType} onChange={(e) => setForm({ ...form, walletType: e.target.value })}>
          {WALLET_TYPES.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
        </Select>
        {editing ? (
          <p className="text-xs text-slate-400">Saldo hanya berubah lewat transaksi atau transfer.</p>
        ) : (
          <Input label="Saldo awal" type="number" min="0" step="any" value={form.initialBalance} placeholder="0"
            onChange={(e) => setForm({ ...form, initialBalance: e.target.value })} />
        )}
      </form>
    </Modal>
  );
}
