import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../../api/authApi";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import { FormError } from "../../components/ui/Feedback";
import AmbientBackground from "../../components/layout/AmbientBackground";
import { useToast } from "../../components/ui/Toast";

export default function RegisterPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const [form, setForm] = useState({ username: "", fullName: "", email: "", password: "", confirmPassword: "" });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const fe = (k) => error?.errors?.[k];

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const res = await register(form);
      toast.success(res.message);
      navigate("/login");
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  return (
    <div className="relative flex min-h-screen items-center justify-center px-4 py-8">
      <AmbientBackground />
      <form onSubmit={submit} className="glass-strong relative z-10 w-full max-w-sm space-y-4 rounded-3xl p-6">
        <h1 className="text-center text-xl font-bold">Buat Akun SpendWise</h1>
        {!error?.errors && <FormError error={error} />}
        <Input label="Username" value={form.username} onChange={set("username")} error={fe("username")} hint="4-30 karakter: huruf, angka, garis bawah" required />
        <Input label="Nama lengkap" value={form.fullName} onChange={set("fullName")} error={fe("fullName")} required />
        <Input label="Email" type="email" value={form.email} onChange={set("email")} error={fe("email")} required />
        <Input label="Password" type="password" value={form.password} onChange={set("password")} error={fe("password")} hint="Minimal 8 karakter" required />
        <Input label="Konfirmasi password" type="password" value={form.confirmPassword} onChange={set("confirmPassword")} error={fe("confirmPassword")} required />
        <Button type="submit" className="w-full" disabled={busy}>{busy ? "Memproses..." : "Daftar"}</Button>
        <p className="text-center text-sm text-slate-500">
          Sudah punya akun? <Link to="/login" className="font-medium text-emerald-600 hover:underline">Masuk</Link>
        </p>
      </form>
    </div>
  );
}
