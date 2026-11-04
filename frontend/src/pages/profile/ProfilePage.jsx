import { useState } from "react";
import { changePassword, updateProfile } from "../../api/authApi";
import { useAuth } from "../../context/AuthContext";
import Button from "../../components/ui/Button";
import Card from "../../components/ui/Card";
import Input from "../../components/ui/Input";
import { FormError } from "../../components/ui/Feedback";
import { useToast } from "../../components/ui/Toast";
import { formatTanggal } from "../../utils/format";

function ProfileForm() {
  const { user, setUser } = useAuth();
  const toast = useToast();
  const [form, setForm] = useState({ fullName: user.fullName, email: user.email });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const fe = (k) => error?.errors?.[k];

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const res = await updateProfile(form);
      setUser(res.data);
      toast.success(res.message);
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card>
      <h2 className="mb-4 text-base font-semibold">Data Akun</h2>
      <form onSubmit={submit} className="space-y-4">
        {!error?.errors && <FormError error={error} />}
        <Input label="Username" value={user.username} disabled />
        <Input label="Nama lengkap" value={form.fullName} error={fe("fullName")} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
        <Input label="Email" type="email" value={form.email} error={fe("email")} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        <p className="text-xs text-slate-400">Bergabung sejak {formatTanggal(user.createdAt?.slice(0, 10))}</p>
        <Button type="submit" disabled={busy}>Simpan perubahan</Button>
      </form>
    </Card>
  );
}

function PasswordForm() {
  const toast = useToast();
  const empty = { oldPassword: "", newPassword: "", confirmPassword: "" };
  const [form, setForm] = useState(empty);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const fe = (k) => error?.errors?.[k];

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const res = await changePassword(form);
      toast.success(res.message);
      setForm(empty);
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card>
      <h2 className="mb-4 text-base font-semibold">Ganti Password</h2>
      <form onSubmit={submit} className="space-y-4">
        {!error?.errors && <FormError error={error} />}
        <Input label="Password lama" type="password" value={form.oldPassword} error={fe("oldPassword")} onChange={(e) => setForm({ ...form, oldPassword: e.target.value })} />
        <Input label="Password baru" type="password" value={form.newPassword} error={fe("newPassword")} hint="Minimal 8 karakter" onChange={(e) => setForm({ ...form, newPassword: e.target.value })} />
        <Input label="Konfirmasi password baru" type="password" value={form.confirmPassword} error={fe("confirmPassword")} onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })} />
        <Button type="submit" disabled={busy}>Ganti password</Button>
      </form>
    </Card>
  );
}

export default function ProfilePage() {
  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <ProfileForm />
      <PasswordForm />
    </div>
  );
}
