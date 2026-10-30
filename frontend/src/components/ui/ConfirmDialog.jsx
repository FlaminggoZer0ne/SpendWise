import { useState } from "react";
import Button from "./Button";
import Modal from "./Modal";
import { FormError } from "./Feedback";

/** `onConfirm` mengembalikan promise; galat dari backend ditampilkan di dalam dialog. */
export default function ConfirmDialog({ title, message, confirmLabel = "Hapus", onConfirm, onClose, variant = "danger" }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);

  const handle = async () => {
    setBusy(true);
    setError(null);
    try {
      await onConfirm();
      onClose();
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  return (
    <Modal
      title={title}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>
            Batal
          </Button>
          <Button variant={variant} onClick={handle} disabled={busy}>
            {busy ? "Memproses..." : confirmLabel}
          </Button>
        </>
      }
    >
      <p className="text-sm text-slate-600">{message}</p>
      <div className="mt-3">
        <FormError error={error} />
      </div>
    </Modal>
  );
}
